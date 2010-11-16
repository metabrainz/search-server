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
import org.apache.lucene.util.NumericUtils;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.io.IOException;
import java.sql.*;

public class CDStubIndex extends DatabaseIndex {

    public CDStubIndex() {
    }

    public CDStubIndex(Connection dbConnection) {
        super(dbConnection);
    }

    public String getName() {
        return "cdstub";
    }

    public Analyzer getAnalyzer()
    {
        return new PerFieldEntityAnalyzer(CDStubIndexField.class);
    }

	@Override
	public IndexField getIdentifierField() {
		return CDStubIndexField.ID;
	}

    
    public int getMaxId() throws SQLException {
        Statement st = this.dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(release_raw.id) FROM release_raw");
        rs.next();
        return rs.getInt(1);
    }

    public int getNoOfRows(int maxId) throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT count(*) FROM release_raw WHERE id<="+maxId);
        rs.next();
        return rs.getInt(1);
    }

    @Override
    public void init(IndexWriter indexWriter) throws SQLException {
        addPreparedStatement("CDSTUBS",
                "SELECT release_raw.id, release_raw.title, release_raw.artist, barcode, comment, discid, added, count(track_raw.id) as tracks " +
                " FROM release_raw " +
                "  JOIN cdtoc_raw ON release_raw.id = cdtoc_raw.release " +
                "  JOIN track_raw ON track_raw.release = release_raw.id " +
                " WHERE release_raw.id BETWEEN ? AND ? " +
                " GROUP BY release_raw.title, release_raw.id, release_raw.artist, barcode, comment, discid, added");
    }

    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {

        PreparedStatement st = getPreparedStatement("CDSTUBS");
        st.setInt(1, min);
        st.setInt(2, max);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs));
        }
    }

    public Document documentFromResultSet(ResultSet rs) throws SQLException {
        MbDocument doc = new MbDocument();
        doc.addField(CDStubIndexField.ID, rs.getString("id"));
        doc.addField(CDStubIndexField.TITLE, rs.getString("title"));
        doc.addField(CDStubIndexField.ARTIST, rs.getString("artist"));
        doc.addField(CDStubIndexField.DISCID, rs.getString("discid"));
        doc.addField(CDStubIndexField.ADDED, NumericUtils.longToPrefixCoded(rs.getTimestamp("added").getTime()));
        
        //TODO Should really index as number
        doc.addField(CDStubIndexField.NUM_TRACKS, rs.getString("tracks"));

        doc.addNonEmptyField(CDStubIndexField.BARCODE, rs.getString("barcode"));
        doc.addNonEmptyField(CDStubIndexField.COMMENT, rs.getString("comment"));
        return doc.getLuceneDocument();
    }

}
