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
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.math.BigInteger;
import java.sql.*;
import java.util.*;

public class RecordingIndex extends DatabaseIndex {


    private final static int QUANTIZED_DURATION = 2000;

    public RecordingIndex(Connection dbConnection) {
        super(dbConnection);
    }

    public RecordingIndex() {
    }


    public String getName() {
        return "recording";
    }

    public Analyzer getAnalyzer() {
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
        ResultSet rs = st.executeQuery("SELECT count(*) FROM recording WHERE id<=" + maxId);
        rs.next();
        return rs.getInt(1);
    }

    String releases;
    String releasesGroupBy;

    public void init(IndexWriter indexWriter) throws SQLException {

        addPreparedStatement("TAGS",
                 "SELECT recording_tag.recording, tag.name as tag, recording_tag.count as count " +
                 " FROM recording_tag " +
                 "  INNER JOIN tag ON tag=id " +
                 " WHERE recording between ? AND ?");

        addPreparedStatement("ISRCS",
                "SELECT recording as recordingId, isrc " +
                " FROM isrc " +
                " WHERE recording BETWEEN ? AND ?  " +
                " ORDER BY recording, id");

        addPreparedStatement("ARTISTCREDITS",
                "SELECT re.id as recordingId, " +
                "  acn.position as pos, " +
                "  acn.joinphrase as joinphrase, " +
                "  a.gid as artistId,  " +
                "  a.comment as comment, " +
                "  an.name as artistName, " +
                "  an2.name as artistCreditName, " +
                "  an3.name as artistSortName " +
                " FROM recording AS re " +
                "  INNER JOIN artist_credit_name acn ON re.artist_credit=acn.artist_credit " +
                "  INNER JOIN artist a ON a.id=acn.artist " +
                "  INNER JOIN artist_name an ON a.name=an.id " +
                "  INNER JOIN artist_name an2 ON acn.name=an2.id " +
                "  INNER JOIN artist_name an3 ON a.sortname=an3.id " +
                " WHERE re.id BETWEEN ? AND ?  " +
                " ORDER BY re.id, acn.position ");

        addPreparedStatement("TRACKS",
                "SELECT tn.name as trackname, t.recording, t.position as trackposition, tl.trackcount, " +
                "  m.release as releaseid, m.position as mediumposition " +
                " FROM track t " +
                "  INNER JOIN track_name tn ON t.name=tn.id " +
                "  INNER JOIN tracklist tl ON t.tracklist=tl.id " +
                "  INNER JOIN medium m ON m.tracklist=tl.id " +
                " WHERE t.recording BETWEEN ? AND ?");

        releases =
                "SELECT " +
                "  r.id as releaseKey, r.gid as releaseid, rn.name as releasename, rgt.name as type, "+
                "  rs.name as status, sum(tr.trackcount) as tracks " +
                " FROM release r " +
                "  INNER JOIN release_group rg ON rg.id = r.release_group " +
                "  INNER JOIN medium m ON m.release=r.id " +
                "  LEFT JOIN tracklist tr ON m.tracklist=tr.id " +
                "  LEFT JOIN release_group_type rgt ON rg.type = rgt.id " +
                "  INNER JOIN release_name rn ON r.name=rn.id " +
                "  LEFT JOIN release_status rs ON r.status = rs.id " +
                " WHERE r.id in " ;

        releasesGroupBy =  " GROUP BY r.id, r.gid, rn.name, rgt.name, rs.name";

        addPreparedStatement("RECORDINGS",
                "SELECT re.id as recordingId, re.gid as trackid, re.length as duration, tn.name as trackname " +
                " FROM recording re " +
                "  INNER JOIN track_name tn ON re.name=tn.id " +
                " WHERE re.id BETWEEN ? AND ?");
    }

    /**
     * Get tag information
     *
     * @param min
     * @param max
     * @return
     * @throws SQLException
     * @throws IOException
     */
    private Map<Integer,List<Tag>> loadTags(int min, int max) throws SQLException, IOException {

        PreparedStatement st = getPreparedStatement("TAGS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        Map<Integer,List<Tag>> tags = TagHelper.completeTagsFromDbResults(rs,"recording");
        return tags;
    }

    /**
     * Get ISRC Information for the recordings
     *
     * @param min
     * @param max
     * @return
     * @throws SQLException
     * @throws IOException
     */
    private Map<Integer, List<String>> loadISRCs(int min, int max) throws SQLException, IOException {

        //ISRC
        Map<Integer, List<String>> isrcWrapper = new HashMap<Integer, List<String>>();
        PreparedStatement st = getPreparedStatement("ISRCS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int recordingId = rs.getInt("recordingId");
            List<String> list;
            if (!isrcWrapper.containsKey(recordingId)) {
                list = new LinkedList<String>();
                isrcWrapper.put(recordingId, list);
            } else {
                list = isrcWrapper.get(recordingId);
            }
            String isrc = new String(rs.getString("isrc"));
            list.add(isrc);
        }
        return isrcWrapper;
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
    private Map<Integer, ArtistCredit> loadArtists(int min, int max) throws SQLException, IOException {

        //Artists
        PreparedStatement st = getPreparedStatement("ARTISTCREDITS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        Map<Integer, ArtistCredit> artistCredits
                = ArtistCreditHelper.completeArtistCreditFromDbResults
                (rs,
                        "recordingId",
                        "artistId",
                        "artistName",
                        "artistSortName",
                        "comment",
                        "joinphrase",
                        "artistCreditName");
        return artistCredits;
    }

    /**
     * Get track  information for recordings
     * <p/>
     * One recording can be linked to by multiple tracks
     *
     * @param min
     * @param max
     * @return
     * @throws SQLException
     * @throws IOException
     */
    private Map<Integer, List<TrackWrapper>> loadTracks(int min, int max) throws SQLException, IOException {

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
            tw.setReleaseId(rs.getInt("releaseid"));
            tw.setTrackCount(rs.getInt("trackcount"));
            tw.setTrackPosition(rs.getInt("trackposition"));
            tw.setTrackName(rs.getString("trackname"));
            tw.setMediumPosition(rs.getInt("mediumposition"));
            list.add(tw);
        }
        return tracks;
    }

    /** Create the release statement
     *
     * @param noOfElements
     * @return
     * @throws SQLException
     */
    private PreparedStatement createReleaseStatement(int noOfElements) throws SQLException {
        StringBuilder inClause = new StringBuilder();
        boolean firstValue = true;
        for (int i = 0; i < noOfElements; i++) {
            if (firstValue) {
                firstValue = false;
            } else {
                inClause.append(',');
            }
            inClause.append('?');
        }
        PreparedStatement stmt = dbConnection.prepareStatement(
                releases +"(" + inClause.toString() + ')' + releasesGroupBy);
        return stmt;

    }

    /** Fill in parameters of the release statement
     *
     * @param stmt
     * @param releaseKeys
     * @throws SQLException
     */
    private void useReleaseStatement(PreparedStatement stmt,Set<Integer> releaseKeys) throws SQLException {
        int count = 1;
        for(Integer key:releaseKeys) {
            stmt.setInt(count,key);
            count++;
        }
    }

    /**
     * Get release information for recordings
     *
     * @param tracks
     * @return
     * @throws SQLException
     * @throws IOException
     */
    private Map<Integer, Release>  loadReleases
            (Map<Integer, List<TrackWrapper>> tracks) throws SQLException, IOException {

        ObjectFactory of = new ObjectFactory();

        //Add all the releaseKeys to a set to prevent duplicates
        Set<Integer> releaseKeys = new HashSet<Integer>();
        for(List<TrackWrapper> recording:tracks.values()) {
            for(TrackWrapper track  : recording) {
                releaseKeys.add(track.getReleaseId());
            }
        }

        PreparedStatement stmt = createReleaseStatement(releaseKeys.size());
        useReleaseStatement(stmt, releaseKeys);
        ResultSet rs = stmt.executeQuery();
        Map<Integer, Release> releases = new HashMap<Integer, Release>();
        Release release;
        while (rs.next()) {
            int releaseKey = rs.getInt("releaseKey");
            if (!releases.containsKey(releaseKey)) {
                release = of.createRelease();
                releases.put(releaseKey, release);
            } else {
                release = releases.get(releaseKey);
            }

            MediumList ml = of.createMediumList();
            ReleaseGroup rg = of.createReleaseGroup();
            release.setId(rs.getString("releaseId"));
            release.setTitle(rs.getString("releasename"));
            rg.setType(rs.getString("type"));
            release.setReleaseGroup(rg);
            release.setStatus(rs.getString("status"));
            ml.setTrackCount(BigInteger.valueOf(rs.getInt("tracks")));
            release.setReleaseGroup(rg);
            release.setMediumList(ml);
        }
        return releases;
    }

    /**
     * Load basic recording info
     *
     * @param indexWriter
     * @param min
     * @param max
     * @param artistCredits
     * @param tracks
     * @throws SQLException
     * @throws IOException
     */

    private void loadRecordings(IndexWriter indexWriter,
                                int min,
                                int max,
                                Map<Integer,List<Tag>> tags,
                                Map<Integer, List<String>> isrcs,
                                Map<Integer, ArtistCredit> artistCredits,
                                Map<Integer, List<TrackWrapper>> tracks,
                                Map<Integer, Release> releases ) throws SQLException, IOException {

        PreparedStatement st = getPreparedStatement("RECORDINGS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, tags, isrcs, artistCredits, tracks, releases));
        }

    }

    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {
        Map<Integer, List<Tag>> tags = loadTags(min, max);
        Map<Integer, List<String>> isrcWrapper = loadISRCs(min, max);
        Map<Integer, ArtistCredit> artistCredits = loadArtists(min, max);
        Map<Integer, List<TrackWrapper>> trackWrapper = loadTracks(min, max);
        Map<Integer, Release> releases = loadReleases(trackWrapper);
        loadRecordings(indexWriter, min, max, tags, isrcWrapper, artistCredits, trackWrapper,releases);
    }

    public Document documentFromResultSet(ResultSet rs,
                                          Map<Integer, List<Tag>> tags,
                                          Map<Integer, List<String>> isrcs,
                                          Map<Integer, ArtistCredit> artistCredits,
                                          Map<Integer, List<TrackWrapper>> tracks,
                                          Map<Integer, Release> releases) throws SQLException {

        int id = rs.getInt("recordingId");

        MbDocument doc = new MbDocument();
        doc.addField(RecordingIndexField.RECORDING_ID, rs.getString("trackid"));
        String recordingName = rs.getString("trackname");
        doc.addNonEmptyField(RecordingIndexField.RECORDING, recordingName);         //Search
        doc.addNonEmptyField(RecordingIndexField.RECORDING_OUTPUT, recordingName);  //Output
        doc.addNumericField(RecordingIndexField.DURATION, rs.getInt("duration"));
        doc.addNumericField(RecordingIndexField.QUANTIZED_DURATION, rs.getInt("duration") / QUANTIZED_DURATION);

        if (isrcs.containsKey(id)) {
            //For each credit artist for this recording
            for (String isrc : isrcs.get(id)) {
                doc.addField(RecordingIndexField.ISRC, isrc);
            }
        }

        if (tracks.containsKey(id)) {
            //For each track for this recording
            for (TrackWrapper track : tracks.get(id)) {
                doc.addNumericField(RecordingIndexField.NUM_TRACKS, track.getTrackCount());
                doc.addNumericField(RecordingIndexField.TRACKNUM, track.getTrackPosition());
                Release release = releases.get(track.getReleaseId());
                doc.addFieldOrHyphen(RecordingIndexField.RELEASE_TYPE, release.getReleaseGroup().getType());
                doc.addFieldOrHyphen(RecordingIndexField.RELEASE_STATUS, release.getStatus());
                doc.addField(RecordingIndexField.RELEASE_ID, release.getId());
                doc.addField(RecordingIndexField.RELEASE, release.getTitle());
                doc.addNumericField(RecordingIndexField.NUM_TRACKS_RELEASE, release.getMediumList().getTrackCount().intValue());

                //Added to TRACK_OUTPUT for outputting xml,
                doc.addField(RecordingIndexField.TRACK_OUTPUT, track.getTrackName());
                //and if different to recording for searching
                if(!track.getTrackName().equals(recordingName)) {
                    doc.addField(RecordingIndexField.RECORDING, track.getTrackName());
                }

                doc.addField(RecordingIndexField.POSITION, String.valueOf(track.getMediumPosition()));

            }
        }

        ArtistCredit ac = artistCredits.get(id);
        ArtistCreditHelper.buildIndexFieldsFromArtistCredit
                (doc,
                        ac,
                        RecordingIndexField.ARTIST,
                        RecordingIndexField.ARTIST_NAMECREDIT,
                        RecordingIndexField.ARTIST_ID,
                        RecordingIndexField.ARTIST_NAME,
                        RecordingIndexField.ARTIST_CREDIT);

        if (tags.containsKey(id)) {
            for (Tag tag : tags.get(id)) {
                doc.addField(RecordingIndexField.TAG, tag.getContent());
                doc.addField(RecordingIndexField.TAGCOUNT, tag.getCount().toString());
            }
        }

        return doc.getLuceneDocument();
    }

}
