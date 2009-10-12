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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReleaseIndex extends DatabaseIndex {

    private Pattern stripBarcodeOfLeadingZeroes;

    public ReleaseIndex(Connection dbConnection) {
        super(dbConnection);

        stripBarcodeOfLeadingZeroes = Pattern.compile("^0+");
    }

    public String getName() {
        return "release";
    }

    public int getMaxId() throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM album");
        rs.next();
        return rs.getInt(1);
    }

    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {
        Map<Integer, List<List<String>>> events = new HashMap<Integer, List<List<String>>>();
        PreparedStatement st = dbConnection.prepareStatement(
                "SELECT album, lower(isocode) as country, releasedate, label.name as label, catno, barcode, format " +
                        "FROM release " +
                        "LEFT JOIN country ON release.country=country.id " +
                        "LEFT JOIN label ON release.label=label.id " +
                        "WHERE album BETWEEN ? AND ?");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int albumId = rs.getInt("album");
            List<List<String>> list;
            if (!events.containsKey(albumId)) {
                list = new LinkedList<List<String>>();
                events.put(albumId, list);
            } else {
                list = events.get(albumId);
            }
            List<String> entry = new ArrayList<String>(5);
            entry.add(rs.getString("country"));
            entry.add(rs.getString("releasedate"));
            entry.add(rs.getString("label"));
            entry.add(rs.getString("catno"));
            entry.add(rs.getString("barcode"));
            entry.add(rs.getString("format"));
            list.add(entry);
        }
        st.close();
        st = dbConnection.prepareStatement(
                "SELECT album.id, album.gid, album.name, " +
                        "artist.gid as artist_gid, artist.name as artist_name,resolution, " +
                        "attributes, tracks, discids, asin, " +
                        "language.isocode_3t as language, script.isocode as script " +
                        "FROM album " +
                        "JOIN albummeta ON album.id=albummeta.id " +
                        "JOIN artist ON album.artist=artist.id " +
                        "LEFT JOIN language ON album.language=language.id " +
                        "LEFT JOIN script ON album.script=script.id " +
                        "WHERE album.id BETWEEN ? AND ?");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, events));
        }
        st.close();
    }

    public Document documentFromResultSet(ResultSet rs, Map<Integer, List<List<String>>> events) throws SQLException {
        Document doc = new Document();
        int albumId = rs.getInt("id");
        addFieldToDocument(doc, ReleaseIndexField.RELEASE_ID, rs.getString("gid"));
        addFieldToDocument(doc, ReleaseIndexField.RELEASE, rs.getString("name"));
        addFieldToDocument(doc, ReleaseIndexField.ARTIST_ID, rs.getString("artist_gid"));
        addFieldToDocument(doc, ReleaseIndexField.ARTIST, rs.getString("artist_name"));
        String comment = rs.getString("resolution");
        if (comment != null && !comment.isEmpty()) {
        	addFieldToDocument(doc, ReleaseIndexField.ARTIST_COMMENT, comment);
        }
        addFieldToDocument(doc, ReleaseIndexField.NUM_TRACKS, rs.getString("tracks"));
        addFieldToDocument(doc, ReleaseIndexField.NUM_DISC_IDS, rs.getString("discids"));

        Object[] attributes = (Object[]) rs.getArray("attributes").getArray();
        for(int i=1;i<attributes.length;i++)
        {
            int nextVal = (Integer) attributes[i];
            if (nextVal >= ReleaseType.getMinDbId() && nextVal <= ReleaseType.getMaxDbId()) {
                addFieldToDocument(doc, ReleaseIndexField.TYPE, ReleaseType.getByDbId(nextVal).getName());
                break;
            }
        }

        for(int i=1;i<attributes.length;i++)
        {
            int nextVal = (Integer) attributes[i];
            if (nextVal >= ReleaseStatus.getMinDbId() && nextVal <= ReleaseStatus.getMaxDbId()) {
                addFieldToDocument(doc, ReleaseIndexField.STATUS, ReleaseStatus.getByDbId(nextVal).getName());
                break;
            }
        }

        String asin = rs.getString("asin");
        if (asin != null && !asin.isEmpty()) {
            addFieldToDocument(doc, ReleaseIndexField.AMAZON_ID, asin);
        }
        String language = rs.getString("language");
        if (language != null && !language.isEmpty()) {
            addFieldToDocument(doc, ReleaseIndexField.LANGUAGE, language);
        }
        String script = rs.getString("script");
        if (script != null && !script.isEmpty()) {
            addFieldToDocument(doc, ReleaseIndexField.SCRIPT, script);
        }

        if (events.containsKey(albumId)) {
            for (List<String> entry : events.get(albumId)) {
                String str;
                str = entry.get(0);
                if (str == null || str.isEmpty()) {
                    str = "-";
                }
                addFieldToDocument(doc, ReleaseIndexField.COUNTRY, str);

                str = entry.get(1);
                if (str == null || str.isEmpty()) {
                    str = "-";
                }
                addFieldToDocument(doc, ReleaseIndexField.DATE, normalizeDate(str));

                str = entry.get(2);
                if (str == null || str.isEmpty()) {
                    str = "-";
                }
                addFieldToDocument(doc, ReleaseIndexField.LABEL, str);

                str = entry.get(3);
                if (str == null || str.isEmpty()) {
                    str = "-";
                }
                addFieldToDocument(doc, ReleaseIndexField.CATALOG_NO, str);

                str = entry.get(4);
                if (str == null || str.isEmpty()) {
                    str = "-";
                }
                Matcher m = stripBarcodeOfLeadingZeroes.matcher(str);
                addFieldToDocument(doc, ReleaseIndexField.BARCODE, m.replaceFirst(""));

                str = entry.get(5);
                if (str == null || str.isEmpty()) {
                    str = "-";
                }
                else {
                    str = ReleaseFormat.getByDbId(Integer.parseInt(str)).getName();
                }
                addFieldToDocument(doc, ReleaseIndexField.FORMAT,str);

            }
        }
        return doc;
    }

}
