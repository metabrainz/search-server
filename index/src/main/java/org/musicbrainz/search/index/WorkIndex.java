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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.MbDocument;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


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
                " SELECT aw.id as awid, l.id as lid, w.id as wid, w.gid, a.gid as aid, an.name as artist_name, sn.name as artist_sortname," +
                        " lt.name as link, lat.name as attribute" +
                        " FROM l_artist_work aw" +
                        " INNER JOIN artist a ON a.id    = aw.entity0" +
                        " INNER JOIN work   w ON w.id     = aw.entity1" +
                        " INNER JOIN artist_name an ON an.id = a.name" +
                        " INNER JOIN artist_name sn ON sn.id = a.sort_name" +
                        " INNER JOIN link l ON aw.link = l.id " +
                        " INNER JOIN link_type lt on l.link_type=lt.id" +
                        " LEFT JOIN  link_attribute la on la.link=l.id" +
                        " LEFT JOIN  link_attribute_type lat on la.attribute_type=lat.id" +
                        " WHERE w.id BETWEEN ? AND ?  "  +
                        " ORDER BY aw.id");

        addPreparedStatement("ALIASES",
                "SELECT work_alias.work as work, n.name as alias " +
                        " FROM work_alias " +
                        "  JOIN work_name n ON (work_alias.name = n.id) " +
                        " WHERE work BETWEEN ? AND ?");

        addPreparedStatement("WORKS",
                "SELECT w.id as wid, w.gid, wn.name as name, wt.name as type, iswc, comment " +
                        " FROM work AS w " +
                        "  LEFT JOIN work_name wn ON w.name = wn.id " +
                        "  LEFT JOIN work_type wt ON w.type = wt.id " +
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
                String attribute = rs.getString("attribute");
                AttributeList attributeList=lastRelation.getAttributeList();
                if(attributeList==null) {
                    attributeList = new ObjectFactory().createAttributeList();
                    lastRelation.setAttributeList(attributeList);
                }
                attributeList.getAttribute().add(attribute);
            }
            else {
                int workId = rs.getInt("wid");

                RelationList list;
                if (!artists.containsKey(workId)) {
                    list = new ObjectFactory().createRelationList();
                    list.setTargetType(ARTIST_RELATION_TYPE);
                    artists.put(workId, list);
                } else {
                    list = artists.get(workId);
                }

                Relation relation = new ObjectFactory().createRelation();
                Artist artist = new ObjectFactory().createArtist();
                artist.setId(rs.getString("aid"));
                artist.setName(rs.getString("artist_name"));
                artist.setSortName(rs.getString("artist_sortname"));
                relation.setArtist(artist);
                relation.setType(rs.getString("link"));
                relation.setDirection(DefDirection.BACKWARD);
                list.getRelation().add(relation);

                String attribute = rs.getString("attribute");
                if(attribute!=null) {
                    AttributeList attributeList=relation.getAttributeList();
                    if(attributeList==null) {
                        attributeList = new ObjectFactory().createAttributeList();
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
    private Map<Integer, List<String>> loadAliases(int min, int max) throws SQLException, IOException {
        Map<Integer, List<String>> aliases = new HashMap<Integer, List<String>>();
        PreparedStatement st = getPreparedStatement("ALIASES");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int workId = rs.getInt("work");

            List<String> list;
            if (!aliases.containsKey(workId)) {
                list = new LinkedList<String>();
                aliases.put(workId, list);
            } else {
                list = aliases.get(workId);
            }
            list.add(rs.getString("alias"));
        }
        rs.close();
        return aliases;
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

        Map<Integer, List<Tag>>    tags = loadTags(min, max);
        Map<Integer, RelationList> artistRelations = loadArtistRelations(min, max);
        Map<Integer, List<String>> aliases = loadAliases(min, max);

        //Works
        PreparedStatement st = getPreparedStatement("WORKS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, tags, artistRelations, aliases));
        }
        rs.close();

    }

    public Document documentFromResultSet(ResultSet rs,
                                          Map<Integer, List<Tag>> tags,
                                          Map<Integer, RelationList> artists,
                                          Map<Integer, List<String>> aliases) throws SQLException {
        MbDocument doc = new MbDocument();
        int id = rs.getInt("wid");
        doc.addField(WorkIndexField.ID, id);
        doc.addField(WorkIndexField.WORK_ID, rs.getString("gid"));
        String name = rs.getString("name");
        doc.addField(WorkIndexField.WORK, name);
        doc.addField(WorkIndexField.WORK_ACCENT, name);

        doc.addFieldOrNoValue(WorkIndexField.TYPE, rs.getString("type"));
        doc.addFieldOrNoValue(WorkIndexField.ISWC, rs.getString("iswc"));
        doc.addFieldOrNoValue(WorkIndexField.COMMENT, rs.getString("comment"));

        if (artists.containsKey(id)) {
            RelationList rl = artists.get(id);
            if (rl.getRelation().size() > 0) {
                doc.addField(WorkIndexField.ARTIST_RELATION, MMDSerializer.serialize(rl));
                for (Relation r : artists.get(id).getRelation()) {
                    doc.addField(WorkIndexField.ARTIST_ID, r.getArtist().getId());
                    doc.addField(WorkIndexField.ARTIST, r.getArtist().getName());
                }
            }
        }

        if (aliases.containsKey(id)) {
            for (String alias : aliases.get(id)) {
                doc.addField(WorkIndexField.ALIAS, alias);
            }
        }

        if (tags.containsKey(id)) {
            for (Tag tag : tags.get(id)) {
                doc.addField(WorkIndexField.TAG, tag.getName());
                doc.addField(WorkIndexField.TAGCOUNT, tag.getCount().toString());
            }
        }
        return doc.getLuceneDocument();
    }

}