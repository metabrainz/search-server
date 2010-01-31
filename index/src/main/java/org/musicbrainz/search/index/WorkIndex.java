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
import org.musicbrainz.mmd2.ArtistCredit;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.sql.*;


public class WorkIndex extends DatabaseIndex {

    public WorkIndex(Connection dbConnection) {
        super(dbConnection);
    }

    public WorkIndex() {
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
    public void init(IndexWriter indexWriter) throws SQLException {

        addPreparedStatement("ARTISTCREDITS",
                        "SELECT w.id as wid, " +
                        "  acn.position as pos, " +
                        "  acn.joinphrase as joinphrase, " +
                        "  a.gid as artistId,  " +
                        "  a.comment as comment, " +
                        "  an.name as artistName, " +
                        "  an2.name as artistCreditName, " +
                        "  an3.name as artistSortName " +
                        " FROM work AS w " +
                        "  INNER JOIN artist_credit_name acn ON w.artist_credit=acn.artist_credit " +
                        "  INNER JOIN artist a ON a.id=acn.artist " +
                        "  INNER JOIN artist_name an ON a.name=an.id " +
                        "  INNER JOIN artist_name an2 ON acn.name=an2.id " +
                        "  INNER JOIN artist_name an3 ON a.sortname=an3.id " +
                        " WHERE w.id BETWEEN ? AND ?  " +
                        " ORDER BY w.id, acn.position");          //Order by pos so come in expected order

        addPreparedStatement("WORKS",
                        "SELECT w.id as wid, w.gid, wn.name as name, lower(wt.name) as type, iswc " +
                        " FROM work AS w " +
                        "  LEFT JOIN work_name wn ON w.name = wn.id " +
                        "  LEFT JOIN work_type wt ON w.type = wt.id " +
                        " WHERE w.id BETWEEN ? AND ? " +
                        " ORDER BY w.id");
    }


    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {

        //Artist Credits
        PreparedStatement st = getPreparedStatement("ARTISTCREDITS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        Map<Integer, ArtistCredit> artistCredits
                = ArtistCreditHelper.completeArtistCreditFromDbResults
                     (rs,
                      "wid",
                      "artistId",
                      "artistName",
                      "artistSortName",
                      "comment",
                      "joinphrase",
                      "artistCreditName");

        //Works
        st = getPreparedStatement("WORKS");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, artistCredits));
        }

    }

    public Document documentFromResultSet(ResultSet rs,Map<Integer, ArtistCredit> artistCredits) throws SQLException {
        MbDocument doc = new MbDocument();
        int id = rs.getInt("wid");
        doc.addField(WorkIndexField.WORK_ID, rs.getString("gid"));
        doc.addField(WorkIndexField.WORK, rs.getString("name"));
        doc.addNonEmptyField(WorkIndexField.TYPE, rs.getString("type"));
        doc.addNonEmptyField(WorkIndexField.ISWC, rs.getString("iswc"));

        ArtistCredit ac = artistCredits.get(id);
        ArtistCreditHelper.buildIndexFieldsFromArtistCredit
               (doc,
                ac,
                WorkIndexField.ARTIST,
                WorkIndexField.ARTIST_NAMECREDIT,
                WorkIndexField.ARTIST_ID,
                WorkIndexField.ARTIST_NAME,
                WorkIndexField.ARTIST_CREDIT);
        
        return doc.getLuceneDocument();
    }

}