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
import org.apache.lucene.document.Document;

import java.sql.*;

public class ArtistIndex extends Index {

    public ArtistIndex(Connection dbConnection) {
		super(dbConnection);
	}

	public String getName() {
        return "artist";
    }

    public int getMaxId() throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM artist");
        rs.next();
        return rs.getInt(1);
    }

    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {
        Map<Integer, List<String>> aliases = new HashMap<Integer, List<String>>();
        PreparedStatement st = dbConnection.prepareStatement("SELECT ref as artist, name as alias FROM artistalias WHERE ref BETWEEN ? AND ?");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int artistId = rs.getInt("artist");
            List<String> list;
            if (!aliases.containsKey(artistId)) {
                list = new LinkedList<String>();
                aliases.put(artistId, list);
            } else {
                list = aliases.get(artistId);
            }
            list.add(rs.getString("alias"));
        }
        st.close();
        st = dbConnection.prepareStatement(
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
        int artistId = rs.getInt("id");
        addFieldToDocument(doc, ArtistIndexField.ARTIST_ID, rs.getString("gid"));
        addFieldToDocument(doc, ArtistIndexField.ARTIST, rs.getString("name"));
        addFieldToDocument(doc, ArtistIndexField.SORTNAME, rs.getString("sortname"));
        addFieldToDocument(doc, ArtistIndexField.TYPE, ArtistType.values()[rs.getInt("type")].getName());

        String begin = rs.getString("begindate");
        if (begin != null && !begin.isEmpty()) {
        	addFieldToDocument(doc, ArtistIndexField.BEGIN, normalizeDate(begin));
        }

        String end = rs.getString("enddate");
        if (end != null && !end.isEmpty()) {
        	addFieldToDocument(doc, ArtistIndexField.END, normalizeDate(end));
        }

        String comment = rs.getString("resolution");
        if (comment != null && !comment.isEmpty()) {
        	addFieldToDocument(doc, ArtistIndexField.COMMENT, comment);
        }

        if (aliases.containsKey(artistId)) {
            for (String alias : aliases.get(artistId)) {
            	addFieldToDocument(doc, ArtistIndexField.ALIAS, alias);
            }
        }
        return doc;
    }

}
