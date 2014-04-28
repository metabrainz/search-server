/*
 * MusicBrainz Search Server
 * Copyright (C) 2009  Lukas Lalinsky

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
import org.apache.lucene.search.similarities.Similarity;
import org.musicbrainz.mmd2.Alias;
import org.musicbrainz.mmd2.AliasList;
import org.musicbrainz.mmd2.ObjectFactory;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.MusicbrainzSimilarity;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class SeriesIndex extends DatabaseIndex {

    public static final String INDEX_NAME = "series";


    public SeriesIndex(Connection dbConnection) {
        super(dbConnection);
    }

    public SeriesIndex() {
    }


    public String getName() {
        return SeriesIndex.INDEX_NAME;
    }

    public Analyzer getAnalyzer() {
        return DatabaseIndex.getAnalyzer(SeriesIndexField.class);
    }

	@Override
	public IndexField getIdentifierField() {
		return SeriesIndexField.ID;
	}
    
    public int getMaxId() throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM series");
        rs.next();
        return rs.getInt(1);
    }

    public int getNoOfRows(int maxId) throws SQLException {
    	PreparedStatement st = dbConnection.prepareStatement(
    		"SELECT count(*) FROM series WHERE id <= ?");
    	st.setInt(1, maxId);
    	ResultSet rs = st.executeQuery();
        rs.next();
        return rs.getInt(1);
    }

    @Override
    public Similarity getSimilarity()
    {
        return new MusicbrainzSimilarity();
    }

    @Override
    public void init(IndexWriter indexWriter, boolean isUpdater) throws SQLException {

        addPreparedStatement("ALIASES",
                "SELECT a.series as series, a.name as alias, a.sort_name as alias_sortname, a.primary_for_locale, a.locale, att.name as type," +
                        "a.begin_date_year, a.begin_date_month, a.begin_date_day, a.end_date_year, a.end_date_month, a.end_date_day" +
                        " FROM series_alias a" +
                        "  LEFT JOIN series_alias_type att on (a.type=att.id)" +
                        " WHERE series BETWEEN ? AND ?" +
                        " ORDER BY series, alias, alias_sortname");


        addPreparedStatement("SERIES",
                "SELECT series.id, series.gid, series.name as name," +
                "  series_type.name as type, " +
                "  series.comment,  " +
                "  lat.name as ordering_attribute" +
                " FROM series " +
                "  INNER JOIN link_attribute_type lat on series.ordering_attribute=lat.id" +
                "  LEFT JOIN series_type ON series.type = series_type.id " +
                " WHERE series.id BETWEEN ? AND ?");

    }



    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {

        ObjectFactory of = new ObjectFactory();

        // Get series aliases
        //Aliases
        Map<Integer, Set<Alias>> aliases = new HashMap<Integer, Set<Alias>>();
        PreparedStatement  st = getPreparedStatement("ALIASES");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int artistId = rs.getInt("series");
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

        // Get series
        st = getPreparedStatement("SERIES");
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
        //Series series = of.createSeries();

        int seriesId = rs.getInt("id");
        doc.addField(SeriesIndexField.ID, seriesId);

        String seriesGuid = rs.getString("gid");
        doc.addField(SeriesIndexField.SERIES_ID, seriesGuid);
        //series.setId(seriesGuid);

        String name=rs.getString("name");
        doc.addField(SeriesIndexField.SERIES,name );
        //series.setName(name);

        String type = rs.getString("type");
        doc.addFieldOrUnknown(SeriesIndexField.TYPE, type);
        if (!Strings.isNullOrEmpty(type)) {
       //     series.setType(type);
        }

        String comment = rs.getString("comment");
        doc.addFieldOrNoValue(SeriesIndexField.COMMENT, comment);
        if (!Strings.isNullOrEmpty(comment)) {
       //     series.setDisambiguation(comment);
        }

        String orderAttr = rs.getString("ordering_attribute");
        doc.addFieldOrNoValue(SeriesIndexField.ORDERING_ATTRIBUTE, orderAttr);

        if (aliases.containsKey(seriesId)) {
            AliasList aliasList = of.createAliasList();
            for (Alias nextAlias : aliases.get(seriesId)) {
                doc.addField(SeriesIndexField.ALIAS, nextAlias.getContent());
                if(!Strings.isNullOrEmpty(nextAlias.getSortName())) {
                    if(!nextAlias.getSortName().equals(nextAlias.getContent())) {
                        doc.addField(SeriesIndexField.ALIAS, nextAlias.getSortName());
                    }
                }
                aliasList.getAlias().add(nextAlias);
            }
      //      series.setAliasList(aliasList);
        }

        //String store = MMDSerializer.serialize(series);
        //doc.addField(SeriesIndexField.INSTRUMENT_STORE, store);
        return doc.getLuceneDocument();
    }

}
