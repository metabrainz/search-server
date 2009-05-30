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

package org.musicbrainz.search;

import java.io.*;
import java.util.*;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Document;

import java.sql.*;

public class LabelIndex extends Index {

    public String getName() {
        return "label";
    }

    public int getMaxId(Connection conn) throws SQLException {
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM label");
        rs.next();
        return rs.getInt(1);
    }

    public void indexData(IndexWriter indexWriter, Connection conn, int min, int max) throws SQLException, IOException {
        Map<Integer, List<String>> aliases = new HashMap<Integer, List<String>>();
        PreparedStatement st = conn.prepareStatement("SELECT ref as label, name as alias FROM labelalias WHERE ref BETWEEN ? AND ?");
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
        st = conn.prepareStatement(
                "SELECT label.id, gid, label.name, sortname, type, begindate, enddate, resolution, labelcode, lower(isocode) as country " +
                        "FROM label JOIN country ON label.country=country.id WHERE label.id BETWEEN ? AND ?");
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
        addLabelGidToDocument(doc, rs.getString("gid"));
        addLabelToDocument(doc, rs.getString("name"));
        addSortNameToDocument(doc, rs.getString("sortname"));

        Integer type = rs.getInt("type");
        if (type == null) {
            type = 0;
        }
        addTypeToDocument(doc, LabelType.values()[type]);

        String begin = rs.getString("begindate");
        if (begin != null && !begin.isEmpty()) {
            addBeginDateToDocument(doc, normalizeDate(begin));
        }

        String end = rs.getString("enddate");
        if (end != null && !end.isEmpty()) {
            addEndDateToDocument(doc, normalizeDate(end));
        }

        String comment = rs.getString("resolution");
        if (comment != null && !comment.isEmpty()) {
            addCommentToDocument(doc, comment);
        }

        String labelcode = rs.getString("labelcode");
        if (labelcode != null && !labelcode.isEmpty()) {
        	addCodeToDocument(doc, labelcode);
        }
        
        addCountryToDocument(doc, rs.getString("country"));

        if (aliases.containsKey(labelId)) {
            for (String alias : aliases.get(labelId)) {
                addAliasToDocument(doc, alias);
            }
        }
        return doc;
    }


    public void addLabelGidToDocument(Document doc, String artistId) {
        doc.add(new Field(LabelIndexFieldName.LABEL_ID.getFieldname(), artistId, Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    public void addLabelToDocument(Document doc, String artist) {
        doc.add(new Field(LabelIndexFieldName.LABEL.getFieldname(), artist, Field.Store.YES, Field.Index.ANALYZED));
    }

    public void addSortNameToDocument(Document doc, String sortName) {
        doc.add(new Field(LabelIndexFieldName.SORTNAME.getFieldname(), sortName, Field.Store.YES, Field.Index.ANALYZED));
    }

    public void addTypeToDocument(Document doc, LabelType type) {
        doc.add(new Field(LabelIndexFieldName.TYPE.getFieldname(), type.getFieldname(), Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    public void addBeginDateToDocument(Document doc, String date) {
        doc.add(new Field(LabelIndexFieldName.BEGIN.getFieldname(), date, Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    public void addEndDateToDocument(Document doc, String date) {
        doc.add(new Field(LabelIndexFieldName.END.getFieldname(), date, Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    public void addCommentToDocument(Document doc, String comment) {
        doc.add(new Field(LabelIndexFieldName.COMMENT.getFieldname(), comment, Field.Store.YES, Field.Index.ANALYZED));
    }

    public void addCodeToDocument(Document doc, String code) {
        doc.add(new Field(LabelIndexFieldName.CODE.getFieldname(), code, Field.Store.YES, Field.Index.ANALYZED));
    }

    public void addCountryToDocument(Document doc, String country) {
        doc.add(new Field(LabelIndexFieldName.COUNTRY.getFieldname(), country, Field.Store.YES, Field.Index.ANALYZED));
    }

    public void addAliasToDocument(Document doc, String alias) {
        doc.add(new Field(LabelIndexFieldName.ALIAS.getFieldname(), alias, Field.Store.NO, Field.Index.ANALYZED));
    }

}
