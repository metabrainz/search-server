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
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.MbDocument;

import java.awt.geom.Area;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


public class AreaIndex extends DatabaseIndex {

    public static final String INDEX_NAME = "area";

    private static final String AREA_RELATION_TYPE = "area";

    public AreaIndex(Connection dbConnection) {
        super(dbConnection);
    }

    public AreaIndex() { }

    public String getName() {
        return AreaIndex.INDEX_NAME;
    }

	@Override
	public IndexField getIdentifierField() {
		return AreaIndexField.ID;
	}
	
    public Analyzer getAnalyzer() {
        return DatabaseIndex.getAnalyzer(AreaIndexField.class);
    }

    public int getMaxId() throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM area");
        rs.next();
        return rs.getInt(1);
    }

    public int getNoOfRows(int maxId) throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT count(*) FROM area WHERE id<="+maxId);
        rs.next();
        return rs.getInt(1);
    }

    @Override
    public void init(IndexWriter indexWriter, boolean isUpdater) throws SQLException {


        addPreparedStatement("AREA",
                        "SELECT a.id, a.gid, a.name, a.comment as comment, at.name as type, " +
                        "   begin_date_year, begin_date_month, begin_date_day, " +
                        "  end_date_year, end_date_month, end_date_day, ended" +
                        " FROM area a" +
                        "  LEFT JOIN area_type at ON a.type = at.id " +
                        " WHERE a.id BETWEEN ? AND ? " +
                        " ORDER BY a.id");

        addPreparedStatement("AREA_PARENT",
                "SELECT a2.id as areaid, a.id, a.gid, a.name, a.comment as comment, at.name as type, " +
                        "   a.begin_date_year, a.begin_date_month, a.begin_date_day, " +
                        "  a.end_date_year, a.end_date_month, a.end_date_day, a.ended, lt.name as link, lt.gid as linkid" +
                        " FROM area a" +
                        " INNER JOIN l_area_area laa on a.id=laa.entity0" +
                        " INNER JOIN area a2         on a2.id=laa.entity1" +
                        " INNER JOIN link l          ON laa.link   = l.id " +
                        " INNER JOIN link_type lt    ON l.link_type=lt.id" +
                        " LEFT JOIN area_type at ON a.type = at.id " +
                        " WHERE a2.id BETWEEN ? AND ? " +
                        " ORDER BY a2.id");

        addPreparedStatement("ALIASES",
                "SELECT a.area as area, a.name as alias, a.sort_name as alias_sortname, a.primary_for_locale, a.locale, att.name as type," +
                        "a.begin_date_year, a.begin_date_month, a.begin_date_day, a.end_date_year, a.end_date_month, a.end_date_day" +
                        " FROM area_alias a" +
                        "  LEFT JOIN area_alias_type att on (a.type=att.id)" +
                        " WHERE area BETWEEN ? AND ?" +
                        " ORDER BY area, alias, alias_sortname");

        addPreparedStatement("ISO1",
                "SELECT area, code from iso_3166_1 WHERE area BETWEEN ? AND ? ORDER BY area, code");

        addPreparedStatement("ISO2",
                "SELECT area, code from iso_3166_2 WHERE area BETWEEN ? AND ? ORDER BY area, code");

        addPreparedStatement("ISO3",
                "SELECT area, code from iso_3166_3 WHERE area BETWEEN ? AND ? ORDER BY area, code");
    }


    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {

        ObjectFactory of = new ObjectFactory();

        // Get area aliases
        Map<Integer, Set<Alias>> aliases = new HashMap<Integer, Set<Alias>>();
        PreparedStatement st = getPreparedStatement("ALIASES");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int areaId = rs.getInt("area");
            Set<Alias> list;
            if (!aliases.containsKey(areaId)) {
                list = new LinkedHashSet<Alias>();
                aliases.put(areaId, list);
            } else {
                list = aliases.get(areaId);
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

        //Iso1Code
        Map<Integer, Iso31661CodeList> iso1 = new HashMap<Integer, Iso31661CodeList>();
        st = getPreparedStatement("ISO1");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            int areaId = rs.getInt("area");
            Iso31661CodeList iso1List;
            if(!iso1.containsKey(areaId)) {
                iso1List = of.createIso31661CodeList();
                iso1.put(areaId, iso1List);
            }
            else {
                iso1List = iso1.get(areaId);
            }
            iso1List.getIso31661Code().add(rs.getString("code"));
        }

        //Iso2Code
        Map<Integer, Iso31662CodeList> iso2 = new HashMap<Integer, Iso31662CodeList>();
        st = getPreparedStatement("ISO2");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            int areaId = rs.getInt("area");
            Iso31662CodeList iso2List;
            if(!iso2.containsKey(areaId)) {
                iso2List = of.createIso31662CodeList();
                iso2.put(areaId, iso2List);
            }
            else {
                iso2List = iso2.get(areaId);
            }
            iso2List.getIso31662Code().add(rs.getString("code"));
        }

        //Iso3Code
        Map<Integer, Iso31663CodeList> iso3 = new HashMap<Integer, Iso31663CodeList>();
        st = getPreparedStatement("ISO3");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            int areaId = rs.getInt("area");
            Iso31663CodeList iso3List;
            if(!iso3.containsKey(areaId)) {
                iso3List = of.createIso31663CodeList();
                iso3.put(areaId, iso3List);
            }
            else {
                iso3List = iso3.get(areaId);
            }
            iso3List.getIso31663Code().add(rs.getString("code"));
        }

        //Linked area id to a relation containing the parent (if has one)
        Map<Integer,RelationList> areaParent = new HashMap<Integer, RelationList>();
        st = getPreparedStatement("AREA_PARENT");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            int areaId = rs.getInt("areaid");

            DefAreaElementInner area = of.createDefAreaElementInner();
            String guid = rs.getString("gid");
            area.setId(guid);

            String name = rs.getString("name");
            area.setName(name);

            area.setSortName(name);

            String comment = rs.getString("comment");
            if (!Strings.isNullOrEmpty(comment)) {
                area.setDisambiguation(comment);
            }

            String type = rs.getString("type");
            if (!Strings.isNullOrEmpty(type)) {
                area.setType(type);
            }

            boolean ended = rs.getBoolean("ended");

            String begin = Utils.formatDate(rs.getInt("begin_date_year"), rs.getInt("begin_date_month"), rs.getInt("begin_date_day"));
            String end = Utils.formatDate(rs.getInt("end_date_year"), rs.getInt("end_date_month"), rs.getInt("end_date_day"));
            LifeSpan lifespan = of.createLifeSpan();
            area.setLifeSpan(lifespan);
            if(!Strings.isNullOrEmpty(begin)) {
                lifespan.setBegin(begin);
            }
            if(!Strings.isNullOrEmpty(end)) {
                lifespan.setEnd(end);
            }
            lifespan.setEnded(Boolean.toString(ended));

            RelationList rl   = of.createRelationList();
            rl.setTargetType(AREA_RELATION_TYPE);
            Relation relation = of.createRelation();
            relation.setArea(area);
            Target target = of.createTarget();
            target.setValue(area.getId());
            relation.setTarget(target);
            relation.setType(rs.getString("link"));
            relation.setTypeId(rs.getString("linkid"));
            relation.setDirection(DefDirection.BACKWARD);
            rl.getRelation().add(relation);
            areaParent.put(areaId, rl);
        }
        st = getPreparedStatement("AREA");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, areaParent, aliases, iso1, iso2, iso3));
        }
        rs.close();

    }


    public Document documentFromResultSet(ResultSet rs,
                                          Map<Integer,RelationList> areaParents,
                                          Map<Integer, Set<Alias>> aliases,
                                          Map<Integer, Iso31661CodeList> iso1,
                                          Map<Integer, Iso31662CodeList> iso2,
                                          Map<Integer, Iso31663CodeList> iso3) throws SQLException {
        MbDocument doc = new MbDocument();

        ObjectFactory of = new ObjectFactory();
        //TODO DefAreaElementInner not defined as @Xmlrootelement so we have to wrap in list
        AreaList areaList = of.createAreaList();
        DefAreaElementInner area = of.createDefAreaElementInner();
        areaList.getArea().add(area);

        int areaId = rs.getInt("id");
        doc.addField(AreaIndexField.ID, areaId);

        String guid = rs.getString("gid");
        doc.addField(AreaIndexField.AREA_ID, guid);
        area.setId(guid);

        String name = rs.getString("name");
        doc.addField(AreaIndexField.AREA, name);
        area.setName(name);

        doc.addField(AreaIndexField.SORTNAME,name);
        area.setSortName(name);

        String comment = rs.getString("comment");
        doc.addFieldOrNoValue(AreaIndexField.COMMENT, comment);
        if (!Strings.isNullOrEmpty(comment)) {
            area.setDisambiguation(comment);
        }

        String type = rs.getString("type");
        doc.addFieldOrUnknown(AreaIndexField.TYPE, type);
        if (!Strings.isNullOrEmpty(type)) {
            area.setType(type);
        }

        boolean ended = rs.getBoolean("ended");
        doc.addFieldOrUnknown(ArtistIndexField.ENDED, Boolean.toString(ended));

        String begin = Utils.formatDate(rs.getInt("begin_date_year"), rs.getInt("begin_date_month"), rs.getInt("begin_date_day"));
        doc.addNonEmptyField(ArtistIndexField.BEGIN, begin);

        String end = Utils.formatDate(rs.getInt("end_date_year"), rs.getInt("end_date_month"), rs.getInt("end_date_day"));
        doc.addNonEmptyField(ArtistIndexField.END, end);

        LifeSpan lifespan = of.createLifeSpan();
        area.setLifeSpan(lifespan);
        if(!Strings.isNullOrEmpty(begin)) {
            lifespan.setBegin(begin);
        }
        if(!Strings.isNullOrEmpty(end)) {
            lifespan.setEnd(end);
        }
        lifespan.setEnded(Boolean.toString(ended));

        if(areaParents.containsKey(areaId)) {
            RelationList rl = areaParents.get(areaId);
            area.getRelationList().add(rl);
        }

        if (aliases.containsKey(areaId)) {
            AliasList aliasList = of.createAliasList();
            for (Alias nextAlias : aliases.get(areaId)) {
                doc.addField(AreaIndexField.ALIAS, nextAlias.getContent());
                if(!nextAlias.getSortName().equals(nextAlias.getContent())) {
                    doc.addField(AreaIndexField.ALIAS, nextAlias.getSortName());
                }
                aliasList.getAlias().add(nextAlias);
            }
            area.setAliasList(aliasList);
        }

        if(iso1.containsKey(areaId)) {
            area.setIso31661CodeList(iso1.get(areaId));
            for(String iso:iso1.get(areaId).getIso31661Code()) {
                doc.addField(AreaIndexField.ISO, iso);
                doc.addField(AreaIndexField.ISO1, iso);
            }

        }

        if(iso2.containsKey(areaId)) {
            area.setIso31662CodeList(iso2.get(areaId));
            for(String iso:iso2.get(areaId).getIso31662Code()) {
                doc.addField(AreaIndexField.ISO, iso);
                doc.addField(AreaIndexField.ISO2, iso);
            }
        }

        if(iso3.containsKey(areaId)) {
            area.setIso31663CodeList(iso3.get(areaId));
            for(String iso:iso3.get(areaId).getIso31663Code()) {
                doc.addField(AreaIndexField.ISO, iso);
                doc.addField(AreaIndexField.ISO3, iso);
            }
        }

        String store = MMDSerializer.serialize(areaList);
        doc.addField(AreaIndexField.AREA_STORE, store);

        AreaBoostDoc.boost(area.getType(), doc.getLuceneDocument());
        return doc.getLuceneDocument();
    }

}