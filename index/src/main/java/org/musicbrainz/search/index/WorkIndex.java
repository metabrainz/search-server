/*
 * MusicBrainz Search Server
 * Copyright (C) 2009  Aurélien Mino

 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package org.musicbrainz.search.index;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.helper.AliasHelper;
import org.musicbrainz.search.helper.LinkedArtistsHelper;
import org.musicbrainz.search.helper.LinkedRecordingsHelper;
import org.musicbrainz.search.helper.TagHelper;
import org.musicbrainz.search.type.RelationTypes;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.*;
import java.util.*;


public class WorkIndex extends DatabaseIndex {

    public static final String INDEX_NAME = "work";


    public WorkIndex(Connection dbConnection) {
        super(dbConnection);
    }

    public WorkIndex() {
    }

    public String getName() {
        return WorkIndex.INDEX_NAME;
    }

    public Analyzer getAnalyzer() {
        return DatabaseIndex.getAnalyzer(WorkIndexField.class);
    }

    @Override
    public IndexField getIdentifierField() {
        return WorkIndexField.ID;
    }

    public int getMaxId() throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM work");
        rs.next();
        return rs.getInt(1);
    }

    public int getNoOfRows(int maxId) throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT count(*) FROM work WHERE id<=" + maxId);
        rs.next();
        return rs.getInt(1);
    }

    @Override
    public void init(IndexWriter indexWriter, boolean isUpdater) throws SQLException {

        addPreparedStatement("TAGS", TagHelper.constructTagQuery("work_tag", "work"));
        addPreparedStatement("ALIASES", AliasHelper.constructAliasQuery("work"));
        addPreparedStatement("ARTISTS",LinkedArtistsHelper.constructArtistRelationQuery("l_artist_work", "work"));
        addPreparedStatement("RECORDINGS", LinkedRecordingsHelper.constructRecordingRelationQuery("l_recording_work", "work"));

        addPreparedStatement("ISWCS",
                "SELECT work, iswc" +
                        " FROM iswc " +
                        " WHERE work BETWEEN ? AND ?");

        addPreparedStatement("WORKS",
                "SELECT w.id as wid, w.gid, w.name as name, wt.name as type, l.iso_code_3 as language, comment " +
                        " FROM work AS w " +
                        "  LEFT JOIN work_type wt ON w.type = wt.id " +
                        "  LEFT JOIN language l on w.language = l.id " +
                        " WHERE w.id BETWEEN ? AND ? " +
                        " ORDER BY w.id");
    }

    /**
     * Load work iswcs
     *
     * @param min
     * @param max
     * @return
     * @throws SQLException
     * @throws IOException
     */
    private Map<Integer, List<String>> loadISWCs(int min, int max) throws SQLException, IOException {
        Map<Integer, List<String>> iswcs = new HashMap<Integer, List<String>>();
        PreparedStatement st = getPreparedStatement("ISWCS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int workId = rs.getInt("work");

            List<String> list;
            if (!iswcs.containsKey(workId)) {
                list = new LinkedList<String>();
                iswcs.put(workId, list);
            } else {
                list = iswcs.get(workId);
            }
            list.add(rs.getString("iswc"));
        }
        rs.close();
        return iswcs;
    }
    /**
     * Index data with workids between min and max
     *
     * @param indexWriter
     * @param min
     * @param max
     * @throws SQLException
     * @throws IOException
     */
    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {

        Map<Integer, List<Tag>>              tags               = TagHelper.loadTags(min, max, getPreparedStatement("TAGS"), "work");
        ArrayListMultimap<Integer, Relation> artistRelations    = LinkedArtistsHelper.loadArtistRelations(min, max, getPreparedStatement("ARTISTS"));
        ArrayListMultimap<Integer, Relation> recordingRelations = LinkedRecordingsHelper.loadRecordingRelations(min, max, getPreparedStatement("RECORDINGS"));
        Map<Integer, Set<Alias>>             aliases            = AliasHelper.completeFromDbResults(min, max, getPreparedStatement("ALIASES"));
        Map<Integer, List<String>>           iswcs              = loadISWCs(min,max);

        //Works
        PreparedStatement st = getPreparedStatement("WORKS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, tags, artistRelations, recordingRelations, aliases, iswcs));
        }
        rs.close();

    }

    public Document documentFromResultSet(ResultSet rs,
                                          Map<Integer, List<Tag>> tags,
                                          ArrayListMultimap<Integer, Relation> artistRelations,
                                          ArrayListMultimap<Integer, Relation> recordingRelations,
                                          Map<Integer, Set<Alias>>   aliases,
                                          Map<Integer, List<String>> iswcs
                                          ) throws SQLException {
        MbDocument doc = new MbDocument();

        ObjectFactory of = new ObjectFactory();
        Work work = of.createWork();

        int id = rs.getInt("wid");
        String guid = rs.getString("gid");
        doc.addField(WorkIndexField.ID, id);
        doc.addField(WorkIndexField.WORK_ID, guid);
        work.setId(guid);

        String name = rs.getString("name");
        doc.addField(WorkIndexField.WORK, name);
        doc.addField(WorkIndexField.WORK_ACCENT, name);
        work.setTitle(name);

        String language = rs.getString("language");
        doc.addFieldOrNoValue(WorkIndexField.LYRICS_LANG,language);
        if (!Strings.isNullOrEmpty(language)) {
            work.setLanguage(language);
        }

        String type = rs.getString("type");
        doc.addFieldOrNoValue(WorkIndexField.TYPE, type);
        if (!Strings.isNullOrEmpty(type)) {
            work.setType(type);
        }


        String comment = rs.getString("comment");
        doc.addFieldOrNoValue(WorkIndexField.COMMENT, comment);
        if (!Strings.isNullOrEmpty(comment)) {
            work.setDisambiguation(comment);
        }

        if (artistRelations.containsKey(id)) {
            work.getRelationList().add(LinkedArtistsHelper.addToDocAndConstructList(of, doc, artistRelations.get(id),
                    WorkIndexField.ARTIST_ID,
                    WorkIndexField.ARTIST));
        }

        if (recordingRelations.containsKey(id)) {
            work.getRelationList().add(LinkedRecordingsHelper.addToDocAndConstructList(of, doc, recordingRelations.get(id),
                    WorkIndexField.RECORDING_ID,
                    WorkIndexField.RECORDING));
        }

        if (aliases.containsKey(id))
        {
            work.setAliasList(AliasHelper.addAliasesToDocAndConstructAliasList(of, doc, aliases, id, WorkIndexField.ALIAS));
        }

        if (iswcs.containsKey(id)) {
            IswcList iswcList = of.createIswcList();
            for (String iswcCode : iswcs.get(id)) {
                doc.addField(WorkIndexField.ISWC, iswcCode);
                iswcList.getIswc().add(iswcCode);
            }
            work.setIswcList(iswcList);
        }
        else
        {
            doc.addFieldOrNoValue(WorkIndexField.ISWC, null);
        }

        if (tags.containsKey(id))
        {
            work.setTagList(TagHelper.addTagsToDocAndConstructTagList(of, doc, tags, id, WorkIndexField.TAG));
        }

        String store = MMDSerializer.serialize(work);
        doc.addField(WorkIndexField.WORK_STORE, store);

        return doc.getLuceneDocument();
    }

}