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

import org.apache.commons.lang.time.StopWatch;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.MbDocument;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.*;
import java.util.*;

public class RecordingIndex extends DatabaseIndex {

    private static final int VARIOUS_ARTIST_CREDIT_ID = 1;

    public static final String INDEX_NAME = "recording";

    private StopWatch trackClock = new StopWatch();
    private StopWatch isrcClock = new StopWatch();
    private StopWatch puidClock = new StopWatch();
    private StopWatch artistClock = new StopWatch();
    private StopWatch trackArtistClock = new StopWatch();
    private StopWatch releaseClock = new StopWatch();
    private StopWatch recordingClock = new StopWatch();


    private final static int QUANTIZED_DURATION = 2000;

    public RecordingIndex(Connection dbConnection) {
        super(dbConnection);
        trackClock.start();
        isrcClock.start();
        puidClock.start();
        artistClock.start();
        trackArtistClock.start();
        releaseClock.start();
        recordingClock.start();
        trackClock.suspend();
        isrcClock.suspend();
        puidClock.suspend();
        artistClock.suspend();
        releaseClock.suspend();
        recordingClock.suspend();
        trackArtistClock.suspend();
    }

    public RecordingIndex() {
    }

    public String getName() {
        return RecordingIndex.INDEX_NAME;
    }

    public Analyzer getAnalyzer() {
        return DatabaseIndex.getAnalyzer(RecordingIndexField.class);
    }

	@Override
	public IndexField getIdentifierField() {
		return RecordingIndexField.ID;
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

    public void init(IndexWriter indexWriter, boolean isUpdater) throws SQLException {

        if(!isUpdater) {
            addPreparedStatement("PUIDS",
                 "SELECT DISTINCT recording as recordingId, puid " +
                 " FROM  tmp_release_puid " +
                 " WHERE recording between ? AND ?");

            addPreparedStatement("TRACKS",
                "SELECT id, track_name, recording, track_position, track_count, " +
                "  release_id, medium_position, format " +
                " FROM tmp_track " +
                " WHERE recording between ? AND ?");
        }
        else {
             addPreparedStatement("PUIDS",
                  "SELECT recording as recordingId, puid.puid " +
                  " FROM recording_puid " +
                  " INNER JOIN puid ON recording_puid.puid = puid.id " +
                  " AND   recording between ? AND ?");

             addPreparedStatement("TRACKS",
                "SELECT id, tn.name as track_name, t.recording, t.position as track_position, tl.track_count, " +
                "  m.release as release_id, m.position as medium_position,mf.name as format " +
                " FROM track t " +
                "  INNER JOIN track_name tn ON t.name=tn.id AND t.recording BETWEEN ? AND ?" +
                "  INNER JOIN tracklist tl ON t.tracklist=tl.id " +
                "  INNER JOIN medium m ON m.tracklist=tl.id " +
                "  LEFT JOIN  medium_format mf ON m.format=mf.id "
             );
        }

        addPreparedStatement("TAGS",
                "SELECT recording_tag.recording, tag.name as tag, recording_tag.count as count " +
                " FROM recording_tag " +
                " INNER JOIN tag ON tag=id " +
                " WHERE recording between ? AND ?");

        addPreparedStatement("ISRCS",
                "SELECT recording as recordingId, isrc " +
                " FROM isrc " +
                " WHERE recording BETWEEN ? AND ?  " +
                " ORDER BY recording, id");

        addPreparedStatement("ARTISTCREDITS",
                "SELECT r.id as recordingId, " +
                "  a.artist_credit, " +
                "  a.pos, " +
                "  a.joinphrase, " +
                "  a.artistId,  " +
                "  a.comment, " +
                "  a.artistName, " +
                "  a.artistCreditName, " +
                "  a.artistSortName, " +
                "  a.aliasName " +
                " FROM recording AS r " +
                "  INNER JOIN tmp_artistcredit a ON r.artist_credit=a.artist_credit " +
                " WHERE r.id BETWEEN ? AND ?  " +
                " ORDER BY r.id, a.pos");

        addPreparedStatement("TRACKARTISTCREDITS",
                "SELECT t.id as id, " +
                "  a.artist_credit, " +
                "  a.pos, " +
                "  a.joinphrase, " +
                "  a.artistId,  " +
                "  a.comment, " +
                "  a.artistName, " +
                "  a.artistCreditName, " +
                "  a.artistSortName, " +
                "  a.aliasName " +
                " FROM track AS t " +
                "  INNER JOIN tmp_artistcredit a ON t.artist_credit=a.artist_credit " +
                " WHERE t.recording BETWEEN ? AND ?  " +
                " ORDER BY t.recording, a.pos");

        releases =
                "SELECT " +
                "  id as releaseKey, gid as releaseid, name as releasename, type, "+
                "  status, date_year, date_month, date_day, tracks,artist_credit, country " +
                " FROM tmp_release r1 " +
                " WHERE r1.id in " ;

        addPreparedStatement("RECORDINGS",
                "SELECT re.id as recordingId, re.gid as trackid, re.length as duration, tn.name as trackname, re.comment " +
                " FROM recording re " +
                "  INNER JOIN track_name tn ON re.name=tn.id " +
                "  AND re.id BETWEEN ? AND ?");
    }

    public void destroy() throws SQLException {

        super.destroy();
        System.out.println(this.getName()+":Isrcs Queries "         + Utils.formatClock(isrcClock));
        System.out.println(this.getName()+":Track Queries "         + Utils.formatClock(trackClock));
        System.out.println(this.getName()+":Artists Queries "       + Utils.formatClock(artistClock));
        System.out.println(this.getName()+":Track Artists Queries " + Utils.formatClock(trackArtistClock));
        System.out.println(this.getName()+":Puids Queries "         + Utils.formatClock(puidClock));
        System.out.println(this.getName()+":Releases Queries "      + Utils.formatClock(releaseClock));
        System.out.println(this.getName()+":Recording Queries "     + Utils.formatClock(recordingClock));

    }

    /**
     * Get puids for the recordings
     *
     * @param min min recording id
     * @param max max recording id
     * @return
     * @throws SQLException
     * @throws IOException
     * @return A map of matches
     */
    private Map<Integer, List<String>> loadPUIDs(int min, int max) throws SQLException, IOException {

        //PUID
        puidClock.resume();
        Map<Integer, List<String>> puidWrapper = new HashMap<Integer, List<String>>();
        PreparedStatement st = getPreparedStatement("PUIDS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int recordingId = rs.getInt("recordingId");
            List<String> list;
            if (!puidWrapper.containsKey(recordingId)) {
                list = new LinkedList<String>();
                puidWrapper.put(recordingId, list);
            } else {
                list = puidWrapper.get(recordingId);
            }
            String puid = new String(rs.getString("puid"));
            list.add(puid);
        }
        rs.close();
        puidClock.suspend();
        return puidWrapper;
    }


    /**
     * Get tag information
     *
     * @param min min recording id
     * @param max max recording id
     * @return
     * @throws SQLException
     * @throws IOException
     * @return A map of matches
     */
    private Map<Integer,List<Tag>> loadTags(int min, int max) throws SQLException, IOException {

        PreparedStatement st = getPreparedStatement("TAGS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        Map<Integer,List<Tag>> tags = TagHelper.completeTagsFromDbResults(rs, "recording");
        rs.close();
        return tags;
    }

    /**
     * Get ISRC Information for the recordings
     *
     * @param min min recording id
     * @param max max recording id
     * @return map of matches
     * @throws SQLException
     * @throws IOException
     */
    private Map<Integer, List<String>> loadISRCs(int min, int max) throws SQLException, IOException {

        //ISRC
        isrcClock.resume();
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
        rs.close();
        isrcClock.suspend();
        return isrcWrapper;
    }

    /**
     * Get Recording Artist Credit
     *
     * @param min min recording id
     * @param max max recording id
     * @return
     * @throws SQLException if sql problem
     * @throws IOException  if io exception
     * @return A map of matches
     */
    private Map<Integer, ArtistCreditWrapper> loadArtists(int min, int max) throws SQLException, IOException {

        //Artists
        artistClock.resume();
        PreparedStatement st = getPreparedStatement("ARTISTCREDITS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        Map<Integer, ArtistCreditWrapper> artistCredits
                = ArtistCreditHelper.completeArtistCreditFromDbResults
                       (rs,
                        "recordingId",
                        "artist_Credit",
                         "artistId",
                        "artistName",
                        "artistSortName",
                        "comment",
                        "joinphrase",
                        "artistCreditName",
                        "aliasName");
        rs.close();
        artistClock.suspend();
        return artistCredits;
    }

    /**
     * Get Track Artist Credit
     *
     * @param min min recording id
     * @param max max recording id
     * @return
     * @throws SQLException if sql problem
     * @throws IOException  if io exception
     * @return A map of matches
     */
    private Map<Integer, ArtistCreditWrapper> loadTrackArtists(int min, int max) throws SQLException, IOException {

        //Artists
        trackArtistClock.resume();
        PreparedStatement st = getPreparedStatement("TRACKARTISTCREDITS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        Map<Integer, ArtistCreditWrapper> artistCredits
                = ArtistCreditHelper.completeArtistCreditFromDbResults
                       (rs,
                        "id",
                        "artist_Credit",
                        "artistId",
                        "artistName",
                        "artistSortName",
                        "comment",
                        "joinphrase",
                        "artistCreditName",
                        "aliasName");
        rs.close();
        trackArtistClock.suspend();
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
     * @return A map of matches
     */
    private Map<Integer, List<TrackWrapper>> loadTracks(int min, int max) throws SQLException, IOException {

        //Tracks and Release Info
        trackClock.resume();
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
            tw.setTrackId(rs.getInt("id"));
            tw.setReleaseId(rs.getInt("release_id"));
            tw.setTrackCount(rs.getInt("track_count"));
            tw.setTrackPosition(rs.getInt("track_position"));
            tw.setTrackName(rs.getString("track_name"));
            tw.setMediumPosition(rs.getInt("medium_position"));
            tw.setMediumFormat(rs.getString("format"));
            list.add(tw);
        }
        rs.close();
        trackClock.suspend();
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
                releases +"(" + inClause.toString() + ')' );
        return stmt;

    }

    /**
     * Get release information for recordings
     *
     * @param tracks
     * @return
     * @throws SQLException
     * @throws IOException
     * @return A map of matches
     */
    private Map<Integer, Release>  loadReleases
            (Map<Integer, List<TrackWrapper>> tracks) throws SQLException, IOException {


        Map<Integer, Release> releases = new HashMap<Integer, Release>();
        ObjectFactory of = new ObjectFactory();

        releaseClock.resume();

        // Add all the releaseKeys to a set to prevent duplicates
        Set<Integer> releaseKeys = new HashSet<Integer>();
        for(List<TrackWrapper> recording : tracks.values()) {
            for(TrackWrapper track : recording) {
                releaseKeys.add(track.getReleaseId());
            }
        }

        if (releaseKeys.isEmpty()) {
        	return releases;
        }
        
        PreparedStatement stmt = createReleaseStatement(releaseKeys.size());
        int count = 1;
        for(Integer key : releaseKeys) {
            stmt.setInt(count, key);
            count++;
        }

        Release release;
        ResultSet rs = stmt.executeQuery();
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
            release.setCountry(rs.getString("country"));
            release.setDate(Utils.formatDate(rs.getInt("date_year"), rs.getInt("date_month"), rs.getInt("date_day")));
            ml.setTrackCount(BigInteger.valueOf(rs.getInt("tracks")));
            release.setReleaseGroup(rg);
            release.setMediumList(ml);

            if(rs.getInt("artist_credit")==VARIOUS_ARTIST_CREDIT_ID)
            {
                ArtistCredit ac = of.createArtistCredit();
                release.setArtistCredit(ac);
            }
        }
        rs.close();
        releaseClock.suspend();
        return releases;
    }



    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {
    	
        Map<Integer, List<Tag>>             tags = loadTags(min, max);
        Map<Integer, List<String>>          puids = loadPUIDs(min, max);
        Map<Integer, List<String>>          isrcs = loadISRCs(min, max);
        Map<Integer, ArtistCreditWrapper>   artistCredits = loadArtists(min, max);
        Map<Integer, ArtistCreditWrapper>   trackArtistCredits = loadTrackArtists(min, max);
        Map<Integer, List<TrackWrapper>>    tracks = loadTracks(min, max);
        Map<Integer, Release>               releases = loadReleases(tracks);
        
        PreparedStatement st = getPreparedStatement("RECORDINGS");
        st.setInt(1, min);
        st.setInt(2, max);
        recordingClock.resume();
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, puids, tags, isrcs, artistCredits, trackArtistCredits, tracks, releases));
        }
        rs.close();
        recordingClock.suspend();
    }

    public Document documentFromResultSet(ResultSet rs,
                                          Map<Integer, List<String>> puids,
                                          Map<Integer, List<Tag>> tags,
                                          Map<Integer, List<String>> isrcs,
                                          Map<Integer, ArtistCreditWrapper> artistCredits,
                                          Map<Integer, ArtistCreditWrapper> trackArtistCredits,
                                          Map<Integer, List<TrackWrapper>> tracks,
                                          Map<Integer, Release> releases) throws SQLException {

        int id = rs.getInt("recordingId");

        MbDocument doc = new MbDocument();
        doc.addField(RecordingIndexField.ID, id);
        doc.addField(RecordingIndexField.RECORDING_ID, rs.getString("trackid"));
        String recordingName = rs.getString("trackname");
        doc.addNonEmptyField(RecordingIndexField.RECORDING, recordingName);         //Search
        doc.addNonEmptyField(RecordingIndexField.RECORDING_OUTPUT, recordingName);  //Output
        doc.addNumericField(RecordingIndexField.DURATION, rs.getInt("duration"));
        doc.addNumericField(RecordingIndexField.QUANTIZED_DURATION, rs.getInt("duration") / QUANTIZED_DURATION);
        doc.addNonEmptyField(RecordingIndexField.COMMENT, rs.getString("comment"));

        if (puids.containsKey(id)) {
            // Add each puid for recording
            for (String puid : puids.get(id)) {
                doc.addField(RecordingIndexField.PUID, puid);
            }
        }

        if (isrcs.containsKey(id)) {
            // For each credit artist for this recording
            for (String isrc : isrcs.get(id)) {
                doc.addField(RecordingIndexField.ISRC, isrc);
            }
        }

        //Recording Artist Credit
        ArtistCreditWrapper ac = artistCredits.get(id);
        if(ac!=null) {
            ArtistCreditHelper.buildIndexFieldsFromArtistCredit
               (doc,
                ac.getArtistCredit(),
                RecordingIndexField.ARTIST,
                RecordingIndexField.ARTIST_NAMECREDIT,
                RecordingIndexField.ARTIST_ID,
                RecordingIndexField.ARTIST_NAME,
                RecordingIndexField.ARTIST_CREDIT);
        }
        else {
            System.out.println("\nNo artist credit found for recording:"+rs.getString("trackid"));
        }
        if (tracks.containsKey(id)) {
            // For each track for this recording
            for (TrackWrapper track : tracks.get(id)) {
                Release release = releases.get(track.getReleaseId());
                if(release!=null)
                {
                    doc.addNumericField(RecordingIndexField.NUM_TRACKS, track.getTrackCount());
                    doc.addNumericField(RecordingIndexField.TRACKNUM, track.getTrackPosition());
                    doc.addFieldOrNoValue(RecordingIndexField.RELEASE_TYPE, release.getReleaseGroup().getType());
                    doc.addFieldOrNoValue(RecordingIndexField.RELEASE_STATUS, release.getStatus());
                    doc.addFieldOrNoValue(RecordingIndexField.RELEASE_DATE, release.getDate());
                    doc.addFieldOrNoValue(RecordingIndexField.COUNTRY, release.getCountry());
                    doc.addField(RecordingIndexField.RELEASE_ID, release.getId());
                    doc.addField(RecordingIndexField.RELEASE, release.getTitle());
                    doc.addNumericField(RecordingIndexField.NUM_TRACKS_RELEASE, release.getMediumList().getTrackCount().intValue());

                    //Is Various Artist Release
                    if(release.getArtistCredit()!=null)
                    {
                        doc.addField(RecordingIndexField.RELEASE_AC_VA, "1");
                    }
                    else
                    {
                        doc.addField(RecordingIndexField.RELEASE_AC_VA, Index.NO_VALUE);
                    }
                    // Added to TRACK_OUTPUT for outputting xml,
                    doc.addField(RecordingIndexField.TRACK_OUTPUT, track.getTrackName());
                    // and if different to recording for searching
                    if(!track.getTrackName().equals(recordingName)) {
                        doc.addField(RecordingIndexField.RECORDING, track.getTrackName());
                    }
                    doc.addField(RecordingIndexField.POSITION, String.valueOf(track.getMediumPosition()));
                    doc.addFieldOrNoValue(RecordingIndexField.FORMAT, track.getMediumFormat());

                    //Get Artist Credit for Track
                    ArtistCreditWrapper taw = trackArtistCredits.get(track.getTrackId());
                    //If different to the Artist Credit for the recording
                    if(taw!=null &&
                        (
                        (ac==null) ||
                        (taw.getArtistCreditId()!=ac.getArtistCreditId())
                        )
                    ) {
                        ArtistCreditHelper.buildIndexFieldsFromArtistCredit
                           (doc,
                            taw.getArtistCredit(),
                            RecordingIndexField.ARTIST,
                            RecordingIndexField.ARTIST_NAMECREDIT,
                            RecordingIndexField.ARTIST_ID,
                            RecordingIndexField.ARTIST_NAME,
                            RecordingIndexField.TRACK_ARTIST_CREDIT);
                    }
                    else {
                        doc.addField(RecordingIndexField.TRACK_ARTIST_CREDIT,Index.NO_VALUE);
                    }
                }
            }
        }
        else
        {
            doc.addFieldOrNoValue(RecordingIndexField.RELEASE_TYPE, "standalone");
        }

        if (tags.containsKey(id)) {
            for (Tag tag : tags.get(id)) {
                doc.addField(RecordingIndexField.TAG, tag.getName());
                doc.addField(RecordingIndexField.TAGCOUNT, tag.getCount().toString());
            }
        }

        return doc.getLuceneDocument();
    }

}
