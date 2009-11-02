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
import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.Analyzer;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;

public class RecordingIndex extends DatabaseIndex {

    private final static int QUANTIZED_DURATION = 2000;
   
    public RecordingIndex(Connection dbConnection) {
        super(dbConnection);
    }

    public String getName() {
        return "recording";
    }

    public Analyzer getAnalyzer()
    {
        return new PerFieldEntityAnalyzer(RecordingIndexField.class);
    }

    public int getMaxId() throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM recording");
        rs.next();
        return rs.getInt(1);
    }

    public int getNoOfRows(int maxId) throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT count(*) FROM recording WHERE id<="+maxId);
        rs.next();
        return rs.getInt(1);
    }

    public void init() throws SQLException {
          addPreparedStatement("ARTISTS",
                  "SELECT re.id as recordingId, " +
               "acn.position as pos, " +
               "acn.joinphrase as joinphrase, " +
               "a.gid as artistId,  " +
               "a.comment as comment, " +
               "an.name as artistName, " +
               "an2.name as artistCreditName, " +
               "an3.name as artistSortName " +
               "FROM recording AS re " +
               "INNER JOIN artist_credit_name acn ON re.artist_credit=acn.artist_credit " +
               "INNER JOIN artist a ON a.id=acn.artist " +
               "INNER JOIN artist_name an on a.name=an.id " +
               "INNER JOIN artist_name an2 on acn.name=an2.id " +
               "INNER JOIN artist_name an3 on a.sortname=an3.id " +
               "WHERE re.id BETWEEN ? AND ?  " +
               "order by re.id,acn.position ");

        addPreparedStatement("TRACKS",
                "SELECT t.recording, t.position,tl.trackcount, " +
                "r.gid as releaseid,rn.name as releasename,rgt.name as type " +
                "FROM track t " +
                "INNER JOIN tracklist tl " +
                "ON t.tracklist=tl.id " +
                "INNER JOIN medium m " +
                "ON m.tracklist=tl.id " +
                "INNER JOIN release r " +
                "ON m.release=r.id " +
                "INNER JOIN release_group rg " +
                "ON rg.id = r.release_group " +
                "LEFT JOIN release_group_type rgt " +
                "ON rg.type = rgt.id " +
                "INNER JOIN release_name rn " +
                "ON r.name=rn.id " +
                "WHERE t.recording BETWEEN ? AND ?");

        addPreparedStatement("RECORDINGS",
                "SELECT re.id as recordingId,re.gid as trackid,re.length as duration,tn.name as trackname " +
                "FROM recording re " +
                "INNER JOIN track_name tn " +
                "ON re.name=tn.id " +
                "WHERE re.id BETWEEN ? AND ?");
    }

    /**
     * Get Artist Information for the recordings
     *
     * @param min
     * @param max
     * @return
     * @throws SQLException
     * @throws IOException
     */
    private Map<Integer, List<ArtistWrapper>> loadArtists(int min, int max) throws SQLException, IOException{

        //Artists
        Map<Integer, List<ArtistWrapper>> artistWrapper = new HashMap<Integer, List<ArtistWrapper>>();
        PreparedStatement st = getPreparedStatement("ARTISTS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
           int recordingId = rs.getInt("recordingId");
           List<ArtistWrapper> list;
           if (!artistWrapper.containsKey(recordingId)) {
               list = new LinkedList<ArtistWrapper>();
               artistWrapper.put(recordingId, list);
           } else {
               list = artistWrapper.get(recordingId);
           }
           ArtistWrapper aw = new ArtistWrapper();
           aw.setArtistId(rs.getString("artistId"));
           aw.setArtistName(rs.getString("artistName"));
           aw.setArtistCreditName(rs.getString("artistCreditName"));
           aw.setArtistSortName(rs.getString("artistSortName"));
           aw.setArtistPos(rs.getInt("pos"));
           aw.setArtistComment(rs.getString("comment"));
           aw.setJoinPhrase(rs.getString("joinphrase"));
           list.add(aw);
        }
        return artistWrapper;
    }

    /**
     * Get track (and release ) information for recordings
     *
     * One recording can be linked to by multiple tracks
     * @param min
     * @param max
     * @return
     * @throws SQLException
     * @throws IOException
     */
    private Map<Integer, List<TrackWrapper>> loadTracks(int min, int max) throws SQLException, IOException{

        //Tracks and Release Info
        Map<Integer, List<TrackWrapper>> tracks = new HashMap<Integer, List<TrackWrapper>>();
        PreparedStatement st = getPreparedStatement("TRACKS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
           int recordingId = rs.getInt("recording");
           List<TrackWrapper> list;
           if (!tracks.containsKey(recordingId)) {
               list = new LinkedList<TrackWrapper>();
               tracks.put(recordingId, list);
           } else {
               list = tracks.get(recordingId);
           }
           TrackWrapper tw = new TrackWrapper();
           tw.setReleaseGroupType(rs.getString("type"));
           tw.setReleaseId(rs.getString("releaseid"));
           tw.setReleaseName(rs.getString("releasename"));
           tw.setTrackCount(rs.getInt("trackcount"));
           tw.setTrackPosition(rs.getInt("position"));
           list.add(tw);
        }
        return tracks;
    }

    /**
     * Load basic recording info
     *
     * @param indexWriter
     * @param min
     * @param max
     * @param artists
     * @param tracks
     * @throws SQLException
     * @throws IOException
     */
    private void loadRecordings(IndexWriter indexWriter,
                                int min,
                                int max,
                                Map<Integer, List<ArtistWrapper>> artists,
                                Map<Integer, List<TrackWrapper>> tracks) throws SQLException, IOException{

        PreparedStatement st = getPreparedStatement("RECORDINGS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs,artists,tracks));
        }

    }

    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {
        Map<Integer, List<ArtistWrapper>>  artistWrapper = loadArtists(min,max);
        Map<Integer, List<TrackWrapper>>   trackWrapper  = loadTracks(min,max);

        loadRecordings(indexWriter,min,max,artistWrapper,trackWrapper);
    }

    public Document documentFromResultSet(ResultSet rs,
                                          Map<Integer, List<ArtistWrapper>> artists,
                                          Map<Integer, List<TrackWrapper>> tracks) throws SQLException {

        int id = rs.getInt("recordingId");

        Document doc = new Document();
        addFieldToDocument(doc, RecordingIndexField.RECORDING_ID, rs.getString("trackid"));
        addNonEmptyFieldToDocument(doc, RecordingIndexField.RECORDING, rs.getString("trackname"));
        addNumericFieldToDocument(doc, RecordingIndexField.DURATION, rs.getInt("duration"));
        addNumericFieldToDocument(doc, RecordingIndexField.QUANTIZED_DURATION, rs.getInt("duration") / QUANTIZED_DURATION);

        if (tracks.containsKey(id)) {
            //For each track for this recording
            for (TrackWrapper track : tracks.get(id)) {
                addNumericFieldToDocument(doc, RecordingIndexField.NUM_TRACKS, track.getTrackCount());
                addNumericFieldToDocument(doc, RecordingIndexField.TRACKNUM, track.getTrackPosition());
                addFieldOrHyphenToDocument(doc, RecordingIndexField.RELEASE_TYPE, track.getReleaseGroupType());
                addFieldToDocument(doc, RecordingIndexField.RELEASE_ID, track.getReleaseId());
                addFieldToDocument(doc, RecordingIndexField.RELEASE, track.getReleaseName());
            }
        }

        if (artists.containsKey(id)) {
            //For each credit artist for this recording
            for (ArtistWrapper artist : artists.get(id)) {
                addFieldToDocument(doc, RecordingIndexField.ARTIST_ID, artist.getArtistId());
                addFieldToDocument(doc, RecordingIndexField.ARTIST_NAME, artist.getArtistName());
                addFieldToDocument(doc, RecordingIndexField.ARTIST_SORTNAME, artist.getArtistSortName());
                addFieldToDocument(doc, RecordingIndexField.ARTIST_NAMECREDIT, artist.getArtistCreditName());
                addFieldOrHyphenToDocument(doc, RecordingIndexField.ARTIST_JOINPHRASE, artist.getJoinPhrase());
                addFieldOrHyphenToDocument(doc, RecordingIndexField.ARTIST_COMMENT, artist.getArtistComment());
            }

            addFieldToDocument(doc, RecordingIndexField.ARTIST, ArtistWrapper.createFullArtistCredit(artists.get(id)));
        }
        return doc;
    }

}