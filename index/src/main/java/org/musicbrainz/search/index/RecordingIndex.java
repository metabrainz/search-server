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

import com.google.common.base.Strings;
import org.apache.commons.lang.time.StopWatch;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.similarities.Similarity;
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.RecordingSimilarity;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.*;
import java.util.*;

public class RecordingIndex extends DatabaseIndex {

    private static final String VARIOUS_ARTISTS_GUID = "89ad4ac3-39f7-470e-963a-56509c546377";
    private static final String VARIOUS_ARTISTS_NAME = "Various Artists";

    private static final int VARIOUS_ARTIST_CREDIT_ID = 1;

    public static final String INDEX_NAME = "recording";

    private StopWatch trackClock = new StopWatch();
    private StopWatch isrcClock = new StopWatch();
    private StopWatch puidClock = new StopWatch();
    private StopWatch artistClock = new StopWatch();
    private StopWatch trackArtistClock = new StopWatch();
    private StopWatch releaseClock = new StopWatch();
    private StopWatch recordingClock = new StopWatch();
    private StopWatch buildClock = new StopWatch();
    private StopWatch storeClock = new StopWatch();


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
        buildClock.start();
        storeClock.start();
        trackClock.suspend();
        isrcClock.suspend();
        puidClock.suspend();
        artistClock.suspend();
        releaseClock.suspend();
        recordingClock.suspend();
        trackArtistClock.suspend();
        buildClock.suspend();
        storeClock.suspend();
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
    String releaseEvents;
    String releaseSecondaryTypes;

    @Override
    public Similarity getSimilarity() {
        return new RecordingSimilarity();
    }

    public void init(IndexWriter indexWriter, boolean isUpdater) throws SQLException {

        if (!isUpdater) {
            addPreparedStatement("PUIDS",
                    "SELECT DISTINCT recording as recordingId, puid " +
                            " FROM  tmp_release_puid " +
                            " WHERE recording between ? AND ?");

            addPreparedStatement("TRACKS",
                    "SELECT id, gid, track_name, length as duration, recording, track_position, track_number, track_count, " +
                            "  release_id, medium_position, format " +
                            " FROM tmp_track " +
                            " WHERE recording between ? AND ?");
        } else {
            addPreparedStatement("PUIDS",
                    "SELECT recording as recordingId, puid.puid " +
                            " FROM recording_puid " +
                            " INNER JOIN puid ON recording_puid.puid = puid.id " +
                            " AND   recording between ? AND ?");

            addPreparedStatement("TRACKS",
                    "SELECT t.id, t.gid, tn.name as track_name, t.length as duration, t.recording, t.position as track_position, t.number as track_number, m.track_count, " +
                            "  m.release as release_id, m.position as medium_position,mf.name as format " +
                            " FROM track t " +
                            "  INNER JOIN track_name tn ON t.name=tn.id AND t.recording BETWEEN ? AND ?" +
                            "  INNER JOIN medium m ON t.medium=m.id " +
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
                        "  id as releaseKey, gid as releaseid, name as releasename, type, " +
                        "  status, tracks,artist_credit, rg_gid " +
                        " FROM tmp_release r1 " +
                        " WHERE r1.id in ";

        releaseEvents =
                " SELECT release, country, " +
                        "   date_year, date_month, date_day"+
                        " FROM tmp_release_event r " +
                        " WHERE r.release in ";

        releaseSecondaryTypes =
                "SELECT rg.name as type, r.id as releaseKey" +
                        " FROM tmp_release r " +
                        " INNER JOIN release_group_secondary_type_join  rgj " +
                        " ON r.rg_id=rgj.release_group " +
                        " INNER JOIN release_group_secondary_type rg  " +
                        " ON rgj.secondary_type = rg.id " +
                        " WHERE r.id in ";

        addPreparedStatement("RECORDINGS",
                "SELECT re.id as recordingId, re.gid as trackid, re.length as duration, tn.name as trackname, re.comment " +
                        " FROM recording re " +
                        "  INNER JOIN track_name tn ON re.name=tn.id " +
                        "  AND re.id BETWEEN ? AND ?");
    }

    public void destroy() throws SQLException {

        super.destroy();
        System.out.println(this.getName() + ":Isrcs Queries " + Utils.formatClock(isrcClock));
        System.out.println(this.getName() + ":Track Queries " + Utils.formatClock(trackClock));
        System.out.println(this.getName() + ":Artists Queries " + Utils.formatClock(artistClock));
        System.out.println(this.getName() + ":Track Artists Queries " + Utils.formatClock(trackArtistClock));
        System.out.println(this.getName() + ":Puids Queries " + Utils.formatClock(puidClock));
        System.out.println(this.getName() + ":Releases Queries " + Utils.formatClock(releaseClock));
        System.out.println(this.getName() + ":Recording Queries " + Utils.formatClock(recordingClock));
        System.out.println(this.getName() + ":Build Index " + Utils.formatClock(buildClock));
        System.out.println(this.getName() + ":Build Store " + Utils.formatClock(storeClock));

    }

    /**
     * Get puids for the recordings
     *
     * @param min min recording id
     * @param max max recording id
     * @return A map of matches
     * @throws SQLException
     * @throws IOException
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
     * @return A map of matches
     * @throws SQLException
     * @throws IOException
     */
    private Map<Integer, List<Tag>> loadTags(int min, int max) throws SQLException, IOException {

        PreparedStatement st = getPreparedStatement("TAGS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        Map<Integer, List<Tag>> tags = TagHelper.completeTagsFromDbResults(rs, "recording");
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
     * @return A map of matches
     * @throws SQLException if sql problem
     * @throws IOException  if io exception
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
     * @return A map of matches
     * @throws SQLException if sql problem
     * @throws IOException  if io exception
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
     * @return A map of matches
     * @throws SQLException
     * @throws IOException
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
            tw.setTrackGuid(rs.getString("gid"));
            tw.setReleaseId(rs.getInt("release_id"));
            tw.setTrackCount(rs.getInt("track_count"));
            tw.setTrackPosition(rs.getInt("track_position"));
            tw.setTrackName(rs.getString("track_name"));
            tw.setMediumPosition(rs.getInt("medium_position"));
            tw.setMediumFormat(rs.getString("format"));
            tw.setDuration(rs.getInt("duration"));
            tw.setTrackNumber(rs.getString("track_number"));
            list.add(tw);
        }
        rs.close();
        trackClock.suspend();
        return tracks;
    }

    /**
     * Create the release statement
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
                releases + "(" + inClause.toString() + ')');
        return stmt;

    }

    private PreparedStatement createReleaseEventStatement(int noOfElements) throws SQLException {
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
                releaseEvents + "(" + inClause.toString() + ')');
        return stmt;

    }

    /**
     * Create the release secondary types statement
     *
     * @param noOfElements
     * @return
     * @throws SQLException
     */
    private PreparedStatement createReleaseSecondaryTypesStatement(int noOfElements) throws SQLException {
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
                releaseSecondaryTypes + "(" + inClause.toString() + ')');
        return stmt;

    }

    /**
     * Get release information for recordings
     *
     * @param tracks
     * @return A map of matches
     * @throws SQLException
     * @throws IOException
     */
    private Map<Integer, Release> loadReleases
    (Map<Integer, List<TrackWrapper>> tracks) throws SQLException, IOException {


        Map<Integer, Release> releases = new HashMap<Integer, Release>();
        ObjectFactory of = new ObjectFactory();

        try {
            releaseClock.resume();
        } catch (IllegalStateException e) {
            System.out.println("Warning: IllegalStateException during StopWatch.resume");
        }

        // Add all the releaseKeys to a set to prevent duplicates
        Set<Integer> releaseKeys = new HashSet<Integer>();
        for (List<TrackWrapper> recording : tracks.values()) {
            for (TrackWrapper track : recording) {
                releaseKeys.add(track.getReleaseId());
            }
        }

        if (releaseKeys.isEmpty()) {
            return releases;
        }

        PreparedStatement stmt = createReleaseStatement(releaseKeys.size());
        int count = 1;
        for (Integer key : releaseKeys) {
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
            rg.setPrimaryType(rs.getString("type"));
            rg.setId(rs.getString("rg_gid"));
            release.setReleaseGroup(rg);
            release.setStatus(rs.getString("status"));
            ml.setTrackCount(BigInteger.valueOf(rs.getInt("tracks")));
            release.setReleaseGroup(rg);
            release.setMediumList(ml);

            if (rs.getInt("artist_credit") == VARIOUS_ARTIST_CREDIT_ID) {
                ArtistCredit ac = createVariousArtistsCredit();
                release.setArtistCredit(ac);
            }
        }
        rs.close();

        //Add ReleaseEvents for each Release
        stmt = createReleaseEventStatement(releaseKeys.size());
        count = 1;
        for (Integer key : releaseKeys) {
            stmt.setInt(count, key);
            count++;
        }
        rs = stmt.executeQuery();
        while (rs.next()) {
            int releaseKey = rs.getInt("release");
            release = releases.get(releaseKey);
            if (release.getReleaseEventList() == null) {
                release.setReleaseEventList(of.createReleaseEventList());
            }
            ReleaseEvent re = of.createReleaseEvent();
            re.setDate(Strings.emptyToNull(Utils.formatDate(rs.getInt("date_year"), rs.getInt("date_month"), rs.getInt("date_day"))));
            re.setCountry((rs.getString("country")));
            release.getReleaseEventList().getReleaseEvent().add(re);
        }

        //Add secondary types of the releasegroup that each release is part of
        stmt = createReleaseSecondaryTypesStatement(releaseKeys.size());
        count = 1;
        for (Integer key : releaseKeys) {
            stmt.setInt(count, key);
            count++;
        }
        rs = stmt.executeQuery();
        while (rs.next()) {
            int releaseKey = rs.getInt("releaseKey");
            release = releases.get(releaseKey);
            ReleaseGroup rg = release.getReleaseGroup();
            if (rg.getSecondaryTypeList() == null) {
                rg.setSecondaryTypeList(of.createSecondaryTypeList());
            }
            rg.getSecondaryTypeList().getSecondaryType().add(rs.getString("type"));
        }

        try {
            releaseClock.suspend();
        } catch (IllegalStateException e) {
            System.out.println("Warning: IllegalStateException during StopWatch.resume");
        }
        return releases;
    }


    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {

        Map<Integer, List<Tag>> tags = loadTags(min, max);
        Map<Integer, List<String>> puids = loadPUIDs(min, max);
        Map<Integer, List<String>> isrcs = loadISRCs(min, max);
        Map<Integer, ArtistCreditWrapper> artistCredits = loadArtists(min, max);
        Map<Integer, ArtistCreditWrapper> trackArtistCredits = loadTrackArtists(min, max);
        Map<Integer, List<TrackWrapper>> tracks = loadTracks(min, max);
        Map<Integer, Release> releases = loadReleases(tracks);

        PreparedStatement st = getPreparedStatement("RECORDINGS");
        st.setInt(1, min);
        st.setInt(2, max);
        recordingClock.resume();
        ResultSet rs = st.executeQuery();
        recordingClock.suspend();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, puids, tags, isrcs, artistCredits, trackArtistCredits, tracks, releases));
        }
        rs.close();

    }

    public Document documentFromResultSet(ResultSet rs,
                                          Map<Integer, List<String>> puids,
                                          Map<Integer, List<Tag>> tags,
                                          Map<Integer, List<String>> isrcs,
                                          Map<Integer, ArtistCreditWrapper> artistCredits,
                                          Map<Integer, ArtistCreditWrapper> trackArtistCredits,
                                          Map<Integer, List<TrackWrapper>> tracks,
                                          Map<Integer, Release> releases) throws SQLException {

        buildClock.resume();
        Set<Integer> durations = new HashSet<Integer>();
        Set<Integer> qdurs = new HashSet<Integer>();

        Set<String> trackNames = new HashSet<String>();

        int id = rs.getInt("recordingId");

        MbDocument doc = new MbDocument();
        ObjectFactory of = new ObjectFactory();
        Recording recording = of.createRecording();

        doc.addField(RecordingIndexField.ID, id);

        String guid = rs.getString("trackid");
        doc.addField(RecordingIndexField.RECORDING_ID, guid);
        recording.setId(guid);

        String recordingName = rs.getString("trackname");
        //Just add an accent version for recording name not track names
        doc.addField(RecordingIndexField.RECORDING_ACCENT, recordingName);
        recording.setTitle(recordingName);

        trackNames.add(recordingName.toLowerCase(Locale.UK));
        int recordingDuration = rs.getInt("duration");
        if (recordingDuration > 0) {
            durations.add(recordingDuration);
            recording.setLength(BigInteger.valueOf(recordingDuration));
        }

        String comment = rs.getString("comment");
        doc.addFieldOrNoValue(RecordingIndexField.COMMENT, comment);
        if (!Strings.isNullOrEmpty(comment)) {
            recording.setDisambiguation(comment);
        }

        if (puids.containsKey(id)) {
            PuidList puidList = of.createPuidList();
            // Add each puid for recording
            for (String nextPuid : puids.get(id)) {
                doc.addField(RecordingIndexField.PUID, nextPuid);
                Puid puid = of.createPuid();
                puid.setId(nextPuid);
                puidList.getPuid().add(puid);
            }
            recording.setPuidList(puidList);
        }

        if (isrcs.containsKey(id)) {
            IsrcList isrcList = of.createIsrcList();
            for (String nextIsrc : isrcs.get(id)) {
                doc.addField(RecordingIndexField.ISRC, nextIsrc);
                Isrc isrc = of.createIsrc();
                isrc.setId(nextIsrc);
                isrcList.getIsrc().add(isrc);
            }
            recording.setIsrcList(isrcList);
        } else {
            doc.addFieldOrNoValue(RecordingIndexField.ISRC, null);
        }

        //Recording Artist Credit
        ArtistCreditWrapper ac = artistCredits.get(id);
        if (ac != null) {
            ArtistCreditHelper.buildIndexFieldsOnlyFromArtistCredit
                    (doc,
                            ac.getArtistCredit(),
                            RecordingIndexField.ARTIST,
                            RecordingIndexField.ARTIST_NAMECREDIT,
                            RecordingIndexField.ARTIST_ID,
                            RecordingIndexField.ARTIST_NAME);
            recording.setArtistCredit(ac.getArtistCredit());
        } else {
            System.out.println("\nNo artist credit found for recording:" + rs.getString("trackid"));
        }

        if (tracks.containsKey(id)) {

            ReleaseList releaseList = of.createReleaseList();
            recording.setReleaseList(releaseList);

            // For each track that uses recording
            for (TrackWrapper trackWrapper : tracks.get(id)) {
                //Get the release details for this track
                Release origRelease = releases.get(trackWrapper.getReleaseId());


                if (origRelease != null) {
                    //This release instance will be shared by all recordings that have a track on the release so we need
                    //to copy details so we can append track specific details
                    Release release = of.createRelease();
                    release.setId(origRelease.getId());
                    release.setTitle(origRelease.getTitle());
                    MediumList ml = of.createMediumList();
                    release.setReleaseGroup(origRelease.getReleaseGroup());
                    release.setStatus(origRelease.getStatus());
                    ml.setTrackCount(origRelease.getMediumList().getTrackCount());
                    release.setMediumList(ml);
                    release.setReleaseEventList(origRelease.getReleaseEventList());
                    releaseList.getRelease().add(release);

                    ReleaseGroup rg = release.getReleaseGroup();
                    doc.addNonEmptyField(RecordingIndexField.TRACK_ID, trackWrapper.getTrackGuid());
                    String primaryType = rg.getPrimaryType();
                    doc.addFieldOrUnknown(RecordingIndexField.RELEASEGROUP_ID, rg.getId());
                    doc.addFieldOrUnknown(RecordingIndexField.RELEASE_PRIMARY_TYPE, primaryType);
                    if (
                            (rg.getSecondaryTypeList() != null) &&
                                    (rg.getSecondaryTypeList().getSecondaryType() != null)
                            ) {
                        for (String secondaryType : rg.getSecondaryTypeList().getSecondaryType()) {
                            doc.addField(RecordingIndexField.RELEASE_SECONDARY_TYPE, secondaryType);
                        }

                        String type = ReleaseGroupHelper.calculateOldTypeFromPrimaryType(primaryType,
                                rg.getSecondaryTypeList().getSecondaryType());
                        doc.addFieldOrNoValue(RecordingIndexField.RELEASE_TYPE, type);
                        rg.setType(type);
                    } else {
                        doc.addFieldOrNoValue(RecordingIndexField.RELEASE_TYPE, release.getReleaseGroup().getPrimaryType());
                        rg.setType(release.getReleaseGroup().getPrimaryType());
                    }

                    doc.addNumericField(RecordingIndexField.NUM_TRACKS, trackWrapper.getTrackCount());
                    doc.addNumericField(RecordingIndexField.TRACKNUM, trackWrapper.getTrackPosition());
                    doc.addFieldOrNoValue(RecordingIndexField.NUMBER, trackWrapper.getTrackNumber());
                    org.musicbrainz.mmd2.Medium.TrackList.Track track = of.createMediumTrackListTrack();
                    track.setTitle(trackWrapper.getTrackName());
                    track.setLength(BigInteger.valueOf(trackWrapper.getDuration()));
                    track.setNumber(trackWrapper.getTrackNumber());

                    Medium medium = of.createMedium();
                    medium.setPosition(BigInteger.valueOf(trackWrapper.getMediumPosition()));
                    medium.setFormat(trackWrapper.getMediumFormat());

                    Medium.TrackList tl  = of.createMediumTrackList();
                    tl.setCount(BigInteger.valueOf(trackWrapper.getTrackCount()));
                    tl.setOffset(BigInteger.valueOf(trackWrapper.getTrackPosition()  - 1));

                    release.getMediumList().getMedium().add(medium);
                    medium.setTrackList(tl);
                    tl.getDefTrack().add(track);
                    doc.addFieldOrNoValue(RecordingIndexField.RELEASE_STATUS, release.getStatus());

                    if (
                            (release.getReleaseEventList() != null) &&
                                    (release.getReleaseEventList().getReleaseEvent().size()>0)
                            ) {
                        for (ReleaseEvent re : release.getReleaseEventList().getReleaseEvent()) {
                            doc.addNonEmptyField(RecordingIndexField.RELEASE_DATE, re.getDate());
                            doc.addNonEmptyField(RecordingIndexField.COUNTRY, re.getCountry());
                        }
                        Collections.sort(release.getReleaseEventList().getReleaseEvent(), new ReleaseEventComparator());
                        ReleaseEvent firstReleaseEvent = release.getReleaseEventList().getReleaseEvent().get(0);
                        if (!Strings.isNullOrEmpty(firstReleaseEvent.getDate())) {
                            release.setDate(firstReleaseEvent.getDate());
                        }
                        if (!Strings.isNullOrEmpty(firstReleaseEvent.getCountry())) {
                            release.setCountry(firstReleaseEvent.getCountry());
                        }

                    } else {
                        doc.addFieldOrNoValue(RecordingIndexField.RELEASE_DATE, null);
                        doc.addFieldOrNoValue(RecordingIndexField.COUNTRY, null);
                    }


                    doc.addField(RecordingIndexField.RELEASE_ID, release.getId());
                    doc.addField(RecordingIndexField.RELEASE, release.getTitle());
                    doc.addNumericField(RecordingIndexField.NUM_TRACKS_RELEASE, release.getMediumList().getTrackCount().intValue());


                    //Is Various Artist Release
                    if (release.getArtistCredit() != null) {
                        doc.addField(RecordingIndexField.RELEASE_AC_VA, "1");
                    } else {
                        doc.addField(RecordingIndexField.RELEASE_AC_VA, Index.NO_VALUE);
                    }
                    trackNames.add(trackWrapper.getTrackName().toLowerCase(Locale.UK));
                    doc.addField(RecordingIndexField.POSITION, String.valueOf(trackWrapper.getMediumPosition()));
                    doc.addFieldOrNoValue(RecordingIndexField.FORMAT, trackWrapper.getMediumFormat());

                    //Get Artist Credit for Track
                    ArtistCreditWrapper taw = trackArtistCredits.get(trackWrapper.getTrackId());
                    //If different to the Artist Credit for the recording
                    if (taw != null &&
                            (
                                    (ac == null) ||
                                            (taw.getArtistCreditId() != ac.getArtistCreditId())
                            )
                            ) {
                        ArtistCreditHelper.buildIndexFieldsOnlyFromArtistCredit
                                (doc,
                                        taw.getArtistCredit(),
                                        RecordingIndexField.ARTIST,
                                        RecordingIndexField.ARTIST_NAMECREDIT,
                                        RecordingIndexField.ARTIST_ID,
                                        RecordingIndexField.ARTIST_NAME);
                        track.setArtistCredit(ac.getArtistCredit());
                    }
                }
            }
        } else {
            doc.addFieldOrNoValue(RecordingIndexField.RELEASE_TYPE, "standalone");
        }

        if (tags.containsKey(id)) {
            TagList tagList = of.createTagList();
            for (Tag nextTag : tags.get(id)) {
                Tag tag = of.createTag();
                doc.addField(RecordingIndexField.TAG, nextTag.getName());
                tag.setName(nextTag.getName());
                tag.setCount(new BigInteger(nextTag.getCount().toString()));
                tagList.getTag().add(tag);
            }
            recording.setTagList(tagList);
        }

        //If we have no recording length in the recording itself or the track length then we add this value so
        //they can search for recordings/tracks with no length
        if (durations.size() == 0) {
            doc.addField(RecordingIndexField.DURATION, Index.NO_VALUE);
            doc.addField(RecordingIndexField.QUANTIZED_DURATION, Index.NO_VALUE);
        } else {
            for (Integer dur : durations) {
                doc.addNumericField(RecordingIndexField.DURATION, dur);
                qdurs.add(dur / QUANTIZED_DURATION);
            }

            for (Integer qdur : qdurs) {
                doc.addNumericField(RecordingIndexField.QUANTIZED_DURATION, qdur);
            }

        }

        //Allow searching of all unique recording/track names
        for (String next : trackNames) {
            doc.addNonEmptyField(RecordingIndexField.RECORDING, next);
        }

        buildClock.suspend();
        storeClock.resume();
        doc.addField(RecordingIndexField.RECORDING_STORE, MMDSerializer.serialize(recording));
        storeClock.suspend();
        return doc.getLuceneDocument();
    }

    /**
     * Create various artist credits
     *
     * @return
     */
    private ArtistCredit createVariousArtistsCredit()
    {
        ObjectFactory of = new ObjectFactory();
        Artist       artist   = of.createArtist();
        artist.setId(VARIOUS_ARTISTS_GUID);
        artist.setName(VARIOUS_ARTISTS_NAME);
        artist.setSortName(VARIOUS_ARTISTS_NAME);
        NameCredit   naCredit = of.createNameCredit();
        naCredit.setArtist(artist);
        ArtistCredit vaCredit = of.createArtistCredit();
        vaCredit.getNameCredit().add(naCredit);
        return vaCredit;
    }

}
