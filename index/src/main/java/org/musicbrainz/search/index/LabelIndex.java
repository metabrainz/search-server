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

import java.sql.*;

public class LabelIndex extends Index {

    private Pattern stripLabelCodeOfLeadingZeroes;

    public LabelIndex(Connection dbConnection) {
        super(dbConnection);
        stripLabelCodeOfLeadingZeroes = Pattern.compile("^0+");


    }

    public String getName() {
        return "label";
    }

    public int getMaxId() throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM label");
        rs.next();
        return rs.getInt(1);
    }

    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {
        Map<Integer, List<String>> aliases = new HashMap<Integer, List<String>>();
        PreparedStatement st = dbConnection.prepareStatement("SELECT ref as label, name as alias FROM labelalias WHERE ref BETWEEN ? AND ?");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
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
        st.close();
        st = dbConnection.prepareStatement(
                "SELECT label.id, gid, label.name, sortname, type, begindate, enddate, resolution, labelcode, lower(isocode) as country " +
                        "FROM label LEFT JOIN country ON label.country=country.id WHERE label.id BETWEEN ? AND ?");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, aliases));

        }
        st.close();
    }

    public Document documentFromResultSet(ResultSet rs, Map<Integer, List<String>> aliases) throws SQLException {

        Document doc = new Document();
        int labelId = rs.getInt("id");
        addFieldToDocument(doc, LabelIndexField.LABEL_ID, rs.getString("gid"));
        addFieldToDocument(doc, LabelIndexField.LABEL, rs.getString("name"));
        addFieldToDocument(doc, LabelIndexField.SORTNAME, rs.getString("sortname"));
        addFieldToDocument(doc, LabelIndexField.TYPE, LabelType.getByDbId(rs.getInt("type")).getName());

        String begin = rs.getString("begindate");
        if (begin != null && !begin.isEmpty()) {
            addFieldToDocument(doc, LabelIndexField.BEGIN, normalizeDate(begin));
        }

        String end = rs.getString("enddate");
        if (end != null && !end.isEmpty()) {
            addFieldToDocument(doc, LabelIndexField.END, normalizeDate(end));
        }

        String comment = rs.getString("resolution");
        if (comment != null && !comment.isEmpty()) {
            addFieldToDocument(doc, LabelIndexField.COMMENT, comment);
        }

        String labelcode = rs.getString("labelcode");
        if (labelcode != null && !labelcode.isEmpty()) {
            Matcher m = stripLabelCodeOfLeadingZeroes.matcher(labelcode);
            addFieldToDocument(doc, LabelIndexField.CODE, m.replaceFirst(""));
        }

        String country = rs.getString("country");
        if (country != null && !country.isEmpty()) {
            addFieldToDocument(doc, LabelIndexField.COUNTRY, country);
        }

        if (aliases.containsKey(labelId)) {
            for (String alias : aliases.get(labelId)) {
                addFieldToDocument(doc, LabelIndexField.ALIAS, alias);
            }
        }
        return doc;
    }

}
