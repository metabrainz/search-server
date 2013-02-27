/* Copyright (c) 2009 Aurélien Mino
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the MusicBrainz project nor the names of the
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.musicbrainz.search.index;

import org.apache.commons.lang.time.StopWatch;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.musicbrainz.search.MbDocument;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class  ReleaseIndex extends DatabaseIndex {


    public  static final String BARCODE_NONE = "none";
    private StopWatch labelClock = new StopWatch();
    private StopWatch mediumClock = new StopWatch();
    private StopWatch puidClock = new StopWatch();
    private StopWatch artistClock = new StopWatch();
    private StopWatch releaseClock = new StopWatch();

    public static final String INDEX_NAME = "release";

    public ReleaseIndex(Connection dbConnection) {
        super(dbConnection);
        labelClock.start();
        mediumClock.start();
        puidClock.start();
        artistClock.start();
        releaseClock.start();
        labelClock.suspend();
        mediumClock.suspend();
        puidClock.suspend();
        artistClock.suspend();
        releaseClock.suspend();
    }

    public ReleaseIndex() {
    }

    public Analyzer getAnalyzer() {
        return DatabaseIndex.getAnalyzer(ReleaseIndexField.class);
    }

    public String getName() {
        return ReleaseIndex.INDEX_NAME;
    }

	@Override
	public IndexField getIdentifierField() {
		return ReleaseIndexField.ID;
	}

    public int getMaxId() throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM release");
        rs.next();
        return rs.getInt(1);
    }

    public int getNoOfRows(int maxId) throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT count(*) FROM release WHERE id<="+maxId);
        rs.next();
        return rs.getInt(1);
    }

    @Override
    public void init(IndexWriter indexWriter, boolean isUpdater) throws SQLException {

        if(!isUpdater) {
           addPreparedStatement("PUIDS",
                "SELECT release, puid " +
                "FROM   tmp_release_puid " +
                "WHERE  release BETWEEN ? AND ? ");
        }
        else {
            addPreparedStatement("PUIDS",
                "SELECT m.release, p.puid " +
                "FROM medium m " +
                " INNER JOIN track t ON (t.tracklist=m.tracklist AND m.release BETWEEN ? AND ?) " +
                " INNER JOIN recording_puid rp ON rp.recording = t.recording " +
                " INNER JOIN puid p ON rp.puid=p.id");
        }

        addPreparedStatement("LABELINFOS",
               "SELECT rl.release as releaseId, l.gid as labelId, ln.name as labelName, catalog_number " +
               " FROM release_label rl " +
               "  LEFT JOIN label l ON rl.label=l.id " +
               "  LEFT JOIN label_name ln ON l.name = ln.id " +
               " WHERE rl.release BETWEEN ? AND ?");

        addPreparedStatement("MEDIUMS",
              "SELECT m.release as releaseId, mf.name as format, tr.track_count as numTracksOnMedium, count(mc.id) as discidsOnMedium " +
              " FROM medium m " +
              "  LEFT JOIN medium_format mf ON m.format=mf.id " +
              "  LEFT JOIN tracklist tr ON m.tracklist=tr.id " +
              "  LEFT JOIN medium_cdtoc mc ON mc.medium=m.id "  +
              " WHERE m.release BETWEEN ? AND ? " +
              " GROUP BY m.release, m.position, m.id, mf.name, tr.track_count" +
              " ORDER BY m.release, m.position, m.id "
        );

        addPreparedStatement("ARTISTCREDITS",
                "SELECT r.id as releaseId, " +
                "  a.artist_credit, " +
                "  a.pos, " +
                "  a.joinphrase, " +
                "  a.artistId,  " +
                "  a.comment, " +
                "  a.artistName, " +
                "  a.artistCreditName, " +
                "  a.artistSortName, " +
                "  a.aliasName " +
                " FROM release AS r " +
                "  INNER JOIN tmp_artistcredit a ON r.artist_credit=a.artist_credit " +
                " WHERE r.id BETWEEN ? AND ?  " +
                " ORDER BY r.id, a.pos");

        addPreparedStatement("SECONDARYTYPES",
                "SELECT rg.name as type, r.id as rid" +
                " FROM tmp_release r " +
                " INNER JOIN release_group_secondary_type_join  rgj " +
                " ON r.rg_id=rgj.release_group " +
                " INNER JOIN release_group_secondary_type rg  " +
                " ON rgj.secondary_type = rg.id " +
                " WHERE r.id BETWEEN ? AND ?");

        addPreparedStatement("RELEASES",
                " SELECT id, gid, name, " +
                "  barcode, country, " +
                "  date_year, date_month, date_day, type, rg_gid, amazon_asin, " +
                "  language, language_2t, script, status, comment " +
                " FROM tmp_release rl " +
                " WHERE id BETWEEN ? AND ? ");


    }


    public void destroy() throws SQLException {
        try
        {
            super.destroy();
            System.out.println(this.getName()+":Label Queries "    + Utils.formatClock(labelClock));
            System.out.println(this.getName()+":Mediums Queries "  + Utils.formatClock(mediumClock));
            System.out.println(this.getName()+":Artists Queries "  + Utils.formatClock(artistClock));
            System.out.println(this.getName()+":Puids Queries "    + Utils.formatClock(puidClock));
            System.out.println(this.getName()+":Releases Queries " + Utils.formatClock(releaseClock));

        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }


    /**
     * Load work iswcs
     *
     * @param min
     * @param max
     * @return
     * @throws SQLException
     * @throws IOException
     */
    private Map<Integer, List<String>> loadSecondaryTypes(int min, int max) throws SQLException, IOException {
        Map<Integer, List<String>> secondaryTypes = new HashMap<Integer, List<String>>();
        PreparedStatement st = getPreparedStatement("SECONDARYTYPES");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int releaseId = rs.getInt("rid");

            List<String> list;
            if (!secondaryTypes.containsKey(releaseId)) {
                list = new LinkedList<String>();
                secondaryTypes.put(releaseId, list);
            } else {
                list = secondaryTypes.get(releaseId);
            }
            list.add(rs.getString("type"));
        }
        rs.close();
        return secondaryTypes;
    }


    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {

        //A particular release can have multiple catalog nos, labels when released as an imprint, typically used
        //by major labels
        labelClock.resume();
        Map<Integer, List<List<String>>> labelInfo = new HashMap<Integer, List<List<String>>>();
        PreparedStatement st = getPreparedStatement("LABELINFOS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int releaseId = rs.getInt("releaseId");
            List<List<String>> list;
            if (!labelInfo.containsKey(releaseId)) {
                list = new LinkedList<List<String>>();
                labelInfo.put(releaseId, list);
            } else {
                list = labelInfo.get(releaseId);
            }
            List<String> entry = new ArrayList<String>(3);
            entry.add(rs.getString("labelId"));
            entry.add(rs.getString("labelName"));
            entry.add(rs.getString("catalog_number"));
            list.add(entry);
        }
        rs.close();
        labelClock.suspend();


        //Medium, NumTracks a release can be released on multiple mediums, and possibly involving different mediums,
        //i.e a release is on CD with
        //a special 7" single included. We also need total tracks and discs ids per medium
        mediumClock.resume();
        Map<Integer, List<List<String>>> mediums = new HashMap<Integer, List<List<String>>>();
        st = getPreparedStatement("MEDIUMS");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            int releaseId = rs.getInt("releaseId");
            List<List<String>> list;
            if (!mediums.containsKey(releaseId)) {
                list = new LinkedList<List<String>>();
                mediums.put(releaseId, list);
            } else {
                list = mediums.get(releaseId);
            }
            List<String> entry = new ArrayList<String>(3);
            entry.add(rs.getString("format"));
            entry.add(String.valueOf(rs.getInt("numTracksOnMedium")));
            entry.add(String.valueOf(rs.getInt("discIdsOnMedium")));
            list.add(entry);
        }
        rs.close();
        mediumClock.suspend();


        //Puids
        Map<Integer, List<String>> puidWrapper = new HashMap<Integer, List<String>>();
        puidClock.resume();
        st = getPreparedStatement("PUIDS");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            int releaseId = rs.getInt("release");
            List<String> list;
            if (!puidWrapper.containsKey(releaseId)) {
                list = new LinkedList<String>();
                puidWrapper.put(releaseId, list);
            } else {
                list = puidWrapper.get(releaseId);
            }
            String puid = new String(rs.getString("puid"));
            list.add(puid);
        }
        rs.close();
        puidClock.suspend();



        //Artist Credits
        artistClock.resume();
        st = getPreparedStatement("ARTISTCREDITS");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        Map<Integer, ArtistCreditWrapper> artistCredits
                = ArtistCreditHelper.completeArtistCreditFromDbResults
                     (rs,
                      "releaseId",
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

        Map<Integer, List<String>> secondaryTypes = loadSecondaryTypes(min, max);
        st = getPreparedStatement("RELEASES");
        st.setInt(1, min);
        st.setInt(2, max);
        releaseClock.resume();
        rs = st.executeQuery();
        releaseClock.suspend();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, secondaryTypes, labelInfo, mediums, puidWrapper, artistCredits));
        }
        rs.close();
    }

    public Document documentFromResultSet(ResultSet rs,
                                          Map<Integer, List<String>> secondaryTypes,
                                          Map<Integer,List<List<String>>> labelInfo,
                                          Map<Integer,List<List<String>>> mediums,
                                          Map<Integer, List<String>> puids,
                                          Map<Integer, ArtistCreditWrapper> artistCredits) throws SQLException {
        MbDocument doc = new MbDocument();
        int id = rs.getInt("id");
        doc.addField(ReleaseIndexField.ID, id);
        doc.addField(ReleaseIndexField.RELEASE_ID, rs.getString("gid"));
        String name = rs.getString("name");
        doc.addField(ReleaseIndexField.RELEASE, name );
        doc.addField(ReleaseIndexField.RELEASE_ACCENT, name);

        String primaryType = rs.getString("type");
        doc.addFieldOrUnknown(ReleaseIndexField.PRIMARY_TYPE, primaryType);
        if (secondaryTypes.containsKey(id)) {
            for (String secondaryType : secondaryTypes.get(id)) {
                doc.addField(ReleaseIndexField.SECONDARY_TYPE, secondaryType);
            }
        }
        String type = ReleaseGroupHelper.calculateOldTypeFromPrimaryType(primaryType, secondaryTypes.get(id));
        doc.addFieldOrUnknown(ReleaseIndexField.TYPE, type);


        doc.addNonEmptyField(ReleaseIndexField.RELEASEGROUP_ID, rs.getString("rg_gid"));
        doc.addFieldOrUnknown(ReleaseIndexField.STATUS, rs.getString("status"));

        doc.addFieldOrUnknown(ReleaseIndexField.COUNTRY, rs.getString("country"));
        doc.addNonEmptyField(ReleaseIndexField.DATE,
                Utils.formatDate(rs.getInt("date_year"), rs.getInt("date_month"), rs.getInt("date_day")));

        String barcode = rs.getString("barcode");
        if(barcode==null) {
            doc.addField(ReleaseIndexField.BARCODE,Index.NO_VALUE);
        }
        else if(barcode.equals("")) {
            doc.addField(ReleaseIndexField.BARCODE, BARCODE_NONE);
        }
        else {
            doc.addField(ReleaseIndexField.BARCODE,barcode);
        }
        doc.addFieldOrNoValue(ReleaseIndexField.AMAZON_ID, rs.getString("amazon_asin"));

        String lang3= rs.getString("language");
        String lang2= rs.getString("language_2t");
        if(lang3!=null)
        {
            doc.addFieldOrUnknown(ReleaseIndexField.LANGUAGE, lang3);
        }
        else
        {
            doc.addFieldOrUnknown(ReleaseIndexField.LANGUAGE, lang2);
        }
        doc.addFieldOrUnknown(ReleaseIndexField.SCRIPT, rs.getString("script"));
        doc.addFieldOrNoValue(ReleaseIndexField.COMMENT, rs.getString("comment"));

        if (labelInfo.containsKey(id)) {
            for (List<String> entry : labelInfo.get(id)) {
                doc.addFieldOrNoValue(ReleaseIndexField.LABEL_ID, entry.get(0));
                doc.addFieldOrNoValue(ReleaseIndexField.LABEL, entry.get(1));
                doc.addFieldOrUnknown(ReleaseIndexField.CATALOG_NO, entry.get(2));
            }
        }

        int trackCount = 0;
        int discCount = 0;
        int mediumCount = 0;
        if (mediums.containsKey(id)) {
            for (List<String> entry : mediums.get(id)) {
                String str;
                str = entry.get(0);
                doc.addFieldOrNoValue(ReleaseIndexField.FORMAT, str);
                int numTracksOnMedium = Integer.parseInt(entry.get(1));
                doc.addNumericField(ReleaseIndexField.NUM_TRACKS_MEDIUM, numTracksOnMedium);
                trackCount += numTracksOnMedium;

                int numDiscsOnMedium = Integer.parseInt(entry.get(2));
                doc.addNumericField(ReleaseIndexField.NUM_DISCIDS_MEDIUM, numDiscsOnMedium);
                discCount += numDiscsOnMedium;
                mediumCount++;
            }
            //Num of mediums on the release
            doc.addNumericField(ReleaseIndexField.NUM_MEDIUMS, mediumCount);

            //Num Tracks over the whole release
            doc.addNumericField(ReleaseIndexField.NUM_TRACKS, trackCount);

            //Num Discs over the whole release
            doc.addNumericField(ReleaseIndexField.NUM_DISCIDS, discCount);

        }
        else
        {
            //No mediums on release
            doc.addNumericField(ReleaseIndexField.NUM_MEDIUMS, 0);

        }


        if (puids.containsKey(id)) {
            for (String puid : puids.get(id)) {
                 doc.addField(ReleaseIndexField.PUID, puid);
            }
        }


        ArtistCreditWrapper ac = artistCredits.get(id);
        if(ac!=null) {
            ArtistCreditHelper.buildIndexFieldsFromArtistCredit
                   (doc,
                    ac.getArtistCredit(),
                    ReleaseIndexField.ARTIST,
                    ReleaseIndexField.ARTIST_NAMECREDIT,
                    ReleaseIndexField.ARTIST_ID,
                    ReleaseIndexField.ARTIST_NAME,
                    ReleaseIndexField.ARTIST_CREDIT);
        }
        else {
            System.out.println("\nNo artist credit found for release:"+rs.getString("gid"));
        }

        return doc.getLuceneDocument();
    }

}
