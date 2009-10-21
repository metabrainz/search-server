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


public class WorkIndex extends DatabaseIndex {

    public WorkIndex(Connection dbConnection) {
        super(dbConnection);
    }

    public String getName() {
        return "work";
    }

    public Analyzer getAnalyzer() {
        return new PerFieldEntityAnalyzer(WorkIndexField.class);
    }

    public int getMaxId() throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM work");
        rs.next();
        return rs.getInt(1);
    }

    public int getNoOfRows(int maxId) throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT count(*) FROM work WHERE id<="+maxId);
        rs.next();
        return rs.getInt(1);
    }

    @Override
    public void init() throws SQLException {

        addPreparedStatement("ARTISTS",
                        "SELECT w.id as wid, " +
                        "acn.position as pos, " +
                        "acn.joinphrase as joinphrase, " +
                        "a.gid as artistId,  " +
                        "a.comment as comment, " +
                        "an.name as artistName, " +
                        "an2.name as artistCreditName, " +
                        "an3.name as artistSortName " +
                        "FROM work AS w " +
                        "INNER JOIN artist_credit_name acn ON w.artist_credit=acn.artist_credit " +
                        "INNER JOIN artist a ON a.id=acn.artist " +
                        "INNER JOIN artist_name an on a.name=an.id " +
                        "INNER JOIN artist_name an2 on acn.name=an2.id " +
                        "INNER JOIN artist_name an3 on a.sortname=an3.id " +
                        "WHERE w.id BETWEEN ? AND ?  " +
                        "order by w.id,acn.position ");          //Order by pos so come in expected order

        addPreparedStatement("WORKS",
                        "SELECT w.id as wid, w.gid, wn.name as name, lower(wt.name) as type " +
                        "FROM work AS w " +
                        "LEFT JOIN work_name wn ON w.name = wn.id " +
                        "LEFT JOIN work_type wt ON w.type = wt.id " +
                        "WHERE w.id BETWEEN ? AND ? " +
                        "order by w.id");
    }


    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {


        //Artists
        Map<Integer, List<ArtistWrapper>> artists = new HashMap<Integer, List<ArtistWrapper>>();
        PreparedStatement st = getPreparedStatement("ARTISTS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int releaseGroupId = rs.getInt("wid");
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

        //Works
        st = getPreparedStatement("WORKS");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, artists));
        }

    }

    public Document documentFromResultSet(ResultSet rs,Map<Integer, List<ArtistWrapper>> artists) throws SQLException {
        Document doc = new Document();
        int id = rs.getInt("wid");
        addFieldToDocument(doc, WorkIndexField.WORK_ID, rs.getString("gid"));
        addFieldToDocument(doc, WorkIndexField.WORK, rs.getString("name"));
        addNonEmptyFieldToDocument(doc, WorkIndexField.TYPE, rs.getString("type"));

        if (artists.containsKey(id)) {
            //For each artist credit
            for (ArtistWrapper artist : artists.get(id)) {
                 addFieldToDocument(doc, WorkIndexField.ARTIST_ID, artist.getArtistId());
                //TODO in many cases these three values might be the same is user actually interested in searching
                //by these variations, or do we just need for output
                addFieldToDocument(doc, WorkIndexField.ARTIST_NAME, artist.getArtistName());
                addFieldToDocument(doc, WorkIndexField.ARTIST_SORTNAME, artist.getArtistSortName());
                addFieldToDocument(doc, WorkIndexField.ARTIST_NAMECREDIT, artist.getArtistCreditName());
                addFieldOrHyphenToDocument(doc, WorkIndexField.ARTIST_JOINPHRASE, artist.getJoinPhrase());
                addFieldOrHyphenToDocument(doc, WorkIndexField.ARTIST_COMMENT, artist.getArtistComment());
            }
            addFieldToDocument(doc, WorkIndexField.ARTIST, ArtistWrapper.createFullArtistCredit(artists.get(id)));
        }
        return doc;
    }

}