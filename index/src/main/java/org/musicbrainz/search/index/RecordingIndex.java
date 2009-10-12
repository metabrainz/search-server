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
        addPreparedStatement("ARTIST_NAMES",
                "SELECT t.recording, n0.name as credit_name, joinphrase, n1.name as artist_name " +
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
                "       ) t ON t.artist_credit = artist_credit_name.artist_credit "
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
        
        // Get artists names
        Map<Integer, List<String>> artistNames = new HashMap<Integer, List<String>>();
        PreparedStatement st = getPreparedStatement("ARTIST_NAMES");
        st.setInt(1, min);
        st.setInt(2, max);
        st.setInt(3, min);
        st.setInt(4, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int recordingId = rs.getInt("recording");

            List<String> list;
            if (!artistNames.containsKey(recordingId)) {
                list = new LinkedList<String>();
                artistNames.put(recordingId, list);
            } else {
                list = artistNames.get(recordingId);
            }
            String creditName = rs.getString("credit_name");
            list.add(creditName);
            
            String joinphrase = rs.getString("joinphrase");
            if (joinphrase != null && !joinphrase.isEmpty()) { list.add(joinphrase); }
            
            if (!creditName.equals(rs.getString("artist_name"))) list.add(rs.getString("artist_name"));
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
            indexWriter.addDocument(documentFromResultSet(rs, artistNames, trackNames));
        }
    }

    protected Document documentFromResultSet(ResultSet rs, Map<Integer, List<String>> artistNames, Map<Integer, List<String>> trackNames)
    throws SQLException {
        Document doc = new Document();

        int recordingId = rs.getInt("id");
        addFieldToDocument(doc, RecordingIndexField.ENTITY_TYPE, this.getName());
        addFieldToDocument(doc, RecordingIndexField.ENTITY_GID, rs.getString("gid"));
        addFieldToDocument(doc, RecordingIndexField.RECORDING, rs.getString("name"));
        addNonEmptyFieldToDocument(doc, RecordingIndexField.COMMENT, rs.getString("comment"));
        addFieldToDocument(doc, RecordingIndexField.DURATION, NumericUtils.longToPrefixCoded(rs.getLong("length")));
        addFieldToDocument(doc, RecordingIndexField.QUANTIZED_DURATION, NumericUtils.longToPrefixCoded(rs.getLong("length") / QUANTIZED_DURATION));
        
        if (artistNames.containsKey(recordingId)) {
            for (String name : artistNames.get(recordingId)) {
                addFieldToDocument(doc, RecordingIndexField.ARTIST, name);
            }
        }
        
        if (trackNames.containsKey(recordingId)) {
            for (String name : trackNames.get(recordingId)) {
                addFieldToDocument(doc, RecordingIndexField.TRACK, name);
            }
        }
        
        return doc;
    }

}
