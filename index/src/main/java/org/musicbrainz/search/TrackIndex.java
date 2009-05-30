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
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.NumberTools;

import java.sql.*;

public class TrackIndex extends Index {

    public String getName() {
        return "track";
    }

    public int getMaxId(Connection conn) throws SQLException {
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM track");
        rs.next();
        return rs.getInt(1);
    }

    public void indexData(IndexWriter indexWriter, Connection conn, int min, int max) throws SQLException, IOException {
        PreparedStatement st = conn.prepareStatement(
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

        addArtistGidToDocument(doc, rs.getString("artist_gid"));
        addArtistToDocument(doc, rs.getString("artist_name"));
        addTrackGidToDocument(doc, rs.getString("gid"));
        addTrackToDocument(doc, rs.getString("name"));
        addReleaseGidToDocument(doc, rs.getString("album_gid"));
        addReleaseToDocument(doc, rs.getString("album_name"));
        addQuantizedDurationToDocument(doc, NumberTools.longToString(rs.getLong("length")));
        addDurationToDocument(doc, NumberTools.longToString(rs.getLong("length") / 2000));
        addTrackNoToDocument(doc, NumberTools.longToString(rs.getLong("sequence")));
        return doc;
    }

    public void addTrackGidToDocument(Document doc, String trackId) {
        doc.add(new Field(TrackIndexFieldName.TRACK_ID.getFieldname(), trackId, Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    public void addTrackToDocument(Document doc, String track) {
        doc.add(new Field(TrackIndexFieldName.TRACK.getFieldname(), track, Field.Store.YES, Field.Index.ANALYZED));
    }


    public void addArtistGidToDocument(Document doc, String artistId) {
        doc.add(new Field(TrackIndexFieldName.ARTIST_ID.getFieldname(), artistId, Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    public void addArtistToDocument(Document doc, String artist) {
        doc.add(new Field(TrackIndexFieldName.ARTIST.getFieldname(), artist, Field.Store.YES, Field.Index.ANALYZED));
    }

    public void addReleaseGidToDocument(Document doc, String releaseId) {
        doc.add(new Field(TrackIndexFieldName.RELEASE_ID.getFieldname(), releaseId, Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    public void addReleaseToDocument(Document doc, String release) {
        doc.add(new Field(TrackIndexFieldName.RELEASE.getFieldname(), release, Field.Store.YES, Field.Index.ANALYZED));
    }

    public void addTypeToDocument(Document doc, ArtistType type) {
        doc.add(new Field(TrackIndexFieldName.RELEASE_TYPE.getFieldname(), type.getFieldname(), Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    public void addNumTracksToDocument(Document doc, String numTracks) {
        doc.add(new Field(TrackIndexFieldName.NUM_TRACKS.getFieldname(), numTracks, Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    public void addDurationToDocument(Document doc, String duration) {
        doc.add(new Field(TrackIndexFieldName.DURATION.getFieldname(), duration, Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    //TODO should FieldStore be YES because although search field , dont return it in xml ?
    public void addQuantizedDurationToDocument(Document doc, String qdur) {
        doc.add(new Field(TrackIndexFieldName.QUANTIZED_DURATION.getFieldname(), qdur, Field.Store.YES, Field.Index.NOT_ANALYZED));

    }

    public void addTrackNoToDocument(Document doc, String trackNo) {
        doc.add(new Field(TrackIndexFieldName.TRACKNUM.getFieldname(), trackNo, Field.Store.YES, Field.Index.NOT_ANALYZED));

    }

}
