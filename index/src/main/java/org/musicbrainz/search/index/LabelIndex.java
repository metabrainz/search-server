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

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.Analyzer;
import org.musicbrainz.mmd2.Tag;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.MusicbrainzSimilarity;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.sql.*;

public class LabelIndex extends DatabaseIndex {


    private Pattern stripLabelCodeOfLeadingZeroes;

    public LabelIndex(Connection dbConnection) {
        super(dbConnection);
        stripLabelCodeOfLeadingZeroes = Pattern.compile("^0+");
    }

    public LabelIndex() {
    }


    public String getName() {
        return "label";
    }

    public Analyzer getAnalyzer() {
        return new PerFieldEntityAnalyzer(LabelIndexField.class);
    }

    public int getMaxId() throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM label");
        rs.next();
        return rs.getInt(1);
    }

    public int getNoOfRows(int maxId) throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT count(*) FROM label WHERE id<=" + maxId);
        rs.next();
        return rs.getInt(1);
    }

    @Override
    public void init(IndexWriter indexWriter) throws SQLException {

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
                "SELECT label.id, gid, n0.name as name, n1.name as sortname, " +
                "  label_type.name as type, begindate_year, begindate_month, begindate_day, " +
                "  enddate_year, enddate_month, enddate_day, " +
                "  comment, labelcode, lower(isocode) as country " +
                " FROM label " +
                "  LEFT JOIN label_name n0 ON label.name = n0.id " +
                "  LEFT JOIN label_name n1 ON label.sortname = n1.id " +
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


        // Get labels
        st = getPreparedStatement("LABELS");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();

        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, tags, aliases));
        }
    }

    public Document documentFromResultSet(ResultSet rs,
                                          Map<Integer,List<Tag>> tags,
                                          Map<Integer, List<String>> aliases) throws SQLException {

        MbDocument doc = new MbDocument();
        int labelId = rs.getInt("id");
        doc.addField(LabelIndexField.LABEL_ID, rs.getString("gid"));
        doc.addField(LabelIndexField.LABEL, rs.getString("name"));
        doc.addField(LabelIndexField.SORTNAME, rs.getString("sortname"));

        //Allows you to search for labels of Unknown type
        String type = rs.getString("type");
        if (type != null) {
            doc.addField(LabelIndexField.TYPE, type);
        } else {
            doc.addField(LabelIndexField.TYPE, LabelType.UNKNOWN.getName());
        }

        doc.addNonEmptyField(LabelIndexField.COMMENT, rs.getString("comment"));
        doc.addNonEmptyField(LabelIndexField.COUNTRY, rs.getString("country"));

        doc.addNonEmptyField(LabelIndexField.BEGIN,
                Utils.formatDate(rs.getInt("begindate_year"), rs.getInt("begindate_month"), rs.getInt("begindate_day")));

        doc.addNonEmptyField(LabelIndexField.END,
                Utils.formatDate(rs.getInt("enddate_year"), rs.getInt("enddate_month"), rs.getInt("enddate_day")));

        String labelcode = rs.getString("labelcode");
        if (labelcode != null && !labelcode.isEmpty()) {
            Matcher m = stripLabelCodeOfLeadingZeroes.matcher(labelcode);
            doc.addField(LabelIndexField.CODE, m.replaceFirst(""));
        }

        if (aliases.containsKey(labelId)) {
            for (String alias : aliases.get(labelId)) {
                doc.addField(LabelIndexField.ALIAS, alias);
            }
        }

        if (tags.containsKey(labelId)) {
            for (Tag tag : tags.get(labelId)) {
                doc.addField(LabelIndexField.TAG, tag.getContent());
                doc.addField(LabelIndexField.TAGCOUNT, tag.getCount().toString());
            }
        }

        return doc.getLuceneDocument();
    }

}
