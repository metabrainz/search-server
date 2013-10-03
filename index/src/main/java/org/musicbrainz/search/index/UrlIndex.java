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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.MbDocument;

import java.io.IOException;
import java.sql.*;


public class UrlIndex extends DatabaseIndex {

    private static final String ARTIST_RELATION_TYPE = "artist";

    public static final String INDEX_NAME = "url";

    public UrlIndex(Connection dbConnection) {
        super(dbConnection);
    }

    public UrlIndex() { }

    public String getName() {
        return UrlIndex.INDEX_NAME;
    }

	@Override
	public IndexField getIdentifierField() {
		return TagIndexField.ID;
	}
	
    public Analyzer getAnalyzer() {
        return DatabaseIndex.getAnalyzer(TagIndexField.class);
    }

    public int getMaxId() throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM url");
        rs.next();
        return rs.getInt(1);
    }

    public int getNoOfRows(int maxId) throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT count(*) FROM url WHERE id<="+maxId);
        rs.next();
        return rs.getInt(1);
    }

    @Override
    public void init(IndexWriter indexWriter, boolean isUpdater) throws SQLException {


        addPreparedStatement("ARTIST_URL",
                        "SELECT u.id, " +
                        " u.gid," +
                        " u.url," +
                        " a.gid as aid," +
                        " a.name as artist_name," +
                        " a.sort_name as artist_sortname," +
                        " lt.name as link" +
                        " FROM l_artist_url lau" +
                        " INNER JOIN url u        ON u.id       = lau.entity1" +
                        " INNER JOIN artist a     ON a.id       = lau.entity0" +
                        " INNER JOIN link l       ON lau.link   = l.id " +
                        " INNER JOIN link_type lt ON l.link_type=lt.id" +
                        " WHERE u.id BETWEEN ? AND ? " +
                        " ORDER BY u.id");
    }


    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {

        PreparedStatement st = getPreparedStatement("ARTIST_URL");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs));
        }
        rs.close();

    }

    public Document documentFromResultSet(ResultSet rs) throws SQLException {
        MbDocument doc = new MbDocument();
        ObjectFactory of = new ObjectFactory();
        Url url = of.createUrl();

        doc.addField(UrlIndexField.ID, rs.getString("id"));

        String guid = rs.getString("gid");
        doc.addField(UrlIndexField.URL_ID, guid);
        url.setId(guid);

        String title = rs.getString("url");
        doc.addField(UrlIndexField.URL, title);
        url.setResource(title);

        String artistId = rs.getString("aid");
        Artist artist = new ObjectFactory().createArtist();
        artist.setId(artistId);
        artist.setName(rs.getString("artist_name"));
        artist.setSortName(rs.getString("artist_sortname"));
        Relation relation = of.createRelation();
        relation.setArtist(artist);
        relation.setType(rs.getString("link"));

        Target target = of.createTarget();
        target.setId(artistId);
        doc.addField(UrlIndexField.TARGET_ID, artistId);
        relation.setTarget(target);

        RelationList relationList = of.createRelationList();
        relationList.setTargetType(ARTIST_RELATION_TYPE);
        doc.addField(UrlIndexField.TARGET_TYPE,ARTIST_RELATION_TYPE);
        relationList.getRelation().add(relation);
        url.getRelationList().add(relationList);

        String store = MMDSerializer.serialize(url);
        doc.addField(UrlIndexField.URL_STORE, store);

        return doc.getLuceneDocument();
    }

}