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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ReleaseIndex extends DatabaseIndex {

    public ReleaseIndex(Connection dbConnection) {
        super(dbConnection);
    }

    @Override
    public String getName() {
        return "release";
    }

    @Override
    public void init() throws SQLException {
        addPreparedStatement("ARTIST_CREDITS",
                "SELECT t.release, t.artist_credit, " +
                "    n0.name as credit_name, joinphrase, artist.id as artist_id, n1.name as artist_name " +
                "  FROM artist_name n0 " +
                "    JOIN artist_credit_name ON artist_credit_name.name = n0.id " +
                "    JOIN artist ON artist.id = artist_credit_name.artist " +
                "    JOIN artist_name n1 ON n1.id = artist.name " +
                "    JOIN ( " +
                "       SELECT release.id AS release, artist_credit " +
                "          FROM release " +
                "          WHERE release.id BETWEEN ? AND ? " +
//                "       UNION " +
//                "       SELECT DISTINCT release_group, artist_credit " +
//                "          FROM release " +
//                "          WHERE release.release_group BETWEEN ? AND ? " +
                "       ) t ON t.artist_credit = artist_credit_name.artist_credit " +
                "  ORDER BY t.release, t.artist_credit, artist_credit_name.position"
        );

        addPreparedStatement("MEDIUM_INFOS",
                "SELECT release, medium.name, medium_format.name AS format, tracklist.trackcount " +
                "  FROM medium " +
                "    JOIN tracklist ON tracklist.id = medium.tracklist " +
                "    LEFT JOIN medium_format ON medium_format.id = medium.format " +
                "  WHERE medium.release BETWEEN ? AND ? "
        );
        
        addPreparedStatement("LABEL_CATNOS",
                "SELECT release, n.name as label, catno " +
                "  FROM release_label " +
                "    LEFT JOIN label ON label.id = release_label.label " +
                "    LEFT JOIN label_name n ON n.id = label.name " +
                "  WHERE release_label.release BETWEEN ? AND ? "
        );
        
        addPreparedStatement("RELEASES",
                "SELECT release.id, release.gid, n.name, " +
                "  date_year, date_month, date_day, lower(country.isocode) as country, barcode, " +
                "  lower(type.name) AS type, language.isocode_3t as language, script.isocode as script, " +
                "  status.name as status, packaging.name as packaging, release.comment, " +
                "  meta.amazonasin " +
                "FROM release " +
                "  JOIN release_name n ON n.id = release.name " +
                "  JOIN release_meta meta ON meta.id = release.id " +
                "  JOIN release_group ON release_group.id = release.release_group " +
                "  LEFT JOIN release_group_type type ON type.id = release_group.type " +
                "  LEFT JOIN country ON country.id = release.country " +
                "  LEFT JOIN release_packaging packaging ON packaging.id = release.packaging " +
                "  LEFT JOIN release_status status ON status.id = release.status " +
                "  LEFT JOIN script ON script.id = release.script " +
                "  LEFT JOIN language ON language.id = release.language " +
                "WHERE release.id BETWEEN ? AND ? "
        );
    }
    
    @Override
    public int getMaxId() throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM release");
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
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int releaseId = rs.getInt("release");
            int artistCreditId = rs.getInt("artist_credit");
            
            Map<Integer, ArtistCredit> rgArtistCredits;
            if (!artistCredits.containsKey(releaseId)) {
                rgArtistCredits = new HashMap<Integer, ArtistCredit>();
                artistCredits.put(releaseId, rgArtistCredits);
            } else {
                rgArtistCredits = artistCredits.get(releaseId);
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
        
        
        // Get medium infos
        Map<Integer, List<MediumInfo>> mediumInfos = new HashMap<Integer, List<MediumInfo>>();
        st = getPreparedStatement("MEDIUM_INFOS");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        
        while (rs.next()) {
            int releaseId = rs.getInt("release");

            List<MediumInfo> list;
            if (!mediumInfos.containsKey(releaseId)) {
                list = new LinkedList<MediumInfo>();
                mediumInfos.put(releaseId, list);
            } else {
                list = mediumInfos.get(releaseId);
            }
            MediumInfo info = new MediumInfo(rs.getString("name"), rs.getString("format"), rs.getInt("trackcount"));
            list.add(info);
        }
        
        
        // Get labels and catnos
        Map<Integer, List<LabelCatno>> labelCatnos = new HashMap<Integer, List<LabelCatno>>();
        st = getPreparedStatement("LABEL_CATNOS");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        
        while (rs.next()) {
            int releaseId = rs.getInt("release");

            List<LabelCatno> list;
            if (!labelCatnos.containsKey(releaseId)) {
                list = new LinkedList<LabelCatno>();
                labelCatnos.put(releaseId, list);
            } else {
                list = labelCatnos.get(releaseId);
            }
            LabelCatno info = new LabelCatno(rs.getString("label"), rs.getString("catno"));
            list.add(info);
        }
        
        // Get release-groups
        st = getPreparedStatement("RELEASES");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, artistCredits, mediumInfos, labelCatnos));
        }
        
    }

    public Document documentFromResultSet(ResultSet rs, Map<Integer, Map<Integer, ArtistCredit>> artistCredits, 
            Map<Integer, List<MediumInfo>> mediumInfos, Map<Integer, List<LabelCatno>> labelCatnos) throws SQLException {
        
        Document doc = new Document();
        int releaseId = rs.getInt("id");
        addFieldToDocument(doc, ReleaseGroupIndexField.ENTITY_TYPE, this.getName());
        addFieldToDocument(doc, ReleaseGroupIndexField.ENTITY_GID, rs.getString("gid"));
        addFieldToDocument(doc, ReleaseIndexField.RELEASE, rs.getString("name"));

        
        // Artist credits
        if (artistCredits.containsKey(releaseId)) {
            for (ArtistCredit ac : artistCredits.get(releaseId).values()) {
                addFieldToDocument(doc, ReleaseIndexField.ARTIST, ac.getArtistCreditString());
                for (ArtistCreditName acn : ac) {
                    if (!acn.getName().equals(acn.getArtistName())) {
                        addFieldToDocument(doc, ReleaseIndexField.ARTIST, acn.getArtistName());
                    }
                }
            }
        }
        
        addNonEmptyFieldToDocument(doc, ReleaseIndexField.DATE, 
                Utils.formatDate(rs.getInt("date_year"), rs.getInt("date_month"), rs.getInt("date_day")));
        addNonEmptyFieldToDocument(doc, ReleaseIndexField.COUNTRY, rs.getString("country"));
        addNonEmptyFieldToDocument(doc, ReleaseIndexField.BARCODE, rs.getString("barcode"));

        // Labels & catno
        if (labelCatnos.containsKey(releaseId)) {
            for (LabelCatno lc : labelCatnos.get(releaseId)) {
                addNonEmptyFieldToDocument(doc, ReleaseIndexField.LABEL, lc.label);
                addNonEmptyFieldToDocument(doc, ReleaseIndexField.CATALOG_NO, lc.catno);
            }
        }
        
        addNonEmptyFieldToDocument(doc, ReleaseIndexField.TYPE, rs.getString("type"));
        addNonEmptyFieldToDocument(doc, ReleaseIndexField.SCRIPT, rs.getString("script"));
        addNonEmptyFieldToDocument(doc, ReleaseIndexField.LANGUAGE, rs.getString("language"));
        addNonEmptyFieldToDocument(doc, ReleaseIndexField.STATUS, rs.getString("status"));
        addNonEmptyFieldToDocument(doc, ReleaseIndexField.PACKAGING, rs.getString("packaging"));
        addNonEmptyFieldToDocument(doc, ReleaseIndexField.COMMENT, rs.getString("comment"));
        
        // Medium infos: format, name, trackcount
        if (mediumInfos.containsKey(releaseId)) {
            for (MediumInfo info : mediumInfos.get(releaseId)) {
                addNonEmptyFieldToDocument(doc, ReleaseIndexField.FORMAT, info.format);
                addNonEmptyFieldToDocument(doc, ReleaseIndexField.MEDIUM, info.name);
                addNonEmptyFieldToDocument(doc, ReleaseIndexField.NUM_TRACKS, info.trackcount.toString());
            }
        }
//        addFieldToDocument(doc, ReleaseIndexField.NUM_DISC_IDS, rs.getString("discids"));

        addNonEmptyFieldToDocument(doc, ReleaseIndexField.ASIN, rs.getString("amazonasin"));
        
        return doc;
    }

}

class MediumInfo {
    
    protected String name;
    protected String format;
    protected Integer trackcount;
    
    MediumInfo(String name, String format, Integer trackcount) {
        this.name = name;
        this.format = format;
        this.trackcount = trackcount;
    }
}

class LabelCatno {
    
    protected String label;
    protected String catno;
    
    LabelCatno(String label, String catno) {
        this.label = label;
        this.catno = catno;
    }
}
