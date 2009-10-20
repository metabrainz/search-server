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

public class TrackIndex extends Index {

    private final static int QUANTIZED_DURATION = 2000;
   
    public TrackIndex(Connection dbConnection) {
        super(dbConnection);
    }

    public String getName() {
        return "track";
    }

    public Analyzer getAnalyzer()
    {
        return new PerFieldEntityAnalyzer(TrackIndexField.class);
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

    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {

         //Artists
        Map<Integer, List<ArtistWrapper>> artists = new HashMap<Integer, List<ArtistWrapper>>();
        PreparedStatement st = dbConnection.prepareStatement(
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
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int recordingId = rs.getInt("recordingId");
            List<ArtistWrapper> list;
            if (!artists.containsKey(recordingId)) {
                list = new LinkedList<ArtistWrapper>();
                artists.put(recordingId, list);
            } else {
                list = artists.get(recordingId);
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
        st.close();

        //Tracks
        //TODO currently when recording is on two albums we generate two seperate documents, but should we having just one document
        //with the tracklist/release info added as multiple fields to docs.
        //If we do MMD v1 allows multiple releases for a track, but code might not expect it.
        st = dbConnection.prepareStatement(
                "SELECT re.id as recordingId,re.gid as trackid,re.length as duration,tn.name as trackname,t.position,tl.trackcount, " +
                "re.comment,r.gid as releaseid,rn.name as releasename,rgt.name as type " +
                "FROM recording re " +
                "INNER JOIN track_name tn " +
                "ON re.name=tn.id " +
                "INNER JOIN track t " +
                "ON re.id=t.recording " +
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
                "WHERE re.id BETWEEN ? AND ?");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs,artists));
        }
        st.close();


    }

    public Document documentFromResultSet(ResultSet rs,Map<Integer, List<ArtistWrapper>> artists) throws SQLException {

        int recordingId = rs.getInt("recordingId");

        Document doc = new Document();
        addFieldToDocument(doc, TrackIndexField.TRACK_ID, rs.getString("trackid"));
        addNonEmptyFieldToDocument(doc, TrackIndexField.TRACK, rs.getString("trackname"));
        addFieldToDocument(doc, TrackIndexField.RELEASE_ID, rs.getString("releaseid"));
        addFieldToDocument(doc, TrackIndexField.RELEASE, rs.getString("releasename"));
        addNumericFieldToDocument(doc, TrackIndexField.NUM_TRACKS, rs.getInt("trackcount"));
        addNumericFieldToDocument(doc, TrackIndexField.DURATION, rs.getInt("duration"));
        addNumericFieldToDocument(doc, TrackIndexField.QUANTIZED_DURATION, rs.getInt("duration") / QUANTIZED_DURATION);
        addNumericFieldToDocument(doc, TrackIndexField.TRACKNUM, rs.getInt("position"));
        addNonEmptyFieldToDocument(doc,TrackIndexField.RELEASE_TYPE, rs.getString("type"));

        if (artists.containsKey(recordingId)) {
            //For each credit artist for this release
            for (ArtistWrapper artist : artists.get(recordingId)) {
                addFieldToDocument(doc,TrackIndexField.ARTIST_ID, artist.getArtistId());
                addFieldToDocument(doc, TrackIndexField.ARTIST, artist.getArtistName());
                addFieldToDocument(doc, TrackIndexField.ARTIST_SORTNAME, artist.getArtistSortName());
                //Only add if different
                if (!artist.getArtistName().equals(artist.getArtistCreditName())) {
                    addFieldToDocument(doc, TrackIndexField.ARTIST, artist.getArtistCreditName());
                }
                addNonEmptyFieldToDocument(doc, TrackIndexField.ARTIST_COMMENT, artist.getArtistComment());
            }

            //Construct a single string comprising all credits, this will be need for V1 because just has single
            //field for artist
            //TODO optimize, if only have single artist we don't need extra field
           /*
            StringBuffer sb = new StringBuffer();
            for (ArtistWrapper artist : artists.get(recordingId)) {
                sb.append(artist.getArtistCreditName());
                if (artist.getJoinPhrase() != null) {
                    sb.append(' ' + artist.getJoinPhrase() + ' ');
                }
            }
            addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST, sb.toString());
            //System.out.println(rgId+":"+sb.toString());
            */
        }
        return doc;
    }

}
