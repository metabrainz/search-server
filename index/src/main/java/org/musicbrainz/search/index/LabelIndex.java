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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.musicbrainz.mmd2.Tag;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.MusicbrainzSimilarity;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LabelIndex extends DatabaseIndex {

    public static final String INDEX_NAME = "label";

    private static final String DELETED_LABEL_MBID = "f43e252d-9ebf-4e8e-bba8-36d080756cc1";

    public LabelIndex(Connection dbConnection) {
        super(dbConnection);
    }

    public LabelIndex() {
    }


    public String getName() {
        return LabelIndex.INDEX_NAME;
    }

    public Analyzer getAnalyzer() {
        return DatabaseIndex.getAnalyzer(LabelIndexField.class);
    }

	@Override
	public IndexField getIdentifierField() {
		return LabelIndexField.ID;
	}
    
    public int getMaxId() throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM label");
        rs.next();
        return rs.getInt(1);
    }

    public int getNoOfRows(int maxId) throws SQLException {
    	PreparedStatement st = dbConnection.prepareStatement(
    		"SELECT count(*) FROM label WHERE id <= ? AND gid <> ?::uuid");
    	st.setInt(1, maxId);
    	st.setString(2, DELETED_LABEL_MBID);
    	ResultSet rs = st.executeQuery();
        rs.next();
        return rs.getInt(1);
    }

    @Override
    public void init(IndexWriter indexWriter, boolean isUpdater) throws SQLException {

        indexWriter.setSimilarity(new MusicbrainzSimilarity());


        addPreparedStatement("TAGS",
                "SELECT label_tag.label, tag.name as tag, label_tag.count as count " +
                " FROM label_tag " +
                "  INNER JOIN tag ON tag=id " +
                " WHERE label between ? AND ?");


        addPreparedStatement("ALIASES", 
                "SELECT label_alias.label as label, n.name as alias " +
                " FROM label_alias " +
                "  JOIN label_name n ON (label_alias.name = n.id) " +
                " WHERE label BETWEEN ? AND ?");


        addPreparedStatement("LABELS",
                "SELECT label.id, gid, n0.name as name, n1.name as sort_name, " +
                "  label_type.name as type, begin_date_year, begin_date_month, begin_date_day, " +
                "  end_date_year, end_date_month, end_date_day, " +
                "  comment, label_code, lower(iso_code) as country, ipi_code " +
                " FROM label " +
                "  LEFT JOIN label_name n0 ON label.name = n0.id " +
                "  LEFT JOIN label_name n1 ON label.sort_name = n1.id " +
                "  LEFT JOIN label_type ON label.type = label_type.id " +
                "  LEFT JOIN country ON label.country = country.id " +
                " WHERE label.id BETWEEN ? AND ?");
    }


    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {

        // Get Tags
        PreparedStatement st = getPreparedStatement("TAGS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        Map<Integer,List<Tag>> tags = TagHelper.completeTagsFromDbResults(rs,"label");
        rs.close();

        // Get labels aliases
        Map<Integer, List<String>> aliases = new HashMap<Integer, List<String>>();
        st = getPreparedStatement("ALIASES");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            int labelId = rs.getInt("label");

            List<String> list;
            if (!aliases.containsKey(labelId)) {
                list = new LinkedList<String>();
                aliases.put(labelId, list);
            } else {
                list = aliases.get(labelId);
            }
            list.add(rs.getString("alias"));
        }
        rs.close();

        // Get labels
        st = getPreparedStatement("LABELS");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();

        while (rs.next()) {
            if(rs.getString("gid").equals(DELETED_LABEL_MBID))
            {
                continue;
            }
            indexWriter.addDocument(documentFromResultSet(rs, tags, aliases));
        }
        rs.close();
    }

    public Document documentFromResultSet(ResultSet rs,
                                          Map<Integer,List<Tag>> tags,
                                          Map<Integer, List<String>> aliases) throws SQLException {

        MbDocument doc = new MbDocument();
        int labelId = rs.getInt("id");
        doc.addField(LabelIndexField.LABEL_ID, rs.getString("gid"));
        doc.addField(LabelIndexField.LABEL, rs.getString("name"));
        doc.addField(LabelIndexField.SORTNAME, rs.getString("sort_name"));

        //Allows you to search for labels of Unknown type
        String type = rs.getString("type");
        if (type != null) {
            doc.addField(LabelIndexField.TYPE, type);
        } else {
            doc.addField(LabelIndexField.TYPE, "unknown");
        }

        doc.addNonEmptyField(LabelIndexField.COMMENT, rs.getString("comment"));
        doc.addNonEmptyField(LabelIndexField.COUNTRY, rs.getString("country"));

        doc.addNonEmptyField(LabelIndexField.BEGIN,
                Utils.formatDate(rs.getInt("begin_date_year"), rs.getInt("begin_date_month"), rs.getInt("begin_date_day")));

        doc.addNonEmptyField(LabelIndexField.END,
                Utils.formatDate(rs.getInt("end_date_year"), rs.getInt("end_date_month"), rs.getInt("end_date_day")));

        int labelcode = rs.getInt("label_code");
        if (labelcode > 0) {
            doc.addNumericField(LabelIndexField.CODE, labelcode);
        }

        doc.addNonEmptyField(ArtistIndexField.IPI, rs.getString("ipi_code"));

        if (aliases.containsKey(labelId)) {
            for (String alias : aliases.get(labelId)) {
                doc.addField(LabelIndexField.ALIAS, alias);
            }
        }

        if (tags.containsKey(labelId)) {
            for (Tag tag : tags.get(labelId)) {
                doc.addField(LabelIndexField.TAG, tag.getName());
                doc.addField(LabelIndexField.TAGCOUNT, tag.getCount().toString());
            }
        }

        return doc.getLuceneDocument();
    }

}
