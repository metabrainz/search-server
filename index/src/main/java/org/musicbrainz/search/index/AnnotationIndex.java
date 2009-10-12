package org.musicbrainz.search.index;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

public class AnnotationIndex extends DatabaseIndex {

	public AnnotationIndex(Connection dbConnection) {
		super(dbConnection);
	}

	public String getName() {
		return "annotation";
	}

	public int getMaxId() throws SQLException {
		Statement st = this.dbConnection.createStatement();
		ResultSet rs = st.executeQuery("SELECT MAX(id) FROM annotation");
		rs.next();
		return rs.getInt(1);
	}

	public void indexData(IndexWriter indexWriter, int min, int max)
	throws SQLException, IOException {
		for(AnnotationType type : AnnotationType.values()) {
			indexDataByType(indexWriter, min, max, type);
		}
	}

	protected void indexDataByType(IndexWriter indexWriter, int min, int max, AnnotationType type)
	throws SQLException, IOException {
		String entityTable = type.getName().equals("release") ? "album" : type.getName();

		PreparedStatement st = dbConnection.prepareStatement(
			"SELECT ? as type, e.gid, e.name, ann.text " +
				"FROM annotation ann " +
				"JOIN (SELECT type, rowid, max(moderation) as last_moderation FROM annotation GROUP BY type, rowid) as last_ann " +
				"	ON ann.rowid = last_ann.rowid AND ann.type = last_ann.type AND ann.moderation = last_ann.last_moderation " +
				"JOIN " + entityTable + " as e ON (ann.rowid = e.id) " +
				"WHERE ann.type = ? AND ann.id BETWEEN ? AND ?");
		st.setString(1, type.getName());
		st.setInt(2, type.getDbId());
		st.setInt(3, min);
		st.setInt(4, max);
		ResultSet rs = st.executeQuery();
		while (rs.next()) {
			indexWriter.addDocument(documentFromResultSet(rs));
		}
		st.close();
	}

	public Document documentFromResultSet(ResultSet rs) throws SQLException {
		Document doc = new Document();
		addFieldToDocument(doc, AnnotationIndexField.MBID, rs.getString("gid"));
		addFieldToDocument(doc, AnnotationIndexField.NAME, rs.getString("name"));
		addFieldToDocument(doc, AnnotationIndexField.TYPE, rs.getString("type"));
		addFieldToDocument(doc, AnnotationIndexField.TEXT, rs.getString("text"));

		return doc;
	}

}
