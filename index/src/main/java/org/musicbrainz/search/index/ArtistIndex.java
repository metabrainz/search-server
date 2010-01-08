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
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ArtistIndex extends DatabaseIndex {

    public ArtistIndex(Connection dbConnection) throws SQLException{
		super(dbConnection);
    }

	public String getName() {
        return "artist";
    }

    public Analyzer getAnalyzer()
    {
        return new PerFieldEntityAnalyzer(ArtistIndexField.class);
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

     @Override
    public void init() throws SQLException {
        //TODO playground also adds artist credits (artist refer to something different in particular release
        //as an alias, should we do this ? 
        addPreparedStatement("ALIASES","SELECT artist_alias.artist as artist, n.name as alias " +
        		"FROM artist_alias " +
        		" JOIN artist_name n ON (artist_alias.name = n.id) " +
        		"WHERE artist BETWEEN ? AND ?");


        addPreparedStatement("ARTISTS",
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
    }

    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {
        Map<Integer, List<String>> aliases = new HashMap<Integer, List<String>>();
        PreparedStatement st = getPreparedStatement("ALIASES");
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

        st = getPreparedStatement("ARTISTS");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, aliases));
        }
    }

    public Document documentFromResultSet(ResultSet rs, Map<Integer, List<String>> aliases) throws SQLException {
    	
        MbDocument doc = new MbDocument();
        int artistId = rs.getInt("id");
        doc.addField(ArtistIndexField.ARTIST_ID, rs.getString("gid"));
        doc.addField(ArtistIndexField.ARTIST, rs.getString("name"));
        doc.addField(ArtistIndexField.SORTNAME, rs.getString("sortname"));

        //Allows you to search for artists  of unknown type
        String type=rs.getString("type");
        if(type!=null) {
            doc.addField(ArtistIndexField.TYPE, type);
        } else {
            doc.addField(ArtistIndexField.TYPE, ArtistType.UNKNOWN.getName());
        }

        doc.addNonEmptyField(ArtistIndexField.BEGIN,
                   Utils.formatDate(rs.getInt("begindate_year"), rs.getInt("begindate_month"), rs.getInt("begindate_day")));

        doc.addNonEmptyField(ArtistIndexField.END,
                   Utils.formatDate(rs.getInt("enddate_year"), rs.getInt("enddate_month"), rs.getInt("enddate_day")));

        doc.addNonEmptyField(ArtistIndexField.COMMENT, rs.getString("comment"));
        doc.addNonEmptyField(ArtistIndexField.COUNTRY, rs.getString("country"));
        doc.addNonEmptyField(ArtistIndexField.GENDER, rs.getString("gender"));

        if (aliases.containsKey(artistId)) {
            for (String alias : aliases.get(artistId)) {
            	doc.addField(ArtistIndexField.ALIAS, alias);
            }
        }
        return doc.getLuceneDocument();
    }

}
