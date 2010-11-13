package org.musicbrainz.search.index;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.sql.Connection;
import java.sql.Statement;


public class AnnotationIndexTest extends AbstractIndexTest {


    public void setUp() throws Exception {
        super.setup();
    }

    private void createIndex(RAMDirectory ramDir) throws Exception {
        PerFieldAnalyzerWrapper analyzer = new PerFieldEntityAnalyzer(AnnotationIndexField.class);
        IndexWriter writer = new IndexWriter(ramDir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        AnnotationIndex ai = new AnnotationIndex(createConnection());
        ai.init(writer);
        ai.addMetaInformation(writer);
        ai.indexData(writer, 0, Integer.MAX_VALUE);
        ai.destroy();
        writer.close();
    }


    /**
     * @throws Exception exception
     */
    private void addReleaseAnnotation() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (2, 'Crocodiles (bonus disc)')");
        stmt.addBatch("INSERT INTO release (id, gid, name, artist_credit, release_group) " +
                "  VALUES (491240, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1, 491240)");
        
        stmt.addBatch("INSERT INTO annotation (id, editor, text, changelog, created) " +
        	"  VALUES (1, 1, 'release annotation', 'change', now())");
        stmt.addBatch("INSERT INTO release_annotation (release, annotation) VALUES (491240, 1)");

        stmt.executeBatch();
        stmt.close();
        conn.close();
    }

     /**
     * @throws Exception exception
     */
    private void addReleaseGroupAnnotation() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (1, 'Crocodiles')");
        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit)" +
                    "  VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1)");
         
        stmt.addBatch("INSERT INTO annotation (id, editor, text, changelog, created) " +
                    "  VALUES (1, 1, 'release group annotation', 'change', now())");
        stmt.addBatch("INSERT INTO release_group_annotation (release_group, annotation) VALUES (491240, 1)");

        stmt.executeBatch();
        stmt.close();
        conn.close();
    }


     /**
     * @throws Exception exception
     */
    private void addArtistAnnotation() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Farming Incident')");
        stmt.addBatch("INSERT INTO artist (id, name, gid, sort_name)" +
                    "  VALUES (521316, 1, '4302e264-1cf0-4d1f-aca7-2a6f89e34b36', 1)");

        stmt.addBatch("INSERT INTO annotation (id, editor, text, changelog, created) " +
                    "  VALUES (1, 1, 'artist annotation', 'change', now())");
        stmt.addBatch("INSERT INTO artist_annotation (artist, annotation) VALUES (521316, 1)");
        
        stmt.addBatch("INSERT INTO annotation (id, editor, text, changelog, created) " +
                    "  VALUES (2, 1, 'artist annotation newer', 'change', now()+1)");
        stmt.addBatch("INSERT INTO artist_annotation (artist, annotation) VALUES (521316, 2)");

        stmt.executeBatch();
        stmt.close();
        conn.close();
    }

      /**
     * @throws Exception  exception
     */
    private void addLabelAnnotation() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO label_name (id, name) VALUES (1, '4AD')");
        stmt.addBatch("INSERT INTO label (id, gid, name, sort_name) " +
                "  VALUES (1, 'a539bb1e-f2e1-4b45-9db8-8053841e7503', 1, 1)");
        
        stmt.addBatch("INSERT INTO annotation (id, editor, text, changelog, created) " +
        	"  VALUES (1, 1, 'label annotation', 'change', now())");
        stmt.addBatch("INSERT INTO label_annotation (label, annotation) VALUES (1, 1)");
        
        stmt.addBatch("INSERT INTO annotation (id, editor, text, changelog, created) " +
        	"  VALUES (2, 1, 'label annotation newer', 'change', now()+1)");
        stmt.addBatch("INSERT INTO label_annotation (label, annotation) VALUES (1, 2)");

        stmt.executeBatch();
        stmt.close();
        conn.close();
    }

      /**
     * @throws Exception exception
     */
    private void addRecordingAnnotation() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO track_name (id, name) VALUES (1, 'Do It Clean') ");
        stmt.addBatch("INSERT INTO recording (id, gid, name, artist_credit)"
                       + "VALUES (1, '2f250ed2-6285-40f1-aa2a-14f1c05e9765', 1, 1)");
        
        stmt.addBatch("INSERT INTO annotation (id, editor, text, changelog, created) " +
        	"  VALUES (1, 1, 'recording annotation', 'change', now())");
        stmt.addBatch("INSERT INTO recording_annotation (recording, annotation) VALUES (1, 1)");
        
        stmt.addBatch("INSERT INTO annotation (id, editor, text, changelog, created) " +
        	"  VALUES (2, 1, 'recording annotation newer', 'change', now()+1)");
        stmt.addBatch("INSERT INTO recording_annotation (recording, annotation) VALUES (1, 2)");

        stmt.executeBatch();
        stmt.close();
        conn.close();
    }

    /**
     * Basic test for Release Annotation
     *
     * @throws Exception exception
     */

    public void testReleaseIndexAnnotationFields() throws Exception {
        addReleaseAnnotation();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);
        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(AnnotationIndexField.ENTITY.getName()).length);
            assertEquals("c3b8dbc9-c1ff-4743-9015-8d762819134e", doc.getField(AnnotationIndexField.ENTITY.getName()).stringValue());
            assertEquals(1, doc.getFields(AnnotationIndexField.NAME.getName()).length);
            assertEquals("Crocodiles (bonus disc)", doc.getField(AnnotationIndexField.NAME.getName()).stringValue());
            assertEquals(1, doc.getFields(AnnotationIndexField.TEXT.getName()).length);
            assertEquals("release annotation", doc.getField(AnnotationIndexField.TEXT.getName()).stringValue());
            assertEquals(1, doc.getFields(AnnotationIndexField.TYPE.getName()).length);
            assertEquals("release", doc.getField(AnnotationIndexField.TYPE.getName()).stringValue());
        }
        ir.close();
    }


     /**
     * Basic test for Release Group Annotation
     *
     * @throws Exception exception
     */

    public void testReleaseGroupIndexAnnotationFields() throws Exception {
        addReleaseGroupAnnotation();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(AnnotationIndexField.ENTITY.getName()).length);
            assertEquals("efd2ace2-b3b9-305f-8a53-9803595c0e37", doc.getField(AnnotationIndexField.ENTITY.getName()).stringValue());
            assertEquals(1, doc.getFields(AnnotationIndexField.NAME.getName()).length);
            assertEquals("Crocodiles", doc.getField(AnnotationIndexField.NAME.getName()).stringValue());
            assertEquals(1, doc.getFields(AnnotationIndexField.TEXT.getName()).length);
            assertEquals("release group annotation", doc.getField(AnnotationIndexField.TEXT.getName()).stringValue());
            assertEquals(1, doc.getFields(AnnotationIndexField.TYPE.getName()).length);
            assertEquals("releasegroup", doc.getField(AnnotationIndexField.TYPE.getName()).stringValue());
        }
        ir.close();
    }

       /**
     * Basic test for Artist Annotation
     *
     * @throws Exception exception
     */

    public void testArtistIndexAnnotationFields() throws Exception {
        addArtistAnnotation();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(AnnotationIndexField.ENTITY.getName()).length);
            assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.getField(AnnotationIndexField.ENTITY.getName()).stringValue());
            assertEquals(1, doc.getFields(AnnotationIndexField.NAME.getName()).length);
            assertEquals("Farming Incident", doc.getField(AnnotationIndexField.NAME.getName()).stringValue());
            assertEquals(1, doc.getFields(AnnotationIndexField.TEXT.getName()).length);
            assertEquals("artist annotation newer", doc.getField(AnnotationIndexField.TEXT.getName()).stringValue());
            assertEquals(1, doc.getFields(AnnotationIndexField.TYPE.getName()).length);
            assertEquals("artist", doc.getField(AnnotationIndexField.TYPE.getName()).stringValue());
        }
        ir.close();
    }

       /**
     * Basic test for Label Annotation
     *
     * @throws Exception exception
     */

    public void testLabelIndexAnnotationFields() throws Exception {
        addLabelAnnotation();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(AnnotationIndexField.ENTITY.getName()).length);
            assertEquals("a539bb1e-f2e1-4b45-9db8-8053841e7503", doc.getField(AnnotationIndexField.ENTITY.getName()).stringValue());
            assertEquals(1, doc.getFields(AnnotationIndexField.NAME.getName()).length);
            assertEquals("4AD", doc.getField(AnnotationIndexField.NAME.getName()).stringValue());
            assertEquals(1, doc.getFields(AnnotationIndexField.TEXT.getName()).length);
            assertEquals("label annotation newer", doc.getField(AnnotationIndexField.TEXT.getName()).stringValue());
            assertEquals(1, doc.getFields(AnnotationIndexField.TYPE.getName()).length);
            assertEquals("label", doc.getField(AnnotationIndexField.TYPE.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * Basic test for Recording Annotation
     *
     * @throws Exception exception
     */

    public void testRecordingIndexAnnotationFields() throws Exception {
        addRecordingAnnotation();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(AnnotationIndexField.ENTITY.getName()).length);
            assertEquals("2f250ed2-6285-40f1-aa2a-14f1c05e9765", doc.getField(AnnotationIndexField.ENTITY.getName()).stringValue());
            assertEquals(1, doc.getFields(AnnotationIndexField.NAME.getName()).length);
            assertEquals("Do It Clean", doc.getField(AnnotationIndexField.NAME.getName()).stringValue());
            assertEquals(1, doc.getFields(AnnotationIndexField.TEXT.getName()).length);
            assertEquals("recording annotation newer", doc.getField(AnnotationIndexField.TEXT.getName()).stringValue());
            assertEquals(1, doc.getFields(AnnotationIndexField.TYPE.getName()).length);
            assertEquals("recording", doc.getField(AnnotationIndexField.TYPE.getName()).stringValue());
        }
        ir.close();
    }

    public void testGetTypeByDbId () throws Exception {
        assertNull(AnnotationType.getByDbId(0));
        assertEquals(AnnotationType.ARTIST,AnnotationType.getByDbId(1));
    }
}


