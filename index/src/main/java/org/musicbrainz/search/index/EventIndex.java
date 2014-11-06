/* Copyright (c) 2013 Paul Taylor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the MusicBrainz project nor the names of the
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.musicbrainz.search.index;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.MbDocument;


import java.io.IOException;
import java.sql.*;
import java.util.*;


public class EventIndex extends DatabaseIndex {

    public static final String INDEX_NAME = "event";

    public EventIndex(Connection dbConnection) {
        super(dbConnection);
    }

    public EventIndex() { }

    public String getName() {
        return EventIndex.INDEX_NAME;
    }

	@Override
	public IndexField getIdentifierField() {
		return EventIndexField.ID;
	}
	
    public Analyzer getAnalyzer() {
        return DatabaseIndex.getAnalyzer(EventIndexField.class);
    }

    public int getMaxId() throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM event");
        rs.next();
        return rs.getInt(1);
    }

    public int getNoOfRows(int maxId) throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT count(*) FROM event WHERE id<="+maxId);
        rs.next();
        return rs.getInt(1);
    }

    @Override
    public void init(IndexWriter indexWriter, boolean isUpdater) throws SQLException {


        addPreparedStatement("EVENT", "SELECT  e.id, e.gid, e.name, e.time, et.name as type, " +
                        "  e.begin_date_year, e.begin_date_month, e.begin_date_day, " +
                        "  e.end_date_year, e.end_date_month, e.end_date_day, e.ended, e.comment " +
                        " FROM event e" +
                        "  LEFT JOIN event_type et ON e.type = et.id " +
                        " WHERE e.id BETWEEN ? AND ? " +
                        " ORDER BY e.id"
        );

        addPreparedStatement("ALIASES",
                "SELECT e.event as event, e.name as alias, e.sort_name as alias_sortname, e.primary_for_locale, e.locale, att.name as type," +
                        "e.begin_date_year, e.begin_date_month, e.begin_date_day, e.end_date_year, e.end_date_month, e.end_date_day" +
                        " FROM event_alias e" +
                        "  LEFT JOIN event_alias_type att on (e.type=att.id)" +
                        " WHERE event BETWEEN ? AND ?" +
                        " ORDER BY event, alias, alias_sortname");

        addPreparedStatement("TAGS",
                "SELECT t1.event, t2.name as tag, t1.count as count " +
                        " FROM event_tag t1" +
                        "  INNER JOIN tag t2 ON tag=id " +
                        " WHERE t1.event between ? AND ?");


        addPreparedStatement("ARTISTS",
                " SELECT aw.id as awid, l.id as lid, w.id as wid, w.gid, a.gid as aid, a.name as artist_name, a.sort_name as artist_sortname," +
                        " lt.name as link, lat.name as attribute" +
                        " FROM l_artist_event aw" +
                        " INNER JOIN artist a ON a.id    = aw.entity0" +
                        " INNER JOIN event   w ON w.id     = aw.entity1" +
                        " INNER JOIN link l ON aw.link = l.id " +
                        " INNER JOIN link_type lt on l.link_type=lt.id" +
                        " LEFT JOIN  link_attribute la on la.link=l.id" +
                        " LEFT JOIN  link_attribute_type lat on la.attribute_type=lat.id" +
                        " WHERE w.id BETWEEN ? AND ?  "  +
                        " ORDER BY aw.id");
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
    private ArrayListMultimap<Integer, Relation> loadArtistRelations(int min, int max) throws SQLException, IOException {
        ObjectFactory of = new ObjectFactory();
        ArrayListMultimap<Integer, Relation> artists = ArrayListMultimap.create();
        PreparedStatement st  = getPreparedStatement("ARTISTS");
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

                Artist artist = of.createArtist();
                artist.setId(rs.getString("aid"));
                artist.setName(rs.getString("artist_name"));
                artist.setSortName(rs.getString("artist_sortname"));

                relation.setArtist(artist);
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
                artists.put(workId, relation);

                lastRelation=relation;
                lastLinkId=linkId;
            }
        }
        rs.close();
        return artists;
    }

    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {

        ObjectFactory of = new ObjectFactory();

        // Get event aliases
        Map<Integer, Set<Alias>> aliases = new HashMap<Integer, Set<Alias>>();
        PreparedStatement st = getPreparedStatement("ALIASES");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int eventId = rs.getInt("event");
            Set<Alias> list;
            if (!aliases.containsKey(eventId)) {
                list = new LinkedHashSet<Alias>();
                aliases.put(eventId, list);
            } else {
                list = aliases.get(eventId);
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

        ArrayListMultimap<Integer, Relation> artistRelations    = loadArtistRelations(min, max);

        // Get Tags
        st = getPreparedStatement("TAGS");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        Map<Integer,List<Tag>> tags = TagHelper.completeTagsFromDbResults(rs,"event");
        rs.close();

        st = getPreparedStatement("EVENT");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, artistRelations, tags, aliases));
        }
        rs.close();

    }

    public Document documentFromResultSet(ResultSet rs,
                                          ArrayListMultimap<Integer, Relation> artistRelations,
                                          Map<Integer,List<Tag>> tags,
                                          Map<Integer, Set<Alias>> aliases) throws SQLException {

        MbDocument doc = new MbDocument();

        ObjectFactory of = new ObjectFactory();
        Event     event = of.createEvent();

        int eventId = rs.getInt("id");
        doc.addField(EventIndexField.ID, eventId);

        String guid = rs.getString("gid");
        doc.addField(EventIndexField.EVENT_ID, guid);
        event.setId(guid);

        String name = rs.getString("name");
        doc.addField(EventIndexField.EVENT, name);
        event.setName(name);

        String comment = rs.getString("comment");
        if (!Strings.isNullOrEmpty(comment)) {
            doc.addField(EventIndexField.COMMENT, comment);
            event.setDisambiguation(comment);
        }

        String type = rs.getString("type");
        doc.addFieldOrUnknown(EventIndexField.TYPE, type);
        if (!Strings.isNullOrEmpty(type)) {
            event.setType(type);
        }

        //Time
        String time = rs.getString("time");
        if(!Strings.isNullOrEmpty(time))
        {
            event.setTime(time);
        }
        boolean ended = rs.getBoolean("ended");
        doc.addFieldOrUnknown(ArtistIndexField.ENDED, Boolean.toString(ended));

        String begin = Utils.formatDate(rs.getInt("begin_date_year"), rs.getInt("begin_date_month"), rs.getInt("begin_date_day"));
        doc.addNonEmptyField(ArtistIndexField.BEGIN, begin);

        String end = Utils.formatDate(rs.getInt("end_date_year"), rs.getInt("end_date_month"), rs.getInt("end_date_day"));
        doc.addNonEmptyField(ArtistIndexField.END, end);

        org.musicbrainz.mmd2.Event.LifeSpan lifespan = of.createEventLifeSpan();
        event.setLifeSpan(lifespan);
        if(!Strings.isNullOrEmpty(begin)) {
            lifespan.setBegin(begin);
        }
        if(!Strings.isNullOrEmpty(end)) {
            lifespan.setEnd(end);
        }


        if (artistRelations.containsKey(eventId)) {
            List<Relation> rl = artistRelations.get(eventId);
            RelationList relationList = of.createRelationList();
            relationList.setTargetType(RelationTypes.ARTIST_RELATION_TYPE);
            event.getRelationList().add(relationList);
            for (Relation r : rl) {
                relationList.getRelation().add(r);
                doc.addField(EventIndexField.ARTIST_ID, r.getArtist().getId());
                doc.addField(EventIndexField.ARTIST, r.getArtist().getName());
            }
        }

        if (aliases.containsKey(eventId))
        {
            event.setAliasList(AliasHelper.addAliasesToDocAndConstructAliasList(of, doc, aliases, eventId, EventIndexField.ALIAS));
        }

        if (tags.containsKey(eventId))
        {
            event.setTagList(TagHelper.addTagsToDocAndConstructTagList(of, doc, tags, eventId, EventIndexField.TAG));
        }

        String store = MMDSerializer.serialize(event);
        doc.addField(EventIndexField.EVENT_STORE, store);
        return doc.getLuceneDocument();
    }

}