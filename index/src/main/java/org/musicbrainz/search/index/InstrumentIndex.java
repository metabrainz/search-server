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
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.MusicbrainzSimilarity;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class InstrumentIndex extends DatabaseIndex {

    public static final String INDEX_NAME = "instrument";


    public InstrumentIndex(Connection dbConnection) {
        super(dbConnection);
    }

    public InstrumentIndex() {
    }


    public String getName() {
        return InstrumentIndex.INDEX_NAME;
    }

    public Analyzer getAnalyzer() {
        return DatabaseIndex.getAnalyzer(InstrumentIndexField.class);
    }

	@Override
	public IndexField getIdentifierField() {
		return InstrumentIndexField.ID;
	}
    
    public int getMaxId() throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM instrument");
        rs.next();
        return rs.getInt(1);
    }

    public int getNoOfRows(int maxId) throws SQLException {
    	PreparedStatement st = dbConnection.prepareStatement(
    		"SELECT count(*) FROM instrument WHERE id <= ?");
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
                "SELECT a.instrument as instrument, a.name as alias, a.sort_name as alias_sortname, a.primary_for_locale, a.locale, att.name as type," +
                        "a.begin_date_year, a.begin_date_month, a.begin_date_day, a.end_date_year, a.end_date_month, a.end_date_day" +
                        " FROM instrument_alias a" +
                        "  LEFT JOIN instrument_alias_type att on (a.type=att.id)" +
                        " WHERE instrument BETWEEN ? AND ?" +
                        " ORDER BY instrument, alias, alias_sortname");


        addPreparedStatement("INSTRUMENTS",
                "SELECT instrument.id, instrument.gid, instrument.name as name," +
                "  instrument_type.name as type, " +
                "  instrument.comment,  " +
                "  instrument.description " +
                " FROM instrument " +
                "  LEFT JOIN instrument_type ON instrument.type = instrument_type.id " +
                " WHERE instrument.id BETWEEN ? AND ?");

        addPreparedStatement("TAGS",
                "SELECT t1.instrument, t2.name as tag, t1.count as count " +
                        " FROM instrument_tag t1" +
                        "  INNER JOIN tag t2 ON tag=id " +
                        " WHERE t1.instrument between ? AND ?");


    }



    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {

        ObjectFactory of = new ObjectFactory();


        // Get instruments aliases
        //Aliases
        Map<Integer, Set<Alias>> aliases = new HashMap<Integer, Set<Alias>>();
        PreparedStatement  st = getPreparedStatement("ALIASES");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int artistId = rs.getInt("instrument");
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

        // Get Tags
        st = getPreparedStatement("TAGS");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        Map<Integer,List<Tag>> tags = TagHelper.completeTagsFromDbResults(rs,"instrument");
        rs.close();

        // Get instruments
        st = getPreparedStatement("INSTRUMENTS");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();

        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, tags, aliases));
        }
        rs.close();
    }

    public Document documentFromResultSet(ResultSet rs, Map<Integer, List<Tag>> tags, Map<Integer, Set<Alias>> aliases) throws SQLException {

        MbDocument doc = new MbDocument();

        ObjectFactory of = new ObjectFactory();
        Instrument instrument = of.createInstrument();

        int instrumentId = rs.getInt("id");
        doc.addField(InstrumentIndexField.ID, instrumentId);

        String instrumentGuid = rs.getString("gid");
        doc.addField(InstrumentIndexField.INSTRUMENT_ID, instrumentGuid);
        instrument.setId(instrumentGuid);

        String name=rs.getString("name");
        doc.addField(InstrumentIndexField.INSTRUMENT,name );
        instrument.setName(name);

        String type = rs.getString("type");
        doc.addFieldOrUnknown(InstrumentIndexField.TYPE, type);
        if (!Strings.isNullOrEmpty(type)) {
            instrument.setType(type);
        }

        String comment = rs.getString("comment");
        doc.addFieldOrNoValue(InstrumentIndexField.COMMENT, comment);
        if (!Strings.isNullOrEmpty(comment)) {
            instrument.setDisambiguation(comment);
        }

        String description = rs.getString("description");
        doc.addFieldOrNoValue(InstrumentIndexField.DESCRIPTION, description);
        if (!Strings.isNullOrEmpty(description)) {
            instrument.setDescription(description);
        }

        if (aliases.containsKey(instrumentId))
        {
            instrument.setAliasList(AliasHelper.addAliasesToDocAndConstructAliasList(of, doc, aliases, instrumentId, InstrumentIndexField.ALIAS));
        }

        if (tags.containsKey(instrumentId))
        {
            TagList tagList = TagHelper.addTagsToDocAndConstructTagList(of, doc, tags, instrumentId, InstrumentIndexField.TAG);
            //TODO
            //instrument.setTagList(tagList)
        }

        String store = MMDSerializer.serialize(instrument);
        doc.addField(InstrumentIndexField.INSTRUMENT_STORE, store);
        return doc.getLuceneDocument();
    }

}
