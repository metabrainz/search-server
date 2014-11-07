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

        addPreparedStatement("SERIES",
                "SELECT series.id, series.gid, series.name as name," +
                "  series_type.name as type, " +
                "  series.comment,  " +
                "  lat.name as ordering_attribute" +
                " FROM series " +
                "  INNER JOIN link_attribute_type lat on series.ordering_attribute=lat.id" +
                "  LEFT JOIN series_type ON series.type = series_type.id " +
                " WHERE series.id BETWEEN ? AND ?");

        addPreparedStatement("TAGS", TagHelper.constructTagQuery("series_tag", "series"));
        addPreparedStatement("ALIASES", AliasHelper.constructAliasQuery("series"));
    }



    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {
        Map<Integer, Set<Alias>> aliases = AliasHelper.completeFromDbResults(min, max, getPreparedStatement("ALIASES"));
        Map<Integer,List<Tag>> tags = TagHelper.loadTags(min, max, getPreparedStatement("TAGS"), "series");

        // Get series
        PreparedStatement st = getPreparedStatement("SERIES");
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
        Series series = of.createSeries();

        int seriesId = rs.getInt("id");
        doc.addField(SeriesIndexField.ID, seriesId);

        String seriesGuid = rs.getString("gid");
        doc.addField(SeriesIndexField.SERIES_ID, seriesGuid);
        series.setId(seriesGuid);

        String name=rs.getString("name");
        doc.addField(SeriesIndexField.SERIES,name );
        series.setName(name);

        String type = rs.getString("type");
        doc.addFieldOrUnknown(SeriesIndexField.TYPE, type);
        if (!Strings.isNullOrEmpty(type)) {
            series.setType(type);
        }

        String comment = rs.getString("comment");
        doc.addFieldOrNoValue(SeriesIndexField.COMMENT, comment);
        if (!Strings.isNullOrEmpty(comment)) {
            series.setDisambiguation(comment);
        }

        String orderAttr = rs.getString("ordering_attribute");
        doc.addFieldOrNoValue(SeriesIndexField.ORDERING_ATTRIBUTE, orderAttr);

        if (aliases.containsKey(seriesId))
        {
            series.setAliasList(AliasHelper.addAliasesToDocAndConstructAliasList(of, doc, aliases, seriesId, EventIndexField.ALIAS));
        }

        if (tags.containsKey(seriesId))
        {
            TagList tagList = TagHelper.addTagsToDocAndConstructTagList(of, doc, tags, seriesId, SeriesIndexField.TAG);
            series.setTagList(tagList);
        }


        String store = MMDSerializer.serialize(series);
        doc.addField(SeriesIndexField.SERIES_STORE, store);
        return doc.getLuceneDocument();
    }

}
