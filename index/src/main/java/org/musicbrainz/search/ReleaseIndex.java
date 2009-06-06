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

package org.musicbrainz.search;

import java.io.*;
import java.util.*;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Document;

import java.sql.*;

public class ReleaseIndex extends Index {


    public String getName() {
        return "release";
    }

    public int getMaxId(Connection conn) throws SQLException {
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM album");
        rs.next();
        return rs.getInt(1);
    }

    public void indexData(IndexWriter indexWriter, Connection conn, int min, int max) throws SQLException, IOException {
        Map<Integer, List<List<String>>> events = new HashMap<Integer, List<List<String>>>();
        PreparedStatement st = conn.prepareStatement(
                "SELECT album, lower(isocode) as country, releasedate, label.name as label, catno, barcode " +
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
            list.add(entry);
        }
        st.close();
        st = conn.prepareStatement(
                "SELECT album.id, album.gid, album.name, " +
                        "artist.gid as artist_gid, artist.name as artist_name, " +
                        "attributes, tracks, discids, asin, language.isocode_3b, script.isocode  " +
                        "FROM album " +
                        "JOIN albummeta ON album.id=albummeta.id " +
                        "JOIN artist ON album.artist=artist.id " +
                        "LEFT JOIN language ON album.language=language.id " +
                        "LEFT JOIN script on script=script.id " +
                        "WHERE album.id BETWEEN ? AND ?");
        st.setInt(1, min);
        st.setInt(2, max);
        rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs, events));
        }
        st.close();
    }

    //TODO Release Type, Release Status
    public Document documentFromResultSet(ResultSet rs, Map<Integer, List<List<String>>> events) throws SQLException {
        Document doc = new Document();
        int albumId = rs.getInt("id");
        addReleaseGidToDocument(doc, rs.getString("gid"));
        addReleaseToDocument(doc, rs.getString("name"));
        addArtistGidToDocument(doc, rs.getString("artist_gid"));
        addArtistToDocument(doc, rs.getString("artist_name"));
        addNumTracksToDocument(doc, rs.getString("tracks"));
        addDiscIdsToDocument(doc, rs.getString("discids"));

        String asin = rs.getString("asin");
        if (asin != null && !asin.isEmpty()) {
            addAsinToDocument(doc, asin);
        }

        String langCode = rs.getString("isocode_3b");
        if (langCode != null) {
            addLanguageToDocument(doc, langCode);
        }

        String scriptCode = rs.getString("isocode");
        if (scriptCode != null) {
            addScriptToDocument(doc, scriptCode);
        }

        if (events.containsKey(albumId)) {
            for (List<String> entry : events.get(albumId)) {
                String str;
                str = entry.get(0);
                if (str == null || str.isEmpty()) {
                    str = "-";
                }
                addCountryToDocument(doc, str);

                str = entry.get(1);
                if (str == null || str.isEmpty()) {
                    str = "-";
                }
                addDateToDocument(doc, normalizeDate(str));

                str = entry.get(2);
                if (str == null || str.isEmpty()) {
                    str = "-";
                }
                addLabelToDocument(doc, str);

                str = entry.get(3);
                if (str == null || str.isEmpty()) {
                    str = "-";
                }
                addCatalogNoToDocument(doc, str);

                str = entry.get(4);
                if (str == null || str.isEmpty()) {
                    str = "-";
                }
                addBarcodeToDocument(doc, str);
            }
        }
        return doc;
    }

    public void addArtistGidToDocument(Document doc, String artistId) {
        doc.add(new Field(ReleaseIndexFieldName.ARTIST_ID.getFieldname(), artistId, Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    public void addArtistToDocument(Document doc, String artist) {
        doc.add(new Field(ReleaseIndexFieldName.ARTIST.getFieldname(), artist, Field.Store.YES, Field.Index.ANALYZED));
    }

    public void addReleaseGidToDocument(Document doc, String releaseId) {
        doc.add(new Field(ReleaseIndexFieldName.RELEASE_ID.getFieldname(), releaseId, Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    public void addReleaseToDocument(Document doc, String release) {
        doc.add(new Field(ReleaseIndexFieldName.RELEASE.getFieldname(), release, Field.Store.YES, Field.Index.ANALYZED));
    }

    public void addNumTracksToDocument(Document doc, String numTracks) {
        doc.add(new Field(ReleaseIndexFieldName.NUM_TRACKS.getFieldname(), numTracks, Field.Store.YES, Field.Index.ANALYZED));
    }

    public void addDiscIdsToDocument(Document doc, String discIds) {
        doc.add(new Field(ReleaseIndexFieldName.NUM_DISC_IDS.getFieldname(), discIds, Field.Store.YES, Field.Index.ANALYZED));
    }

    public void addAsinToDocument(Document doc, String asin) {
        doc.add(new Field(ReleaseIndexFieldName.AMAZON_ID.getFieldname(), asin, Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    public void addLanguageToDocument(Document doc, String language) {
        doc.add(new Field(ReleaseIndexFieldName.LANGUAGE.getFieldname(), language, Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    public void addScriptToDocument(Document doc, String script) {
        doc.add(new Field(ReleaseIndexFieldName.SCRIPT.getFieldname(), script, Field.Store.YES, Field.Index.NOT_ANALYZED));
    }


    public void addCountryToDocument(Document doc, String country) {
        doc.add(new Field(ReleaseIndexFieldName.COUNTRY.getFieldname(), country, Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    public void addDateToDocument(Document doc, String date) {
        doc.add(new Field(ReleaseIndexFieldName.DATE.getFieldname(), date, Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    public void addLabelToDocument(Document doc, String label) {
        doc.add(new Field(ReleaseIndexFieldName.LABEL.getFieldname(), label, Field.Store.YES, Field.Index.ANALYZED));
    }

    public void addCatalogNoToDocument(Document doc, String catalogNo) {
        doc.add(new Field(ReleaseIndexFieldName.CATALOG_NO.getFieldname(), catalogNo, Field.Store.YES, Field.Index.ANALYZED));
    }

    public void addBarcodeToDocument(Document doc, String barcode) {
        doc.add(new Field(ReleaseIndexFieldName.BARCODE.getFieldname(), barcode, Field.Store.YES, Field.Index.ANALYZED));
    }

}
