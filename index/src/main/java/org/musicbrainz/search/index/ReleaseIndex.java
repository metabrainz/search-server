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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class ReleaseIndex extends DatabaseIndex {


    public ReleaseIndex(Connection dbConnection) {
        super(dbConnection);
    }

    public Analyzer getAnalyzer() {
        return new PerFieldEntityAnalyzer(ReleaseIndexField.class);
    }

    public String getName() {
        return "release";
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
    public void init() throws SQLException {
       addPreparedStatement("LABELINFOS",
               "SELECT rl.release as releaseId, ln.name as label, catno " +
                "FROM release_label rl " +
                "LEFT JOIN label l ON rl.label=l.id " +
                "LEFT JOIN label_name ln ON l.name = ln.id " +
                "WHERE rl.release BETWEEN ? AND ?");

        addPreparedStatement("MEDIUMS",
              "SELECT m.release as releaseId, mf.name as format,tr.trackcount as numTracksOnMedium, count(mc.id) as discidsOnMedium " +
                "FROM medium m " +
                "LEFT JOIN medium_format mf ON m.format=mf.id " +
                "LEFT JOIN tracklist tr ON m.tracklist=tr.id " +
                "LEFT JOIN medium_cdtoc mc ON mc.medium=m.id "  +
                "WHERE m.release BETWEEN ? AND ? " +
                "GROUP BY m.release,m.id,mf.name,tr.trackcount");

         addPreparedStatement("ARTISTS",
                "SELECT r.id as releaseId, " +
                "acn.position as pos, " +
                "acn.joinphrase as joinphrase, " +
                "a.gid as artistId,  " +
                "a.comment as comment, " +
                "an.name as artistName, " +
                "an2.name as artistCreditName, " +
                "an3.name as artistSortName " +
                "FROM release AS r " +
                "INNER JOIN artist_credit_name acn ON r.artist_credit=acn.artist_credit " +
                "INNER JOIN artist a ON a.id=acn.artist " +
                "INNER JOIN artist_name an on a.name=an.id " +
                "INNER JOIN artist_name an2 on acn.name=an2.id " +
                "INNER JOIN artist_name an3 on a.sortname=an3.id " +
                "WHERE r.id BETWEEN ? AND ?  " +
                "order by r.id,acn.position ");

         addPreparedStatement("RELEASES",
                "SELECT rl.id, rl.gid, rn.name as name, " +
                "barcode,lower(country.isocode) as country, " +
                "date_year, date_month, date_day,rgt.name as type,rm.amazonasin, " +
                "language.isocode_3t as language, script.isocode as script,rs.name as status " +
                "FROM release rl " +
                "INNER JOIN release_meta rm ON rl.id = rm.id " +
                "INNER JOIN release_group rg ON rg.id = rl.release_group " +
                "LEFT JOIN release_group_type rgt  ON rg.type = rgt.id " +
                "LEFT JOIN country ON rl.country=country.id " +
                "LEFT JOIN release_name rn ON rl.name = rn.id " +
                "LEFT JOIN release_status rs ON rl.status = rs.id " +
                "LEFT JOIN language ON rl.language=language.id " +
                "LEFT JOIN script ON rl.script=script.id " +
                "WHERE rl.id BETWEEN ? AND ?");
    }

    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {

        //A particular release can have multiple catalognos, labels when released as an imprint, typically used
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
            List<String> entry = new ArrayList<String>(2);
            entry.add(rs.getString("label"));
            entry.add(rs.getString("catno"));
            list.add(entry);
        }


        //Medium, NumTracks a release can be released on multiple mediums, and possibly involving mediums, i.e a release is on CD with
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


        //Artists
        Map<Integer, List<ArtistWrapper>> artists = new HashMap<Integer, List<ArtistWrapper>>();
        st = getPreparedStatement("ARTISTS");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            int releaseGroupId = rs.getInt("releaseId");
            List<ArtistWrapper> list;
            if (!artists.containsKey(releaseGroupId)) {
                list = new LinkedList<ArtistWrapper>();
                artists.put(releaseGroupId, list);
            } else {
                list = artists.get(releaseGroupId);
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

        st = getPreparedStatement("RELEASES");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, labelInfo, mediums,artists));
        }
    }

    public Document documentFromResultSet(ResultSet rs,
                                          Map<Integer,List<List<String>>> labelInfo,
                                          Map<Integer,List<List<String>>> mediums,
                                          Map<Integer, List<ArtistWrapper>> artists) throws SQLException {
        Document doc = new Document();
        int id = rs.getInt("id");
        addFieldToDocument(doc, ReleaseIndexField.RELEASE_ID, rs.getString("gid"));
        addFieldToDocument(doc, ReleaseIndexField.RELEASE, rs.getString("name"));
        addNonEmptyFieldToDocument(doc, ReleaseIndexField.TYPE, rs.getString("type"));
        addNonEmptyFieldToDocument(doc, ReleaseIndexField.STATUS, rs.getString("status"));
        addNonEmptyFieldToDocument(doc, ReleaseIndexField.COUNTRY, rs.getString("country"));
        addNonEmptyFieldToDocument(doc, ReleaseIndexField.DATE,
                Utils.formatDate(rs.getInt("date_year"), rs.getInt("date_month"), rs.getInt("date_day")));
        addNonEmptyFieldToDocument(doc, ReleaseIndexField.BARCODE, rs.getString("barcode"));
        addNonEmptyFieldToDocument(doc, ReleaseIndexField.AMAZON_ID, rs.getString("amazonasin"));
        addNonEmptyFieldToDocument(doc, ReleaseIndexField.LANGUAGE, rs.getString("language"));
        addNonEmptyFieldToDocument(doc, ReleaseIndexField.SCRIPT, rs.getString("script"));

        if (labelInfo.containsKey(id)) {
            for (List<String> entry : labelInfo.get(id)) {
                addFieldOrHyphenToDocument(doc, ReleaseIndexField.LABEL, entry.get(0));
                addFieldOrHyphenToDocument(doc, ReleaseIndexField.CATALOG_NO, entry.get(1));
            }
        }

        int trackCount = 0;
        int discCount = 0;
        if (mediums.containsKey(id)) {
            for (List<String> entry : mediums.get(id)) {
                String str;
                str = entry.get(0);
                addFieldOrHyphenToDocument(doc, ReleaseIndexField.FORMAT, str);
                int numTracksOnMedium = Integer.parseInt(entry.get(1));
                addNumericFieldToDocument(doc,ReleaseIndexField.NUM_TRACKS_MEDIUM,numTracksOnMedium);
                trackCount+=numTracksOnMedium;

                int numDiscsOnMedium = Integer.parseInt(entry.get(2));
                addNumericFieldToDocument(doc,ReleaseIndexField.NUM_DISCIDS_MEDIUM,numDiscsOnMedium);
                discCount+=numDiscsOnMedium;

            }

            //Num Tracks over the whole release
            addNumericFieldToDocument(doc,ReleaseIndexField.NUM_TRACKS,trackCount);

            //Num Discs over the whole release
            addNumericFieldToDocument(doc,ReleaseIndexField.NUM_DISCIDS,trackCount);

        }

        if (artists.containsKey(id)) {
            //For each artist credit for this release
            for (ArtistWrapper artist : artists.get(id)) {
                  addFieldToDocument(doc, ReleaseIndexField.ARTIST_ID, artist.getArtistId());
                 //TODO in many cases these three values might be the same is user actually interested in searching
                 //by these variations, or do we just need for output
                 addFieldToDocument(doc, ReleaseIndexField.ARTIST_NAME, artist.getArtistName());
                 addFieldToDocument(doc, ReleaseIndexField.ARTIST_SORTNAME, artist.getArtistSortName());
                 addFieldToDocument(doc, ReleaseIndexField.ARTIST_NAMECREDIT, artist.getArtistCreditName());
                 addFieldOrHyphenToDocument(doc, ReleaseIndexField.ARTIST_JOINPHRASE, artist.getJoinPhrase());
                 addFieldOrHyphenToDocument(doc, ReleaseIndexField.ARTIST_COMMENT, artist.getArtistComment());
            }
            addFieldToDocument(doc, ReleaseIndexField.ARTIST, ArtistWrapper.createFullArtistCredit(artists.get(id)));
        }
        return doc;
    }

}
