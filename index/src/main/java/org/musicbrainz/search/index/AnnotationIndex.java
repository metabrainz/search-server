package org.musicbrainz.search.index;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.analysis.Analyzer;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

public class AnnotationIndex extends DatabaseIndex {

	public AnnotationIndex(Connection dbConnection) {
		super(dbConnection);
	}

	public String getName() {
		return "annotation";
	}

    public Analyzer getAnalyzer()
    {
        return new PerFieldEntityAnalyzer(AnnotationIndexField.class);
    }
    
    public int getMaxId() throws SQLException {
		Statement st = this.dbConnection.createStatement();
		ResultSet rs = st.executeQuery("SELECT MAX(id) FROM annotation");
		rs.next();
		return rs.getInt(1);
	}

    public int getNoOfRows(int maxId) throws SQLException {
        Statement st = dbConnection.createStatement();
        ResultSet rs = st.executeQuery("SELECT count(*) FROM annotation WHERE id<="+maxId);
        rs.next();
        return rs.getInt(1);
    }

    public void indexData(IndexWriter indexWriter, int min, int max)
	throws SQLException, IOException {
        indexArtistData(indexWriter,min,max);
        indexLabelData(indexWriter,min,max);
        indexRecordingData(indexWriter,min,max);
        indexReleaseData(indexWriter,min,max);
        indexReleaseGroupData(indexWriter,min,max);
    }

    protected void indexArtistData(IndexWriter indexWriter, int min, int max)
	throws SQLException, IOException {
        PreparedStatement st = dbConnection.prepareStatement(

            "select at.gid,a.text,an.name " +
            "from annotation a " +
            "inner join artist_annotation aa on a.id=aa.annotation " +
            "inner join (select distinct aa2.artist as id,max(created) as created_date from annotation a2 " +
            "            inner join artist_annotation aa2 on a2.id=aa2.annotation group by aa2.artist) AS last_ann " +
            "on    aa.artist=last_ann.id "  +
            "and   a.created=last_ann.created_date "  +
            "inner join artist at on aa.artist=at.id " +
            "inner join artist_name an on at.name=an.id " +
            "where a.id between ? and ? " );
		st.setInt(1, min);
		st.setInt(2, max);
		ResultSet rs = st.executeQuery();
		while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs,AnnotationType.ARTIST));
		}
		st.close();
    }

    protected void indexLabelData(IndexWriter indexWriter, int min, int max)
	throws SQLException, IOException {
         PreparedStatement st = dbConnection.prepareStatement(

            "select r.gid,a.text,rn.name " +
            "from annotation a " +
            "inner join label_annotation ra on a.id=ra.annotation " +
            "inner join (select distinct ra2.label as id,max(created) as created_date from annotation a2 " +
            "            inner join label_annotation ra2 on a2.id=ra2.annotation group by ra2.label) AS last_ann " +
            "on    ra.label=last_ann.id "  +
            "and   a.created=last_ann.created_date "  +
            "inner join label r on ra.label=r.id " +
            "inner join label_name rn on r.name=rn.id " +
            "where a.id between ? and ? " );
		st.setInt(1, min);
		st.setInt(2, max);
		ResultSet rs = st.executeQuery();
		while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs,AnnotationType.LABEL));
		}
		st.close();
    }

    protected void indexRecordingData(IndexWriter indexWriter, int min, int max)
        throws SQLException, IOException {
             PreparedStatement st = dbConnection.prepareStatement(

                "select r.gid,a.text,rn.name " +
                "from annotation a " +
                "inner join recording_annotation ra on a.id=ra.annotation " +
                "inner join (select distinct ra2.recording as id,max(created) as created_date from annotation a2 " +
                "            inner join recording_annotation ra2 on a2.id=ra2.annotation group by ra2.recording) AS last_ann " +
                "on    ra.recording=last_ann.id "  +
                "and   a.created=last_ann.created_date "  +
                "inner join recording r on ra.recording=r.id " +
                "inner join track_name rn on r.name=rn.id " +
                "where a.id between ? and ? " );
            st.setInt(1, min);
            st.setInt(2, max);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                indexWriter.addDocument(documentFromResultSet(rs,AnnotationType.TRACK));
            }
            st.close();
        }


    protected void indexReleaseData(IndexWriter indexWriter, int min, int max)
	throws SQLException, IOException {
         PreparedStatement st = dbConnection.prepareStatement(

            "select r.gid,a.text,rn.name " +
            "from annotation a " +
            "inner join release_annotation ra on a.id=ra.annotation " +
            "inner join (select distinct ra2.release as id,max(created) as created_date from annotation a2 " +
            "            inner join release_annotation ra2 on a2.id=ra2.annotation group by ra2.release) AS last_ann " +
            "on    ra.release=last_ann.id "  +
            "and   a.created=last_ann.created_date "  +
            "inner join release r on ra.release=r.id " +
            "inner join release_name rn on r.name=rn.id " +
            "where a.id between ? and ? " );
		st.setInt(1, min);
		st.setInt(2, max);
		ResultSet rs = st.executeQuery();
		while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs,AnnotationType.RELEASE));
		}
		st.close();
    }

    protected void indexReleaseGroupData(IndexWriter indexWriter, int min, int max)
	    throws SQLException, IOException {
         PreparedStatement st = dbConnection.prepareStatement(

            "select r.gid,a.text,rn.name " +
            "from annotation a " +
            "inner join release_group_annotation ra on a.id=ra.annotation " +
            "inner join (select ra2.release_group as id,max(created) as created_date from annotation a2 " +
            "            inner join release_group_annotation ra2 on a2.id=ra2.annotation group by ra2.release_group) AS last_ann " +
            "on    ra.release_group=last_ann.id "  +
            "and   a.created=last_ann.created_date "  +
            "inner join release_group r on ra.release_group=r.id " +
            "inner join release_name rn on r.name=rn.id " +
            "where a.id between ? and ? " );
		st.setInt(1, min);
		st.setInt(2, max);
		ResultSet rs = st.executeQuery();
		while (rs.next()) {
            indexWriter.addDocument(documentFromResultSet(rs,AnnotationType.RELEASE_GROUP));
		}
		st.close();
    }

    public Document documentFromResultSet(ResultSet rs,AnnotationType type) throws SQLException {
        MbDocument doc = new MbDocument();
        doc.addField(AnnotationIndexField.MBID, rs.getString("gid"));
        doc.addField(AnnotationIndexField.NAME, rs.getString("name"));
	    doc.addField(AnnotationIndexField.TYPE, type.getName());
	    doc.addField(AnnotationIndexField.TEXT, rs.getString("text"));
        return doc.getLuceneDocument();
    }
}
