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

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.NumberTools;

import java.sql.*;

public class TrackIndex extends Index {

    public TrackIndex(Connection dbConnection) {
		super(dbConnection);
	}

	public String getName() {
        return "track";
    }

    public int getMaxId() throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM track");
        rs.next();
        return rs.getInt(1);
    }

    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {
        PreparedStatement st = dbConnection.prepareStatement(
                "SELECT artist.gid as artist_gid, artist.name as artist_name, " +
                        "track.gid, track.name, " +
                        "album.gid as album_gid, album.name as album_name, track.length, albumjoin.sequence " +
                        "FROM track " +
                        "JOIN artist ON track.artist=artist.id " +
                        "JOIN albumjoin ON track.id=albumjoin.track " +
                        "JOIN album ON album.id=albumjoin.album " +
                        "WHERE track.id BETWEEN ? AND ?");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs));
        }
        st.close();
    }

    //TODO Numtracks and type
    public Document documentFromResultSet(ResultSet rs) throws SQLException {
        Document doc = new Document();

        addFieldToDocument(doc, TrackIndexField.TRACK_ID, rs.getString("gid"));
        addFieldToDocument(doc, TrackIndexField.TRACK, rs.getString("name"));
        addFieldToDocument(doc, TrackIndexField.ARTIST_ID, rs.getString("artist_gid"));
        addFieldToDocument(doc, TrackIndexField.ARTIST, rs.getString("artist_name"));
        addFieldToDocument(doc, TrackIndexField.RELEASE_ID, rs.getString("album_gid"));
        addFieldToDocument(doc, TrackIndexField.RELEASE, rs.getString("album_name"));
        addFieldToDocument(doc, TrackIndexField.QUANTIZED_DURATION, NumberTools.longToString(rs.getLong("length")));
        addFieldToDocument(doc, TrackIndexField.DURATION, NumberTools.longToString(rs.getLong("length") / 2000));
        addFieldToDocument(doc, TrackIndexField.TRACKNUM, NumberTools.longToString(rs.getLong("sequence")));
        
        return doc;
    }

}
