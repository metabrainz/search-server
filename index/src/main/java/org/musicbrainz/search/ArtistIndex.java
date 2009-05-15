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

public class ArtistIndex extends Index {

    private String[] TYPES = {"unknown", "person", "group"};

    public String getName() {
        return "artist";
    }

    public int getMaxId(Connection conn) throws SQLException {
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM artist");
        rs.next();
        return rs.getInt(1);
    }

    public void indexData(IndexWriter indexWriter, Connection conn, int min, int max) throws SQLException, IOException {
        Map<Integer, List<String>> aliases = new HashMap<Integer, List<String>>();
        PreparedStatement st = conn.prepareStatement("SELECT ref, name FROM artistalias WHERE ref BETWEEN ? AND ?");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int artistId = rs.getInt(1);
            List<String> list;
            if (!aliases.containsKey(artistId)) {
                list = new LinkedList<String>();
                aliases.put(artistId, list);
            } else {
                list = aliases.get(artistId);
            }
            list.add(rs.getString(2));
        }
        st.close();
        st = conn.prepareStatement(
                "SELECT id, gid, name, sortname, type, begindate, enddate, resolution " +
                        "FROM artist WHERE id BETWEEN ? AND ?");
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
        int artistId = rs.getInt(1);
        addArtistGidToDocument(doc, rs.getString(2));
        addArtistToDocument(doc, rs.getString(3));
        addSortNameToDocument(doc, rs.getString(4));

        Integer type = rs.getInt(5);
        if (type == null) {
            type = 0;
        }
        addTypeToDocument(doc, ArtistType.values()[type]);

        String begin = rs.getString(6);
        if (begin != null && !begin.isEmpty()) {
            addBeginDateToDocument(doc, normalizeDate(begin));
        }

        String end = rs.getString(7);
        if (end != null && !end.isEmpty()) {
            addEndDateToDocument(doc, normalizeDate(end));
        }

        String comment = rs.getString(8);
        if (comment != null && !comment.isEmpty()) {
            addCommentToDocument(doc, comment);
        }

        if (aliases.containsKey(artistId)) {
            for (String alias : aliases.get(artistId)) {
                addAliasToDocument(doc, alias);
            }
        }
        return doc;
    }


    public void addArtistGidToDocument(Document doc, String artistId) {
        doc.add(new Field(ArtistIndexFieldName.ARTIST_ID.getFieldname(), artistId, Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    public void addArtistToDocument(Document doc, String artist) {
        doc.add(new Field(ArtistIndexFieldName.ARTIST.getFieldname(), artist, Field.Store.YES, Field.Index.ANALYZED));
    }

    public void addSortNameToDocument(Document doc, String sortName) {
        doc.add(new Field(ArtistIndexFieldName.SORTNAME.getFieldname(), sortName, Field.Store.YES, Field.Index.ANALYZED));
    }

    public void addTypeToDocument(Document doc, ArtistType type) {
        doc.add(new Field(ArtistIndexFieldName.TYPE.getFieldname(), type.getFieldname(), Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    public void addBeginDateToDocument(Document doc, String date) {
        doc.add(new Field(ArtistIndexFieldName.BEGIN.getFieldname(), date, Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    public void addEndDateToDocument(Document doc, String date) {
        doc.add(new Field(ArtistIndexFieldName.END.getFieldname(), date, Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    public void addCommentToDocument(Document doc, String comment) {
        doc.add(new Field(ArtistIndexFieldName.COMMENT.getFieldname(), comment, Field.Store.YES, Field.Index.ANALYZED));

    }

    public void addAliasToDocument(Document doc, String alias) {
        doc.add(new Field(ArtistIndexFieldName.ALIAS.getFieldname(), alias, Field.Store.NO, Field.Index.ANALYZED));

    }


}
