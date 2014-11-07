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
import org.musicbrainz.search.helper.AliasHelper;
import org.musicbrainz.search.helper.TagHelper;

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

        addPreparedStatement("INSTRUMENTS",
                "SELECT instrument.id, instrument.gid, instrument.name as name," +
                "  instrument_type.name as type, " +
                "  instrument.comment,  " +
                "  instrument.description " +
                " FROM instrument " +
                "  LEFT JOIN instrument_type ON instrument.type = instrument_type.id " +
                " WHERE instrument.id BETWEEN ? AND ?");

        addPreparedStatement("TAGS", TagHelper.constructTagQuery("instrument_tag", "instrument"));
        addPreparedStatement("ALIASES", AliasHelper.constructAliasQuery("instrument"));


    }



    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {

        ObjectFactory of = new ObjectFactory();

        //Aliases
        Map<Integer, Set<Alias>> aliases = AliasHelper.completeFromDbResults(min, max, getPreparedStatement("ALIASES"));
        Map<Integer,List<Tag>> tags = TagHelper.loadTags(min, max, getPreparedStatement("TAGS"), "instrument");

        // Get instruments
        PreparedStatement st = getPreparedStatement("INSTRUMENTS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
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
            instrument.setTagList(tagList);
        }

        String store = MMDSerializer.serialize(instrument);
        doc.addField(InstrumentIndexField.INSTRUMENT_STORE, store);
        return doc.getLuceneDocument();
    }

}
