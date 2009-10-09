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

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.analysis.Analyzer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ReleaseIndex extends Index {

    private Pattern stripBarcodeOfLeadingZeroes;

    public ReleaseIndex(Connection dbConnection) {
        super(dbConnection);

        //TODO move to analyzer
        stripBarcodeOfLeadingZeroes = Pattern.compile("^0+");
    }

    public Analyzer getAnalyzer() {
        return new ReleaseAnalyzer();
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

    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {

        //A particular release can have multiple catalognos, labels when released as an imprint, typically used
        //by major labels
        Map<Integer, List<List<String>>> events = new HashMap<Integer, List<List<String>>>();
        PreparedStatement st = dbConnection.prepareStatement(
                "SELECT rl.release as releaseId, ln.name as label, catno " +
                        "FROM release_label rl " +
                        "LEFT JOIN label l ON rl.label=l.id " +
                        "LEFT JOIN label_name ln ON l.name = ln.id " +
                        "WHERE rl.release BETWEEN ? AND ?");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int releaseId = rs.getInt("releaseId");
            List<List<String>> list;
            if (!events.containsKey(releaseId)) {
                list = new LinkedList<List<String>>();
                events.put(releaseId, list);
            } else {
                list = events.get(releaseId);
            }
            List<String> entry = new ArrayList<String>(2);
            entry.add(rs.getString("label"));
            entry.add(rs.getString("catno"));
            list.add(entry);
        }
        st.close();

        //Format,NumTracks a release can be released on mutiple mediums, and possibly involving formats, i.e a release is on CD with
        //a special 7" single included. We also need total tracks, if release consists of multiple mediums we just have
        //to sum up the tracks on each medium to get the total for the release
        Map<Integer, List<List<String>>> formats = new HashMap<Integer, List<List<String>>>();
        st = dbConnection.prepareStatement(
                "SELECT m.release as releaseId, mf.name as format,sum(tr.trackcount) as numTracks " +
                "FROM medium m " +
                "LEFT JOIN medium_format mf ON m.format=mf.id " +
                "LEFT JOIN tracklist tr ON m.tracklist=tr.id " +
                "WHERE m.release BETWEEN ? AND ? " +
                "GROUP BY release,mf.name"
                );
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            int releaseId = rs.getInt("releaseId");
            List<List<String>> list;
            if (!formats.containsKey(releaseId)) {
                list = new LinkedList<List<String>>();
                formats.put(releaseId, list);
            } else {
                list = formats.get(releaseId);
            }
            List<String> entry = new ArrayList<String>(2);
            entry.add(rs.getString("format"));
            entry.add(rs.getString("numTracks"));
            list.add(entry);
        }
        st.close();

        //No of Disc Ids
        Map<Integer, List<List<String>>> numDiscIds = new HashMap<Integer, List<List<String>>>();
        st = dbConnection.prepareStatement(
                "SELECT m.release as releaseId, count(mc.id) as discids " +
                "FROM medium m " +
                "LEFT JOIN medium_cdtoc mc ON mc.medium=m.id "  +
                "WHERE m.release BETWEEN ? AND ?" +
                "GROUP BY releaseId"
                );
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            int releaseId = rs.getInt("releaseId");
            List<List<String>> list;
            if (!numDiscIds.containsKey(releaseId)) {
                list = new LinkedList<List<String>>();
                numDiscIds.put(releaseId, list);
            } else {
                list = numDiscIds.get(releaseId);
            }
            List<String> entry = new ArrayList<String>(2);
            entry.add(rs.getString("discids"));
            list.add(entry);
        }
        st.close();

        //Artists
        Map<Integer, List<ArtistWrapper>> artists = new HashMap<Integer, List<ArtistWrapper>>();
        st = dbConnection.prepareStatement(
                "SELECT r.id as releaseId, " +
                "acn.position as pos, " +
                "acn.joinphrase as joinphrase, " +
                "a.gid as artistId,  " +
                "a.comment as comment, " +
                "an.name as artistName, " +
                "an2.name as artistCreditName " +
                "FROM release AS r " +
                "INNER JOIN artist_credit_name acn ON r.artist_credit=acn.artist_credit " +
                "INNER JOIN artist a ON a.id=acn.artist " +
                "INNER JOIN artist_name an on a.name=an.id " +
                "INNER JOIN artist_name an2 on acn.name=an2.id " +
                "WHERE r.id BETWEEN ? AND ?  " +
                "order by r.id,acn.position ");
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
            aw.setArtistPos(rs.getInt("pos"));
            aw.setComment(rs.getString("comment"));
            aw.setJoinPhrase(rs.getString("joinphrase"));
            list.add(aw);
        }
        st.close();


        st = dbConnection.prepareStatement(
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
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, events, formats,numDiscIds,artists));
        }
        st.close();
    }

    public Document documentFromResultSet(ResultSet rs,
                                          Map<Integer,List<List<String>>> events,
                                          Map<Integer,List<List<String>>> formats,
                                          Map<Integer,List<List<String>>>  numDiscIds,
                                          Map<Integer, List<ArtistWrapper>> artists) throws SQLException {
        Document doc = new Document();
        int releaseId = rs.getInt("id");
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



        if (events.containsKey(releaseId)) {
            for (List<String> entry : events.get(releaseId)) {
                String str;

                str = entry.get(0);
                if (str == null || str.isEmpty()) {
                    str = "-";
                }
                addFieldToDocument(doc, ReleaseIndexField.LABEL, str);

                str = entry.get(1);
                if (str == null || str.isEmpty()) {
                    str = "-";
                }
                addFieldToDocument(doc, ReleaseIndexField.CATALOG_NO, str);
            }
        }

        if (formats.containsKey(releaseId)) {
            for (List<String> entry : formats.get(releaseId)) {
                String str;
                str = entry.get(0);
                addNonEmptyFieldToDocument(doc, ReleaseIndexField.FORMAT, str);
            }

            //Num Tracks already calculated
            addNonEmptyFieldToDocument(doc,ReleaseIndexField.NUM_TRACKS,formats.get(releaseId).get(0).get(1));
        }


        if (numDiscIds.containsKey(releaseId)) {
            for (List<String> entry : numDiscIds.get(releaseId)) {
                String str;
                str = entry.get(0);
                addNonEmptyFieldToDocument(doc, ReleaseIndexField.NUM_DISC_IDS, str);

            }
        }

         if (artists.containsKey(releaseId)) {
            //For each credit artist for this release
            for (ArtistWrapper artist : artists.get(releaseId)) {
                addFieldToDocument(doc, ReleaseIndexField.ARTIST_ID, artist.getArtistId());
                addFieldToDocument(doc, ReleaseIndexField.ARTIST, artist.getArtistName());
                //Only add if different
                if (!artist.getArtistName().equals(artist.getArtistCreditName())) {
                    addFieldToDocument(doc, ReleaseIndexField.ARTIST, artist.getArtistCreditName());
                }
                addNonEmptyFieldToDocument(doc, ReleaseIndexField.ARTIST_COMMENT, artist.getComment());
            }

            //Construct a single string comprising all credits, this will be need for V1 because just has single
            //field for artist
            //TODO optimize, if only have single artist we don't need extra field

            StringBuffer sb = new StringBuffer();
            for (ArtistWrapper artist : artists.get(releaseId)) {
                sb.append(artist.getArtistCreditName());
                if (artist.getJoinPhrase() != null) {
                    sb.append(' ' + artist.getJoinPhrase() + ' ');
                }
            }
            addFieldToDocument(doc, ReleaseIndexField.ARTIST_V1, sb.toString());

        }

        return doc;
    }

}
