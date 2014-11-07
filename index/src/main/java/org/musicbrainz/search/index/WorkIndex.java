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

        addPreparedStatement("RECORDINGS",
                " SELECT aw.id as awid, l.id as lid, w.id as wid, w.gid, a.gid as aid, a.name as recording_name, " +
                        " lt.name as link, lat.name as attribute" +
                        " FROM l_recording_work aw" +
                        " INNER JOIN recording a ON a.id    = aw.entity0" +
                        " INNER JOIN work   w ON w.id     = aw.entity1" +
                        " INNER JOIN link l ON aw.link = l.id " +
                        " INNER JOIN link_type lt on l.link_type=lt.id" +
                        " LEFT JOIN  link_attribute la on la.link=l.id" +
                        " LEFT JOIN  link_attribute_type lat on la.attribute_type=lat.id" +
                        " WHERE w.id BETWEEN ? AND ?  "  +
                        " ORDER BY aw.id");

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
     * Load Recording Relations
     *
     * @param min
     * @param max
     * @return
     * @throws SQLException
     * @throws IOException
     */
    private ArrayListMultimap<Integer, Relation> loadRecordingRelations(int min, int max) throws SQLException, IOException {
        ObjectFactory of = new ObjectFactory();
        ArrayListMultimap<Integer, Relation> recordings = ArrayListMultimap.create();
        PreparedStatement st  = getPreparedStatement("RECORDINGS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        int lastLinkId=-1;
        Relation lastRelation = null;
        while (rs.next()) {
            int linkId = rs.getInt("awid");

            //If have another attribute for the same relation
            if(linkId==lastLinkId) {
                Relation.AttributeList.Attribute attribute = of.createRelationAttributeListAttribute();
                attribute.setContent(rs.getString("attribute"));
                Relation.AttributeList attributeList=lastRelation.getAttributeList();
                attributeList.getAttribute().add(attribute);
            }
            //New relation (may or may not be new work but doesn't matter)
            else {
                int workId = rs.getInt("wid");

                Relation relation = of.createRelation();

                Recording recording = of.createRecording();
                recording.setId(rs.getString("aid"));
                recording.setTitle(rs.getString("recording_name"));

                relation.setRecording(recording);
                relation.setType(rs.getString("link"));
                relation.setDirection(DefDirection.BACKWARD);

                //Each relation may contain attributes if it does needs attribute list
                String attributeValue = rs.getString("attribute");
                if(!Strings.isNullOrEmpty(attributeValue))
                {
                    Relation.AttributeList attributeList = of.createRelationAttributeList();
                    relation.setAttributeList(attributeList);
                    Relation.AttributeList.Attribute attribute = new ObjectFactory().createRelationAttributeListAttribute();
                    attribute.setContent(attributeValue);
                    attributeList.getAttribute().add(attribute);
                }
                //Add relation
                recordings.put(workId, relation);

                lastRelation=relation;
                lastLinkId=linkId;
            }
        }
        rs.close();
        return recordings;
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
        ArrayListMultimap<Integer, Relation> recordingRelations = loadRecordingRelations(min, max);
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
            List<Relation> rl = artistRelations.get(id);
            RelationList relationList = of.createRelationList();
            relationList.setTargetType(RelationTypes.ARTIST_RELATION_TYPE);
            work.getRelationList().add(relationList);
            for (Relation r : rl) {
                relationList.getRelation().add(r);
                doc.addField(WorkIndexField.ARTIST_ID, r.getArtist().getId());
                doc.addField(WorkIndexField.ARTIST, r.getArtist().getName());
            }
        }


        if (recordingRelations.containsKey(id)) {
            List<Relation> rl = recordingRelations.get(id);
            RelationList relationList = of.createRelationList();
            relationList.setTargetType(RelationTypes.RECORDING_RELATION_TYPE);
            work.getRelationList().add(relationList);
            for (Relation r : rl) {
                relationList.getRelation().add(r);
                doc.addField(WorkIndexField.RECORDING_ID, r.getRecording().getId());
                doc.addField(WorkIndexField.RECORDING, r.getRecording().getTitle());
            }
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

        if (tags.containsKey(id)) {
            TagList tagList = of.createTagList();
            for (Tag nextTag : tags.get(id)) {
                Tag tag = of.createTag();
                doc.addField(WorkIndexField.TAG, nextTag.getName());
                tag.setName(nextTag.getName());
                tag.setCount(new BigInteger(nextTag.getCount().toString()));
                tagList.getTag().add(tag);
            }
            work.setTagList(tagList);
        }

        String store = MMDSerializer.serialize(work);
        doc.addField(WorkIndexField.WORK_STORE, store);

        return doc.getLuceneDocument();
    }

}