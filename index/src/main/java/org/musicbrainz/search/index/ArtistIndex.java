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

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.Document;

import java.sql.*;

public class ArtistIndex extends DatabaseIndex {

    public ArtistIndex(Connection dbConnection) {
        super(dbConnection);
    }

    @Override
    public String getName() {
        return "artist";
    }

    @Override
    public void init() throws SQLException {
        addPreparedStatement("NAMES",
                "SELECT artist, n.name " +
                "  FROM artist_credit_name " +
                "    JOIN artist_name n ON n.id = artist_credit_name.name " +
                "  WHERE artist BETWEEN ? AND ? " +
                "UNION " +
                "SELECT artist, n.name " +
                "  FROM artist_alias " +
                "    JOIN artist_name n ON n.id = artist_alias.name " +
                "  WHERE artist BETWEEN ? AND ? " 
        );
        
        addPreparedStatement("ARTISTS",
                "SELECT artist.id, gid, n.name AS name, n2.name AS sortname, " +
                "  artist_type.name AS type, lower(country.isocode) AS country, gender.name AS gender, " +
                "  begindate_year, begindate_month, begindate_day, " +
                "  enddate_year, enddate_month, enddate_day, " +
                "  comment " +
                "FROM artist " +
                "  JOIN artist_name n ON artist.name = n.id " +
                "  JOIN artist_name n2 ON artist.sortname = n2.id " +
                "  LEFT JOIN artist_type ON artist.type = artist_type.id " +
                "  LEFT JOIN gender ON artist.gender = gender.id " +
                "  LEFT JOIN country ON artist.country = country.id " +
                "WHERE artist.id BETWEEN ? AND ?");
    }
  
    @Override
    public int getMaxId() throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM artist");
        rs.next();
        return rs.getInt(1);
    }

    @Override
    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {
        Map<Integer, List<String>> names = new HashMap<Integer, List<String>>();
        PreparedStatement st = getPreparedStatement("NAMES");;
        st.setInt(1, min);
        st.setInt(2, max);
        st.setInt(3, min);
        st.setInt(4, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int artistId = rs.getInt("artist");
            List<String> list;
            if (!names.containsKey(artistId)) {
                list = new LinkedList<String>();
                names.put(artistId, list);
            } else {
                list = names.get(artistId);
            }
            list.add(rs.getString("name"));
        }

        st = getPreparedStatement("ARTISTS");;
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, names));
        }
    }

    protected Document documentFromResultSet(ResultSet rs, Map<Integer, List<String>> names) throws SQLException {

        Document doc = new Document();
        int artistId = rs.getInt("id");
        addFieldToDocument(doc, ArtistIndexField.ENTITY_TYPE, this.getName());
        addFieldToDocument(doc, ArtistIndexField.ENTITY_GID, rs.getString("gid"));
        addFieldToDocument(doc, ArtistIndexField.ARTIST, rs.getString("name"));
        addFieldToDocument(doc, ArtistIndexField.SORTNAME, rs.getString("sortname"));
        addNonEmptyFieldToDocument(doc, ArtistIndexField.TYPE, rs.getString("type"));
        addNonEmptyFieldToDocument(doc, ArtistIndexField.GENDER, rs.getString("gender"));
        addNonEmptyFieldToDocument(doc, ArtistIndexField.COMMENT, rs.getString("comment"));
        addNonEmptyFieldToDocument(doc, ArtistIndexField.COUNTRY, rs.getString("country"));

        addNonEmptyFieldToDocument(doc, ArtistIndexField.BEGIN, 
            Utils.formatDate(rs.getInt("begindate_year"), rs.getInt("begindate_month"), rs.getInt("begindate_day")));

        addNonEmptyFieldToDocument(doc, ArtistIndexField.END, 
            Utils.formatDate(rs.getInt("enddate_year"), rs.getInt("enddate_month"), rs.getInt("enddate_day")));
        
        if (names.containsKey(artistId)) {
            for (String name : names.get(artistId)) {
                addFieldToDocument(doc, ArtistIndexField.ALIAS, name);
            }
        }
        return doc;
    }

}
