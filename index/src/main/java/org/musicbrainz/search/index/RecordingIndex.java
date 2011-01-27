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
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.*;
import java.util.*;

public class RecordingIndex extends DatabaseIndex {

    public static final String INDEX_NAME = "recording";

    private StopWatch isrcClock = new StopWatch();
    private StopWatch puidClock = new StopWatch();
    private StopWatch artistClock = new StopWatch();
    private StopWatch recordingClock = new StopWatch();

    private String    cacheType;

    private final static int QUANTIZED_DURATION = 2000;

    public RecordingIndex(Connection dbConnection, String cacheType) {
        super(dbConnection);
        this.cacheType=cacheType;

        isrcClock.start();
        puidClock.start();
        artistClock.start();
        recordingClock.start();
        isrcClock.suspend();
        puidClock.suspend();
        artistClock.suspend();
        recordingClock.suspend();
    }

    public RecordingIndex() {
    }

    public String getName() {
        return RecordingIndex.INDEX_NAME;
    }

    public Analyzer getAnalyzer() {
        return new PerFieldEntityAnalyzer(RecordingIndexField.class);
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
            if(cacheType.equals(CacheType.TEMPTABLE))  {
                addPreparedStatement("PUIDS",
                     "SELECT recording as recordingId, puid " +
                     " FROM  tmp_release_puid " +
                     " WHERE recording between ? AND ?");

            }
            else if(cacheType.equals(CacheType.NONE))  {
                addPreparedStatement("PUIDS",
                     "SELECT recording as recordingId, puid.puid " +
                     " FROM recording_puid " +
                     " INNER JOIN puid ON recording_puid.puid = puid.id " +
                     " AND   recording between ? AND ?");
            }
        }
        else {
             addPreparedStatement("PUIDS",
                  "SELECT recording as recordingId, puid.puid " +
                  " FROM recording_puid " +
                  " INNER JOIN puid ON recording_puid.puid = puid.id " +
                  " AND   recording between ? AND ?");
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
                "  a.pos, " +
                "  a.joinphrase, " +
                "  a.artistId,  " +
                "  a.comment, " +
                "  a.artistName, " +
                "  a.artistCreditName, " +
                "  a.artistSortName " +
                " FROM recording AS r " +
                "  INNER JOIN tmp_artistcredit a ON r.artist_credit=a.artist_credit " +
                " WHERE r.id BETWEEN ? AND ?  " +
                " ORDER BY r.id, a.pos");

        addPreparedStatement("RECORDINGS",
                "SELECT re.id as recordingId, re.gid as trackid, re.length as duration, tn.name as trackname, " +
                "  tn2.name as track_name, t.position as track_position, tl.track_count, " +
                "  m.position as medium_position, " +
                "  r.id as releaseKey, r.gid as releaseid, r.name as releasename, r.type, "+
                "  r.status, r.date_year, r.date_month, r.date_day, tracks" +
                " FROM recording re " +
                "  INNER JOIN track_name tn ON re.name=tn.id " +
                "  INNER JOIN track t on t.recording=re.id " +
                "  INNER JOIN track_name tn2 ON t.name=tn2.id " +
                "  INNER JOIN tracklist tl ON t.tracklist=tl.id " +
                "  INNER JOIN medium m ON m.tracklist=tl.id " +
                "  INNER  JOIN tmp_release r ON m.release=r.id " +
                " WHERE re.id BETWEEN ? AND ? " +
                "  ORDER BY re.id");
    }

    public void destroy() throws SQLException {

        super.destroy();
        System.out.println(" Isrcs Queries " + Float.toString(isrcClock.getTime()/1000) + " seconds");
        System.out.println(" Artists Queries " + Float.toString(artistClock.getTime()/1000) + " seconds");
        System.out.println(" Puids Queries " + Float.toString(puidClock.getTime()/1000) + " seconds");
        System.out.println(" Recording Queries " + Float.toString(recordingClock.getTime()/1000) + " seconds");
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
     * Get Artist Information for the recordings
     *
     * @param min min recording id
     * @param max max recoridng id
     * @return
     * @throws SQLException if sql problem
     * @throws IOException  if io exception
     * @return A map of matches
     */
    private Map<Integer, ArtistCredit> loadArtists(int min, int max) throws SQLException, IOException {

        //Artists
        artistClock.resume();
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
        rs.close();
        artistClock.suspend();
        return artistCredits;
    }

    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {

        MbDocument      doc      = null;
        int             lastId   = -1;
        String          recordingName = null;
        Set<String>     trackNames = new HashSet<String>();

        Map<Integer, List<Tag>> tags = loadTags(min, max);
        Map<Integer, List<String>> puids = loadPUIDs(min, max);
        Map<Integer, List<String>> isrcs = loadISRCs(min, max);
        Map<Integer, ArtistCredit> artistCredits = loadArtists(min, max);

        PreparedStatement st = getPreparedStatement("RECORDINGS");
        st.setInt(1, min);
        st.setInt(2, max);
        recordingClock.resume();
        ResultSet rs = st.executeQuery();

        while(rs.next())
        {
            int id = rs.getInt("recordingId");
            if(id!=lastId)
            {
                //Check this isnt first result, and if not add the last one
                if(doc!=null)
                {
                    indexWriter.addDocument(doc.getLuceneDocument());
                }

                //New Recording
                doc = new MbDocument();
                lastId = id;
                trackNames = new HashSet<String>();

                doc.addField(RecordingIndexField.ID, id);
                doc.addField(RecordingIndexField.RECORDING_ID, rs.getString("trackid"));
                recordingName = rs.getString("trackname");
                trackNames.add(recordingName);
                doc.addNonEmptyField(RecordingIndexField.RECORDING, recordingName);         //Search
                doc.addNonEmptyField(RecordingIndexField.RECORDING_OUTPUT, recordingName);  //Output
                doc.addNumericField(RecordingIndexField.DURATION, rs.getInt("duration"));
                doc.addNumericField(RecordingIndexField.QUANTIZED_DURATION, rs.getInt("duration") / QUANTIZED_DURATION);

                // Add each puid for recording
                if (puids.containsKey(id)) {
                    for (String puid : puids.get(id)) {
                        doc.addField(RecordingIndexField.PUID, puid);
                    }
                }

                // For each credit artist for this recording
                if (isrcs.containsKey(id)) {
                    for (String isrc : isrcs.get(id)) {
                        doc.addField(RecordingIndexField.ISRC, isrc);
                    }
                }

                //Artist Credit
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
                        doc.addField(RecordingIndexField.TAG, tag.getName());
                        doc.addField(RecordingIndexField.TAGCOUNT, tag.getCount().toString());
                    }
                }
            }

            //-- A recording can link to multiple tracks and those tracks are within tracklists
            //-- than can be used by multiple mediums and hence releases.
            //-- Each track/release combo will be an additional result set returned that needs processing

            //Track Info
            doc.addNumericField(RecordingIndexField.NUM_TRACKS, rs.getInt("track_count"));
            doc.addNumericField(RecordingIndexField.TRACKNUM, rs.getInt("track_position"));
            // Added to TRACK_OUTPUT for outputting xml,
            String trackName = rs.getString("track_name");
            doc.addField(RecordingIndexField.TRACK_OUTPUT, trackName);
            // and if different to recoridng/other tracks names add for searching
            if(!trackNames.contains(trackName)) {
                doc.addField(RecordingIndexField.RECORDING, trackName);
            }
            doc.addField(RecordingIndexField.POSITION, String.valueOf(rs.getInt("medium_position")));

            //Release Info
            doc.addFieldOrHyphen(RecordingIndexField.RELEASE_TYPE, rs.getString("type"));
            doc.addFieldOrHyphen(RecordingIndexField.RELEASE_STATUS, rs.getString("status"));
            doc.addFieldOrHyphen(RecordingIndexField.RELEASE_DATE, Utils.formatDate(rs.getInt("date_year"), rs.getInt("date_month"), rs.getInt("date_day")));
            doc.addField(RecordingIndexField.RELEASE_ID, rs.getString("releaseid"));
            doc.addField(RecordingIndexField.RELEASE, rs.getString("releasename"));
            doc.addNumericField(RecordingIndexField.NUM_TRACKS_RELEASE, rs.getInt("tracks"));
        }

        //Add the last recording
        if(doc!=null)
        {
            indexWriter.addDocument(doc.getLuceneDocument());
        }
        rs.close();
        recordingClock.suspend();
    }

}
