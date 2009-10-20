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

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.document.Document;
import org.musicbrainz.search.MbDocument;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RecordingIndex extends DatabaseIndex {

    private final static int QUANTIZED_DURATION = 2000;
    
    public RecordingIndex(Connection dbConnection) {
        super(dbConnection);
    }

    @Override
    public String getName() {
        return "recording";
    }

    @Override
    public void init() throws SQLException {
        addPreparedStatement("ARTIST_CREDITS",
                "SELECT t.recording, t.artist_credit, " +
                "    n0.name as credit_name, joinphrase, artist.id as artist_id, n1.name as artist_name " +
                "  FROM artist_name n0 " +
                "    JOIN artist_credit_name ON artist_credit_name.name = n0.id " +
                "    JOIN artist ON artist.id = artist_credit_name.artist " +
                "    JOIN artist_name n1 ON n1.id = artist.name " +
                "    JOIN ( " +
                "       SELECT recording.id AS recording, artist_credit " +
                "          FROM recording " +
                "          WHERE recording.id BETWEEN ? AND ? " +
                "       UNION " +
                "       SELECT DISTINCT recording, artist_credit " +
                "          FROM track " +
                "          WHERE track.recording BETWEEN ? AND ? " +
                "       ) t ON t.artist_credit = artist_credit_name.artist_credit " +
                "  ORDER BY t.recording, t.artist_credit, artist_credit_name.position"
        );

        addPreparedStatement("TRACK_NAMES",
                "SELECT t.recording, n.name " +
                "  FROM track_name n " +
                "    JOIN (" +
                "       SELECT DISTINCT recording, name " +
                "       FROM track " +
                "       WHERE track.recording BETWEEN ? AND ?" +
                "    ) t ON t.name = n.id  "
        );
        
        addPreparedStatement("RECORDINGS",
                "SELECT recording.id, gid, n.name, comment, length " +
                "FROM recording " +
                "  JOIN track_name n ON n.id = recording.name " +
                "WHERE recording.id BETWEEN ? AND ? "
        );
    }
    
    @Override
    public int getMaxId() throws SQLException {
        Statement st = getDbConnection().createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM recording");
        rs.next();
        return rs.getInt(1);
    }

    @Override
    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {
        
        // Get artists credits
        Map<Integer, Map<Integer, ArtistCredit>> artistCredits = new HashMap<Integer, Map<Integer, ArtistCredit>>();
        PreparedStatement st = getPreparedStatement("ARTIST_CREDITS");
        st.setInt(1, min);
        st.setInt(2, max);
        st.setInt(3, min);
        st.setInt(4, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int recordingId = rs.getInt("recording");
            int artistCreditId = rs.getInt("artist_credit");

            Map<Integer, ArtistCredit> rgArtistCredits;
            if (!artistCredits.containsKey(recordingId)) {
                rgArtistCredits = new HashMap<Integer, ArtistCredit>();
                artistCredits.put(recordingId, rgArtistCredits);
            } else {
                rgArtistCredits = artistCredits.get(recordingId);
            }

            ArtistCredit ac;
            if (!rgArtistCredits.containsKey(artistCreditId)) {
                ac = new ArtistCredit();
                rgArtistCredits.put(artistCreditId, ac);
            } else {
                ac = rgArtistCredits.get(artistCreditId);
            }
            ArtistCreditName acn = new ArtistCreditName(
                    rs.getString("credit_name"), 
                    rs.getString("joinphrase"),
                    rs.getInt("artist_id"), 
                    rs.getString("artist_name")
            );
            ac.appendArtistCreditName(acn);
 
        }
        
        // Get tracks names
        Map<Integer, List<String>> trackNames = new HashMap<Integer, List<String>>();
        st = getPreparedStatement("TRACK_NAMES");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            int recordingId = rs.getInt("recording");

            List<String> list;
            if (!trackNames.containsKey(recordingId)) {
                list = new LinkedList<String>();
                trackNames.put(recordingId, list);
            } else {
                list = trackNames.get(recordingId);
            }
            list.add(rs.getString("name"));
        }
        
        // Get recordings
        st = getPreparedStatement("RECORDINGS");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, artistCredits, trackNames));
        }
    }

    protected Document documentFromResultSet(ResultSet rs, Map<Integer, Map<Integer, ArtistCredit>> artistCredits, Map<Integer, List<String>> trackNames)
    throws SQLException {
        MbDocument doc = new MbDocument();

        int recordingId = rs.getInt("id");
        doc.addField(RecordingIndexField.ENTITY_TYPE, this.getName());
        doc.addField(RecordingIndexField.ENTITY_GID, rs.getString("gid"));
        doc.addField(RecordingIndexField.RECORDING, rs.getString("name"));
        doc.addNonEmptyField(RecordingIndexField.COMMENT, rs.getString("comment"));
        doc.addField(RecordingIndexField.DURATION, NumericUtils.longToPrefixCoded(rs.getLong("length")));
        doc.addField(RecordingIndexField.QUANTIZED_DURATION, NumericUtils.longToPrefixCoded(rs.getLong("length") / QUANTIZED_DURATION));
        
        if (artistCredits.containsKey(recordingId)) {
            for (ArtistCredit ac : artistCredits.get(recordingId).values()) {
                doc.addField(ReleaseGroupIndexField.ARTIST, ac.getArtistCreditString());
                for (ArtistCreditName acn : ac) {
                    if (!acn.getName().equals(acn.getArtistName())) {
                        doc.addField(ReleaseGroupIndexField.ARTIST, acn.getArtistName());
                    }
                }
            }
        }
        
        if (trackNames.containsKey(recordingId)) {
            for (String name : trackNames.get(recordingId)) {
                doc.addField(RecordingIndexField.TRACK, name);
            }
        }
        
        return doc.getLuceneDocument();
    }

}
