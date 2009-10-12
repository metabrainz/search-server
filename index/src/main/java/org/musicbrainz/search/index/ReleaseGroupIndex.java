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

import java.sql.*;

public class ReleaseGroupIndex extends DatabaseIndex {

    public ReleaseGroupIndex(Connection dbConnection) {
        super(dbConnection);
    }

    @Override
    public String getName() {
        return "release-group";
    }
    
    @Override
    public void init() throws SQLException {
        addPreparedStatement("ARTIST_CREDITS",
                "SELECT t.release_group, t.artist_credit, " +
                "    n0.name as credit_name, joinphrase, artist.id as artist_id, n1.name as artist_name " +
                "  FROM artist_name n0 " +
                "    JOIN artist_credit_name ON artist_credit_name.name = n0.id " +
                "    JOIN artist ON artist.id = artist_credit_name.artist " +
                "    JOIN artist_name n1 ON n1.id = artist.name " +
                "    JOIN ( " +
                "       SELECT release_group.id AS release_group, artist_credit " +
                "          FROM release_group " +
                "          WHERE release_group.id BETWEEN ? AND ? " +
                "       UNION " +
                "       SELECT DISTINCT release_group, artist_credit " +
                "          FROM release " +
                "          WHERE release.release_group BETWEEN ? AND ? " +
                "       ) t ON t.artist_credit = artist_credit_name.artist_credit " +
                "  ORDER BY t.release_group, t.artist_credit, artist_credit_name.position"
        );

        addPreparedStatement("RELEASE_NAMES",
                "SELECT t.release_group, n.name " +
                "  FROM release_name n " +
                "    JOIN (" +
                "       SELECT DISTINCT release_group, name " +
                "       FROM release " +
                "       WHERE release.release_group BETWEEN ? AND ?" +
                "    ) t ON t.name = n.id  "
        );
        
        addPreparedStatement("RELEASE-GROUPS",
                "SELECT release_group.id, gid, n.name, lower(type.name) AS type, comment," +
                "  meta.firstreleasedate_year, meta.firstreleasedate_month, meta.firstreleasedate_day " +
                "FROM release_group " +
                "  JOIN release_name n ON n.id = release_group.name " +
                "  JOIN release_group_meta meta ON meta.id = release_group.id " +
                "  LEFT JOIN release_group_type type ON type.id = release_group.type " +
                "WHERE release_group.id BETWEEN ? AND ? "
        );
    }
    
    @Override
    public int getMaxId() throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM release_group");
        rs.next();
        return rs.getInt(1);
    }

    @Override
    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {

        // Get artists credits
        Map<Integer, Map<Integer, ArtistCredit>> artistCredits = new HashMap<Integer, Map<Integer, ArtistCredit>>();
        PreparedStatement st = getPreparedStatement("ARTIST_CREDITS");
        st.setInt(1, min);
        st.setInt(2, max);
        st.setInt(3, min);
        st.setInt(4, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int rgId = rs.getInt("release_group");
            int artistCreditId = rs.getInt("artist_credit");
            
            Map<Integer, ArtistCredit> rgArtistCredits;
            if (!artistCredits.containsKey(rgId)) {
                rgArtistCredits = new HashMap<Integer, ArtistCredit>();
                artistCredits.put(rgId, rgArtistCredits);
            } else {
                rgArtistCredits = artistCredits.get(rgId);
            }

            ArtistCredit ac;
            if (!rgArtistCredits.containsKey(artistCreditId)) {
                ac = new ArtistCredit();
                rgArtistCredits.put(artistCreditId, ac);
            } else {
                ac = rgArtistCredits.get(artistCreditId);
            }
            
            ArtistCreditName acn = new ArtistCreditName(
                    rs.getString("credit_name"), 
                    rs.getString("joinphrase"),
                    rs.getInt("artist_id"), 
                    rs.getString("artist_name")
            );
            ac.appendArtistCreditName(acn);
 
        }
        
        // Get releases names
        Map<Integer, List<String>> releaseNames = new HashMap<Integer, List<String>>();
        st = getPreparedStatement("RELEASE_NAMES");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            int rgId = rs.getInt("release_group");

            List<String> list;
            if (!releaseNames.containsKey(rgId)) {
                list = new LinkedList<String>();
                releaseNames.put(rgId, list);
            } else {
                list = releaseNames.get(rgId);
            }
            list.add(rs.getString("name"));
        }
        
        // Get release-groups
        st = getPreparedStatement("RELEASE-GROUPS");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, artistCredits, releaseNames));
        }

    }

    public Document documentFromResultSet(ResultSet rs, Map<Integer, Map<Integer, ArtistCredit>> artistCredits, Map<Integer, List<String>> releaseNames) throws SQLException {
        
        Document doc = new Document();
        int rgId = rs.getInt("id");
        addFieldToDocument(doc, ReleaseGroupIndexField.ENTITY_TYPE, this.getName());
        addFieldToDocument(doc, ReleaseGroupIndexField.ENTITY_GID, rs.getString("gid"));
        addFieldToDocument(doc, ReleaseGroupIndexField.RELEASEGROUP, rs.getString("name"));
        addNonEmptyFieldToDocument(doc, ReleaseGroupIndexField.COMMENT, rs.getString("comment"));
        addNonEmptyFieldToDocument(doc, ReleaseGroupIndexField.TYPE, rs.getString("type"));

        addNonEmptyFieldToDocument(doc, ReleaseGroupIndexField.FIRST_RELEASE_DATE, 
                Utils.formatDate(rs.getInt("firstreleasedate_year"), rs.getInt("firstreleasedate_month"), rs.getInt("firstreleasedate_day")));
        
        // Artist credits
        if (artistCredits.containsKey(rgId)) {
            for (ArtistCredit ac : artistCredits.get(rgId).values()) {
                addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST, ac.getArtistCreditString());
                for (ArtistCreditName acn : ac) {
                    if (!acn.getName().equals(acn.getArtistName())) {
                        addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST, acn.getArtistName());
                    }
                }
            }
        }
        
        // Release names
        if (releaseNames.containsKey(rgId)) {
            StringBuffer sb = new StringBuffer();
            for (String name : releaseNames.get(rgId)) {
                sb.append(name);
                sb.append(" ");
            }
            addFieldToDocument(doc, ReleaseGroupIndexField.RELEASES, sb.toString());
        }
        
        return doc;
    }

}
