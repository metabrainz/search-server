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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.musicbrainz.mmd2.ArtistCredit;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class ReleaseIndex extends DatabaseIndex {


    public ReleaseIndex(Connection dbConnection) {
        super(dbConnection);
    }

    public ReleaseIndex() {
    }

    public Analyzer getAnalyzer() {
        return new PerFieldEntityAnalyzer(ReleaseIndexField.class);
    }

    public String getName() {
        return "release";
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
    public void init(IndexWriter indexWriter) throws SQLException {
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
              " GROUP BY m.release, m.id, mf.name, tr.track_count");

         addPreparedStatement("ARTISTCREDITS",
                "SELECT r.id as releaseId, " +
                "  acn.position as pos, " +
                "  acn.join_phrase as joinphrase, " +
                "  a.gid as artistId,  " +
                "  a.comment as comment, " +
                "  an.name as artistName, " +
                "  an2.name as artistCreditName, " +
                "  an3.name as artistSortName " +
                " FROM release AS r " +
                "  INNER JOIN artist_credit_name acn ON r.artist_credit=acn.artist_credit " +
                "  INNER JOIN artist a ON a.id=acn.artist " +
                "  INNER JOIN artist_name an ON a.name=an.id " +
                "  INNER JOIN artist_name an2 ON acn.name=an2.id " +
                "  INNER JOIN artist_name an3 ON a.sort_name=an3.id " +
                " WHERE r.id BETWEEN ? AND ?  " +
                " ORDER BY r.id, acn.position");

         addPreparedStatement("RELEASES",
                "SELECT rl.id, rl.gid, rn.name as name, " +
                "  barcode, lower(country.iso_code) as country, " +
                "  date_year, date_month, date_day, rgt.name as type, rm.amazon_asin, " +
                "  language.iso_code_3t as language, script.iso_code as script, rs.name as status " +
                " FROM release rl " +
                "  INNER JOIN release_meta rm ON rl.id = rm.id " +
                "  INNER JOIN release_group rg ON rg.id = rl.release_group " +
                "  LEFT JOIN release_group_type rgt  ON rg.type = rgt.id " +
                "  LEFT JOIN country ON rl.country=country.id " +
                "  LEFT JOIN release_name rn ON rl.name = rn.id " +
                "  LEFT JOIN release_status rs ON rl.status = rs.id " +
                "  LEFT JOIN language ON rl.language=language.id " +
                "  LEFT JOIN script ON rl.script=script.id " +
                " WHERE rl.id BETWEEN ? AND ?");


        addPreparedStatement("PUIDS",
                "SELECT m.release, p.puid " +
                "FROM medium m " +
                " INNER JOIN track t ON (t.tracklist=m.tracklist and m.release between ? AND ?) " +
                " INNER JOIN recording_puid rp ON rp.recording = t.recording " +
                " INNER JOIN puid p ON rp.puid=p.id");
    }

    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {

        //A particular release can have multiple catalog nos, labels when released as an imprint, typically used
        //by major labels
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


        //Medium, NumTracks a release can be released on multiple mediums, and possibly involving different mediums,
        //i.e a release is on CD with
        //a special 7" single included. We also need total tracks and discs ids per medium
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


        //Puids
        Map<Integer, List<String>> puidWrapper = new HashMap<Integer, List<String>>();
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

        //Artist Credits
        st = getPreparedStatement("ARTISTCREDITS");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        Map<Integer, ArtistCredit> artistCredits
                = ArtistCreditHelper.completeArtistCreditFromDbResults
                     (rs,
                      "releaseId",
                      "artistId",
                      "artistName",
                      "artistSortName",
                      "comment",
                      "joinphrase",
                      "artistCreditName");

        st = getPreparedStatement("RELEASES");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, labelInfo, mediums, puidWrapper, artistCredits));
        }
    }

    public Document documentFromResultSet(ResultSet rs,
                                          Map<Integer,List<List<String>>> labelInfo,
                                          Map<Integer,List<List<String>>> mediums,
                                          Map<Integer, List<String>> puids,
                                          Map<Integer, ArtistCredit> artistCredits) throws SQLException {
        MbDocument doc = new MbDocument();
        int id = rs.getInt("id");
        doc.addField(ReleaseIndexField.ID, id);
        doc.addField(ReleaseIndexField.RELEASE_ID, rs.getString("gid"));
        doc.addField(ReleaseIndexField.RELEASE, rs.getString("name"));
        doc.addNonEmptyField(ReleaseIndexField.TYPE, rs.getString("type"));
        doc.addNonEmptyField(ReleaseIndexField.STATUS, rs.getString("status"));
        doc.addNonEmptyField(ReleaseIndexField.COUNTRY, rs.getString("country"));
        doc.addNonEmptyField(ReleaseIndexField.DATE,
                Utils.formatDate(rs.getInt("date_year"), rs.getInt("date_month"), rs.getInt("date_day")));
        doc.addNonEmptyField(ReleaseIndexField.BARCODE, rs.getString("barcode"));
        doc.addNonEmptyField(ReleaseIndexField.AMAZON_ID, rs.getString("amazon_asin"));
        doc.addNonEmptyField(ReleaseIndexField.LANGUAGE, rs.getString("language"));
        doc.addNonEmptyField(ReleaseIndexField.SCRIPT, rs.getString("script"));

        if (labelInfo.containsKey(id)) {
            for (List<String> entry : labelInfo.get(id)) {
                doc.addFieldOrHyphen(ReleaseIndexField.LABEL_ID, entry.get(0));
                doc.addFieldOrHyphen(ReleaseIndexField.LABEL, entry.get(1));
                doc.addFieldOrHyphen(ReleaseIndexField.CATALOG_NO, entry.get(2));
            }
        }

        int trackCount = 0;
        int discCount = 0;
        int mediumCount = 0;
        if (mediums.containsKey(id)) {
            for (List<String> entry : mediums.get(id)) {
                String str;
                str = entry.get(0);
                doc.addFieldOrHyphen(ReleaseIndexField.FORMAT, str);
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

        if (puids.containsKey(id)) {
            for (String puid : puids.get(id)) {
                 doc.addField(ReleaseIndexField.PUID, puid);
            }
        }

        ArtistCredit ac = artistCredits.get(id);
        ArtistCreditHelper.buildIndexFieldsFromArtistCredit
               (doc,
                ac,
                ReleaseIndexField.ARTIST,
                ReleaseIndexField.ARTIST_NAMECREDIT,
                ReleaseIndexField.ARTIST_ID,
                ReleaseIndexField.ARTIST_NAME,
                ReleaseIndexField.ARTIST_CREDIT);
        
        return doc.getLuceneDocument();
    }

}
