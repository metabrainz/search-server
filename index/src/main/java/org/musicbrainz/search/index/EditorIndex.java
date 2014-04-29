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

import com.google.common.base.Strings;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.similarities.Similarity;
import org.musicbrainz.mmd2.Editor;
import org.musicbrainz.mmd2.ObjectFactory;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.MusicbrainzSimilarity;

import java.io.IOException;
import java.sql.*;

public class EditorIndex extends DatabaseIndex {

    public static final String INDEX_NAME = "editor";


    public EditorIndex(Connection dbConnection) {
        super(dbConnection);
    }

    public EditorIndex() {
    }


    public String getName() {
        return EditorIndex.INDEX_NAME;
    }

    public Analyzer getAnalyzer() {
        return DatabaseIndex.getAnalyzer(EditorIndexField.class);
    }

	@Override
	public IndexField getIdentifierField() {
		return EditorIndexField.ID;
	}
    
    public int getMaxId() throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) FROM editor");
        rs.next();
        return rs.getInt(1);
    }

    public int getNoOfRows(int maxId) throws SQLException {
    	PreparedStatement st = dbConnection.prepareStatement(
    		"SELECT count(*) FROM editor WHERE id <= ?");
    	st.setInt(1, maxId);
    	ResultSet rs = st.executeQuery();
        rs.next();
        return rs.getInt(1);
    }

    @Override
    public Similarity getSimilarity()
    {
        return new MusicbrainzSimilarity();
    }

    @Override
    public void init(IndexWriter indexWriter, boolean isUpdater) throws SQLException {

        addPreparedStatement("EDITORS",
                "SELECT editor.id, editor.name as name," +
                  "  editor.bio  " +
                " FROM editor " +
                " WHERE editor.id BETWEEN ? AND ?" +
                " AND email_confirm_date is not NULL");


    }



    public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {

        ObjectFactory of = new ObjectFactory();
        PreparedStatement st = getPreparedStatement("EDITORS");
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
        Editor editor = of.createEditor();

        int editorId = rs.getInt("id");
        doc.addField(EditorIndexField.ID, editorId);


        String name=rs.getString("name");
        doc.addField(EditorIndexField.EDITOR,name );
        editor.setName(name);

        String bio = rs.getString("bio");
        doc.addFieldOrNoValue(EditorIndexField.BIO, bio);
        if (!Strings.isNullOrEmpty(bio)) {
            editor.setBio(bio);
        }

        String store = MMDSerializer.serialize(editor);
        doc.addField(EditorIndexField.EDITOR_STORE, store);
        return doc.getLuceneDocument();
    }

}
