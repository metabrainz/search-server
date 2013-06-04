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

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


public class AreaIndex extends DatabaseIndex {

    public static final String INDEX_NAME = "area";

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
        return DatabaseIndex.getAnalyzer(TagIndexField.class);
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
                        "SELECT a.id, a.gid, a.name, a.sort_name, at.name as type " +
                        " FROM area a" +
                        "  LEFT JOIN area_type at ON a.type = at.id " +
                        " WHERE a.id BETWEEN ? AND ? " +
                        " ORDER BY a.id");

        addPreparedStatement("ALIASES",
                "SELECT a.area as area, a.name as alias, a.sort_name as alias_sortname, a.primary_for_locale, a.locale, att.name as type," +
                        "a.begin_date_year, a.begin_date_month, a.begin_date_day, a.end_date_year, a.end_date_month, a.end_date_day" +
                        " FROM area_alias a" +
                        "  LEFT JOIN area_alias_type att on (a.type=att.id)" +
                        " WHERE area BETWEEN ? AND ?" +
                        " ORDER BY area, alias, alias_sortname");

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

        st = getPreparedStatement("AREA");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, aliases));
        }
        rs.close();

    }

    public Document documentFromResultSet(ResultSet rs,
                                          Map<Integer, Set<Alias>> aliases) throws SQLException {
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

        String sortName = rs.getString("sort_name");
        doc.addField(AreaIndexField.SORTNAME,sortName);
        area.setSortName(sortName);

        String type = rs.getString("type");
        doc.addFieldOrUnknown(AreaIndexField.TYPE, type);
        if (!Strings.isNullOrEmpty(type)) {
            area.setType(type);
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

        String store = MMDSerializer.serialize(areaList);
        doc.addField(AreaIndexField.AREA_STORE, store);
        return doc.getLuceneDocument();
    }

}