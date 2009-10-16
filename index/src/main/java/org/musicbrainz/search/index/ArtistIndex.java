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
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.musicbrainz.search.analysis.StandardUnaccentAnalyzer;

import java.sql.*;

public class ArtistIndex extends Index {

    public ArtistIndex(Connection dbConnection) {
		super(dbConnection);
	}

	public String getName() {
        return "artist";
    }

    public Analyzer getAnalyzer()
    {
        return new ArtistAnalyzer();
    }

    public int getMaxId() throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM artist");
        rs.next();
        return rs.getInt(1);
    }

    public int getNoOfRows(int maxId) throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT count(*) FROM artist WHERE id<="+maxId);
        rs.next();
        return rs.getInt(1);
    }

    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {
        Map<Integer, List<String>> aliases = new HashMap<Integer, List<String>>();
        PreparedStatement st = dbConnection.prepareStatement
                ("SELECT artist_alias.artist as artist, n.name as alias " +
        		"FROM artist_alias " +
        		" JOIN artist_name n ON (artist_alias.name = n.id) " +
        		"WHERE artist BETWEEN ? AND ?");

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
            "SELECT artist.id, gid, n0.name as name, n1.name as sortname, " +
                "	lower(artist_type.name) as type, begindate_year, begindate_month, begindate_day, " +
                "	enddate_year, enddate_month, enddate_day, " +
                "	comment, lower(isocode) as country, lower(gender.name) as gender " +
                "FROM artist " +
                " LEFT JOIN artist_name n0 ON artist.name = n0.id " +
                " LEFT JOIN artist_name n1 ON artist.sortname = n1.id " +
                " LEFT JOIN artist_type ON artist.type = artist_type.id " +
                " LEFT JOIN country ON artist.country = country.id " +
                " LEFT JOIN gender ON artist.gender=gender.id " +
                "WHERE artist.id BETWEEN ? AND ?");


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

        //Search V1 allows you to search for unknown artists,
        String type=rs.getString("type");
        if(type!=null) {
            addFieldToDocument(doc, ArtistIndexField.TYPE, type);
        } else {
            addFieldToDocument(doc, ArtistIndexField.TYPE, ArtistType.UNKNOWN.getName());
        }

        addNonEmptyFieldToDocument(doc, ArtistIndexField.BEGIN,
                   Utils.formatDate(rs.getInt("begindate_year"), rs.getInt("begindate_month"), rs.getInt("begindate_day")));

        addNonEmptyFieldToDocument(doc, ArtistIndexField.END,
                   Utils.formatDate(rs.getInt("enddate_year"), rs.getInt("enddate_month"), rs.getInt("enddate_day")));

        addNonEmptyFieldToDocument(doc, ArtistIndexField.COMMENT, rs.getString("comment"));
         addNonEmptyFieldToDocument(doc, ArtistIndexField.COUNTRY, rs.getString("country"));
        addNonEmptyFieldToDocument(doc, ArtistIndexField.GENDER, rs.getString("gender"));

        if (aliases.containsKey(artistId)) {
            for (String alias : aliases.get(artistId)) {
            	addFieldToDocument(doc, ArtistIndexField.ALIAS, alias);
            }
        }
        return doc;
    }

}
