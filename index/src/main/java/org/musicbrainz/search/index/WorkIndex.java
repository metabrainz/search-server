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
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.MbDocument;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.*;
import java.util.*;


public class WorkIndex extends DatabaseIndex {

    public static final String INDEX_NAME = "work";
    private static final String ARTIST_RELATION_TYPE = "artist";

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

        addPreparedStatement("TAGS",
                "SELECT work_tag.work, tag.name as tag, work_tag.count as count " +
                        " FROM work_tag " +
                        "  INNER JOIN tag ON tag=id " +
                        " WHERE work between ? AND ?");

        addPreparedStatement("ARTISTS",
                " SELECT aw.id as awid, l.id as lid, w.id as wid, w.gid, a.gid as aid, a.name as artist_name, a.sort_name as artist_sortname," +
                        " lt.name as link, lat.name as attribute" +
                        " FROM l_artist_work aw" +
                        " INNER JOIN artist a ON a.id    = aw.entity0" +
                        " INNER JOIN work   w ON w.id     = aw.entity1" +
                        " INNER JOIN link l ON aw.link = l.id " +
                        " INNER JOIN link_type lt on l.link_type=lt.id" +
                        " LEFT JOIN  link_attribute la on la.link=l.id" +
                        " LEFT JOIN  link_attribute_type lat on la.attribute_type=lat.id" +
                        " WHERE w.id BETWEEN ? AND ?  "  +
                        " ORDER BY aw.id");

        addPreparedStatement("ALIASES",
                "SELECT a.work as work, a.name as alias, a.sort_name as alias_sortname, a.primary_for_locale, a.locale, att.name as type," +
                        "a.begin_date_year, a.begin_date_month, a.begin_date_day, a.end_date_year, a.end_date_month, a.end_date_day" +
                        " FROM work_alias a" +
                        "  LEFT JOIN work_alias_type att on (a.type=att.id)" +
                        " WHERE work BETWEEN ? AND ?" +
                        " ORDER BY work, alias, alias_sortname");

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
     * Load Tags
     *
     * @param min
     * @param max
     * @return
     * @throws SQLException
     * @throws IOException
     */
    private Map<Integer, List<Tag>> loadTags(int min, int max) throws SQLException, IOException {
        // Get Tags
        PreparedStatement st = getPreparedStatement("TAGS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        Map<Integer, List<Tag>> tags = TagHelper.completeTagsFromDbResults(rs, "work");
        rs.close();
        return tags;
    }

    /**
     * Load Artist Relations
     *
     * @param min
     * @param max
     * @return
     * @throws SQLException
     * @throws IOException
     */
    private Map<Integer, RelationList> loadArtistRelations(int min, int max) throws SQLException, IOException {
        ObjectFactory of = new ObjectFactory();
        Map<Integer, RelationList> artists = new HashMap<Integer, RelationList>();
        PreparedStatement st  = getPreparedStatement("ARTISTS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        int lastLinkId=-1;
        Relation lastRelation = null;
        while (rs.next()) {
            int linkId = rs.getInt("awid");

            if(linkId==lastLinkId) {
                Relation.AttributeList.Attribute attribute = of.createRelationAttributeListAttribute();
                attribute.setContent(rs.getString("attribute"));
                Relation.AttributeList attributeList=lastRelation.getAttributeList();
                if(attributeList==null) {
                    attributeList = of.createRelationAttributeList();
                    lastRelation.setAttributeList(attributeList);
                }
                attributeList.getAttribute().add(attribute);
            }
            else {
                int workId = rs.getInt("wid");

                RelationList list;
                if (!artists.containsKey(workId)) {
                    list = of.createRelationList();
                    list.setTargetType(ARTIST_RELATION_TYPE);
                    artists.put(workId, list);
                } else {
                    list = artists.get(workId);
                }

                Relation relation = of.createRelation();
                Artist artist = of.createArtist();
                artist.setId(rs.getString("aid"));
                artist.setName(rs.getString("artist_name"));
                artist.setSortName(rs.getString("artist_sortname"));
                relation.setArtist(artist);
                relation.setType(rs.getString("link"));
                relation.setDirection(DefDirection.BACKWARD);
                list.getRelation().add(relation);
                Relation.AttributeList.Attribute attribute = new ObjectFactory().createRelationAttributeListAttribute();
                attribute.setContent(rs.getString("attribute"));
                if(attribute!=null) {
                    Relation.AttributeList attributeList=relation.getAttributeList();
                    if(attributeList==null) {
                        attributeList = of.createRelationAttributeList();
                        relation.setAttributeList(attributeList);
                    }
                    attributeList.getAttribute().add(attribute);
                }

                lastRelation=relation;
                lastLinkId=linkId;
            }

        }
        rs.close();
        return artists;
    }

    /**
     * Load work aliases
     *
     * @param min
     * @param max
     * @return
     * @throws SQLException
     * @throws IOException
     */
    private  Map<Integer, Set<Alias>> loadAliases(int min, int max) throws SQLException, IOException {

        ObjectFactory of = new ObjectFactory();

        // Get labels aliases
        //Aliases
        Map<Integer, Set<Alias>> aliases = new HashMap<Integer, Set<Alias>>();
        PreparedStatement st = getPreparedStatement("ALIASES");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int artistId = rs.getInt("work");
            Set<Alias> list;
            if (!aliases.containsKey(artistId)) {
                list = new LinkedHashSet<Alias>();
                aliases.put(artistId, list);
            } else {
                list = aliases.get(artistId);
            }
            Alias alias = of.createAlias();
            alias.setContent(rs.getString("alias"));
            alias.setSortName(rs.getString("alias_sortname"));
            boolean isPrimary = rs.getBoolean("primary_for_locale");
            if(isPrimary) {
                alias.setPrimary("primary");
            }
            String locale = rs.getString("locale");
            if(locale!=null) {
                alias.setLocale(locale);
            }
            String type = rs.getString("type");
            if(type!=null) {
                alias.setType(type);
            }

            String begin = Utils.formatDate(rs.getInt("begin_date_year"), rs.getInt("begin_date_month"), rs.getInt("begin_date_day"));
            if(!Strings.isNullOrEmpty(begin))  {
                alias.setBeginDate(begin);
            }

            String end = Utils.formatDate(rs.getInt("end_date_year"), rs.getInt("end_date_month"), rs.getInt("end_date_day"));
            if(!Strings.isNullOrEmpty(end))  {
                alias.setEndDate(end);
            }
            list.add(alias);
        }
        rs.close();
        return aliases;
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

        Map<Integer, List<Tag>>    tags             = loadTags(min, max);
        Map<Integer, RelationList> artistRelations  = loadArtistRelations(min, max);
        Map<Integer, Set<Alias>> aliases            = loadAliases(min, max);
        Map<Integer, List<String>> iswcs            = loadISWCs(min,max);

        //Works
        PreparedStatement st = getPreparedStatement("WORKS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, tags, artistRelations, aliases, iswcs));
        }
        rs.close();

    }

    public Document documentFromResultSet(ResultSet rs,
                                          Map<Integer, List<Tag>> tags,
                                          Map<Integer, RelationList> artists,
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

        if (artists.containsKey(id)) {
            RelationList rl = artists.get(id);
            work.getRelationList().add(rl);
            if (rl.getRelation().size() > 0) {
                for (Relation r : artists.get(id).getRelation()) {
                    doc.addField(WorkIndexField.ARTIST_ID, r.getArtist().getId());
                    doc.addField(WorkIndexField.ARTIST, r.getArtist().getName());
                }
            }
        }

        if (aliases.containsKey(id)) {
            AliasList aliasList = of.createAliasList();
            for (Alias nextAlias : aliases.get(id)) {
                doc.addField(WorkIndexField.ALIAS, nextAlias.getContent());
                if(!Strings.isNullOrEmpty(nextAlias.getSortName())) {
                    if(!nextAlias.getSortName().equals(nextAlias.getContent())) {
                        doc.addField(WorkIndexField.ALIAS, nextAlias.getSortName());
                    }
                }
                aliasList.getAlias().add(nextAlias);
            }
            work.setAliasList(aliasList);
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