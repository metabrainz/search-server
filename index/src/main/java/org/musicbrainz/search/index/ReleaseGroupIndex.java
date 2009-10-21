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
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.sql.*;

public class
        ReleaseGroupIndex extends DatabaseIndex {

    public ReleaseGroupIndex(Connection dbConnection) {
        super(dbConnection);
    }

    public String getName() {
        return "releasegroup";
    }

    public Analyzer getAnalyzer() {
        return new PerFieldEntityAnalyzer(ReleaseGroupIndexField.class);
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

    @Override
    public void init() throws SQLException {
        addPreparedStatement("RELEASES",
                "SELECT DISTINCT release_group, n0.name as name " +
                "FROM release " +
                "LEFT JOIN release_name n0 ON release.name = n0.id " +
                "WHERE release_group BETWEEN ? AND ?");

        addPreparedStatement("ARTISTS",
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
                        "order by rg.id,acn.position ");          //Order by pos so come in expected order

        addPreparedStatement("RELEASEGROUPS",
            "SELECT rg.id, rg.gid, n0.name as name, lower(release_group_type.name) as type " +
                        "FROM release_group AS rg " +
                        "LEFT JOIN release_name n0 ON rg.name = n0.id " +
                        "LEFT JOIN release_group_type  ON rg.type = release_group_type.id " +
                        "WHERE rg.id BETWEEN ? AND ?" +
                        "order by rg.id");
    }


    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {

        //Releases
        Map<Integer, List<String>> releases = new HashMap<Integer, List<String>>();
        PreparedStatement st =getPreparedStatement("RELEASES");
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

        //Artists
        Map<Integer, List<ArtistWrapper>> artists = new HashMap<Integer, List<ArtistWrapper>>();
        st = getPreparedStatement("ARTISTS");
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
            aw.setArtistComment(rs.getString("comment"));
            aw.setJoinPhrase(rs.getString("joinphrase"));
            list.add(aw);
        }

        //ReleaseGroups
        st = getPreparedStatement("RELEASEGROUPS");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, releases, artists));
        }

    }

    public Document documentFromResultSet(ResultSet rs, Map<Integer, List<String>> releases, Map<Integer, List<ArtistWrapper>> artists) throws SQLException {
        Document doc = new Document();
        int id = rs.getInt("id");
        addFieldToDocument(doc, ReleaseGroupIndexField.RELEASEGROUP_ID, rs.getString("gid"));
        addFieldToDocument(doc, ReleaseGroupIndexField.RELEASEGROUP, rs.getString("name"));
        addNonEmptyFieldToDocument(doc, ReleaseGroupIndexField.TYPE, rs.getString("type"));

        //Add each release name within this release group
        if (releases.containsKey(id)) {
            for (String release : releases.get(id)) {
                addFieldToDocument(doc, ReleaseGroupIndexField.RELEASE, release);
            }
        }

        if (artists.containsKey(id)) {
            //For each artist credit for this release
            for (ArtistWrapper artist : artists.get(id)) {
                 addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST_ID, artist.getArtistId());
                //TODO in many cases these three values might be the same is user actually interested in searching
                //by these variations, or do we just need for output
                addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST_NAME, artist.getArtistName());
                addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST_SORTNAME, artist.getArtistSortName());
                addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST_NAMECREDIT, artist.getArtistCreditName());
                addFieldOrHyphenToDocument(doc, ReleaseGroupIndexField.ARTIST_JOINPHRASE, artist.getJoinPhrase());
                addFieldOrHyphenToDocument(doc, ReleaseGroupIndexField.ARTIST_COMMENT, artist.getArtistComment());
            }
            addFieldToDocument(doc, TrackIndexField.ARTIST, ArtistWrapper.createFullArtistCredit(artists.get(id)));
        }
        return doc;
    }

}


