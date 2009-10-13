/*
 * MusicBrainz Search Server
 * Copyright (C) 2009  Aurélien Mino

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
import java.util.*;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.Analyzer;

import java.sql.*;

public class
        ReleaseGroupIndex extends Index {

    public ReleaseGroupIndex(Connection dbConnection) {
        super(dbConnection);
    }

    public String getName() {
        return "releasegroup";
    }

    public Analyzer getAnalyzer() {
        return new ReleaseGroupAnalyzer();
    }

    public int getMaxId() throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM release_group");
        rs.next();
        return rs.getInt(1);
    }

    public int getNoOfRows(int maxId) throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT count(*) FROM release_group WHERE id<="+maxId);
        rs.next();
        return rs.getInt(1);
    }

    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {

        //Releases
        Map<Integer, List<String>> releases = new HashMap<Integer, List<String>>();
        PreparedStatement st = dbConnection.prepareStatement(
                "SELECT DISTINCT release_group, n0.name as name " +
                "FROM release " +
                "LEFT JOIN release_name n0 ON release.name = n0.id " +
                "WHERE release_group BETWEEN ? AND ?");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int rgId = rs.getInt("release_group");
            List<String> list;
            if (!releases.containsKey(rgId)) {
                list = new LinkedList<String>();
                releases.put(rgId, list);
            } else {
                list = releases.get(rgId);
            }
            list.add(rs.getString("name"));
        }
        st.close();

        //Artists
        Map<Integer, List<ArtistWrapper>> artists = new HashMap<Integer, List<ArtistWrapper>>();
        st = dbConnection.prepareStatement(
                "SELECT rg.id as releaseGroupId, " +
                        "acn.position as pos, " +
                        "acn.joinphrase as joinphrase, " +
                        "a.gid as artistId,  " +
                        "a.comment as comment, " +
                        "an.name as artistName, " +
                        "an2.name as artistCreditName, " +
                        "an3.name as artistSortName " +
                        "FROM release_group AS rg " +
                        "INNER JOIN artist_credit_name acn ON rg.artist_credit=acn.artist_credit " +
                        "INNER JOIN artist a ON a.id=acn.artist " +
                        "INNER JOIN artist_name an on a.name=an.id " +
                        "INNER JOIN artist_name an2 on acn.name=an2.id " +
                        "INNER JOIN artist_name an3 on a.sortname=an3.id " +
                        "WHERE rg.id BETWEEN ? AND ?  " +
                        "order by rg.id,acn.position ");
        st.setInt(1, min);
        st.setInt(2, max);


        rs = st.executeQuery();
        while (rs.next()) {
            int releaseGroupId = rs.getInt("releaseGroupId");
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
            aw.setComment(rs.getString("comment"));
            aw.setJoinPhrase(rs.getString("joinphrase"));
            list.add(aw);
        }
        st.close();


        st = dbConnection.prepareStatement(
                "SELECT rg.id, rg.gid, n0.name as name, lower(release_group_type.name) as type " +
                        "FROM release_group AS rg " +
                        "LEFT JOIN release_name n0 ON rg.name = n0.id " +
                        "LEFT JOIN release_group_type  ON rg.type = release_group_type.id " +
                        "WHERE rg.id BETWEEN ? AND ?" +
                        "order by rg.id");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, releases, artists));
        }
        st.close();

    }

    public Document documentFromResultSet(ResultSet rs, Map<Integer, List<String>> releases, Map<Integer, List<ArtistWrapper>> artists) throws SQLException {
        Document doc = new Document();
        int rgId = rs.getInt("id");
        addFieldToDocument(doc, ReleaseGroupIndexField.RELEASEGROUP_ID, rs.getString("gid"));
        addFieldToDocument(doc, ReleaseGroupIndexField.RELEASEGROUP, rs.getString("name"));
        addNonEmptyFieldToDocument(doc, ReleaseGroupIndexField.TYPE, rs.getString("type"));

        if (releases.containsKey(rgId)) {
            for (String release : releases.get(rgId)) {
                addFieldToDocument(doc, ReleaseGroupIndexField.RELEASES, release);
            }
        }

        if (artists.containsKey(rgId)) {
            //For each credit artist for this release
            for (ArtistWrapper artist : artists.get(rgId)) {
                addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST_ID, artist.getArtistId());
                addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST, artist.getArtistName());
                addFieldToDocument(doc, ReleaseIndexField.ARTIST_SORTNAME, artist.getArtistSortName());
                //Only add if different
                if (!artist.getArtistName().equals(artist.getArtistCreditName())) {
                    addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST, artist.getArtistCreditName());
                }
                addNonEmptyFieldToDocument(doc, ReleaseGroupIndexField.ARTIST_COMMENT, artist.getComment());
            }

            //Construct a single string comprising all credits, this will be need for V1 because just has single
            //field for artist
            //TODO optimize, if only have single artist we don't need extra field

            StringBuffer sb = new StringBuffer();
            for (ArtistWrapper artist : artists.get(rgId)) {
                sb.append(artist.getArtistCreditName());
                if (artist.getJoinPhrase() != null) {
                    sb.append(' ' + artist.getJoinPhrase() + ' ');
                }
            }
            addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST_V1, sb.toString());
            //System.out.println(rgId+":"+sb.toString());

        }
        return doc;
    }

}


