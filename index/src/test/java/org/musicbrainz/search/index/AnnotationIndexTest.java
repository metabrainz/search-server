package org.musicbrainz.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.musicbrainz.search.analysis.StandardUnaccentAnalyzer;
import org.musicbrainz.search.index.AnnotationIndex;
import org.musicbrainz.search.index.AnnotationIndexField;

import java.sql.Connection;
import java.sql.Statement;


public class AnnotationIndexTest extends AbstractIndexTest {


    public void setUp() throws Exception {
        super.setup();

    }

    private void createIndex(RAMDirectory ramDir) throws Exception {
        PerFieldAnalyzerWrapper analyzer = new AnnotationAnalyzer();
        IndexWriter writer = new IndexWriter(ramDir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        AnnotationIndex li = new AnnotationIndex(createConnection());
        li.indexData(writer, 0, Integer.MAX_VALUE);
        writer.close();
    }


    /**
     * @throws Exception
     */
    private void addAnnotationOne() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO release_name(id,name, refcount)VALUES (1, 'Crocodiles', 0)");
        stmt.addBatch("INSERT INTO release_name(id,name, refcount)VALUES (2, 'Crocodiles (bonus disc)', 0)");
        stmt.addBatch("INSERT INTO release(id, gid, name, artist_credit, release_group, status, packaging,country, " +
                "language, script, date_year, date_month, date_day,barcode, comment, editpending) " +
                "  VALUES (491240,'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1,491240,1,1,1,1, 1, 1, 1, 1, null, null, 1)");
        stmt.addBatch("INSERT INTO annotation(id, editor, text, changelog,created) VALUES (1, 1, 'release annotation', 'change',now())");
        stmt.addBatch("INSERT INTO release_annotation(release, annotation) VALUES (491240, 1)");


        stmt.executeBatch();
        stmt.close();
        conn.close();
    }

     /**
     * @throws Exception
     */
    private void addAnnotationTwo() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();

         stmt.addBatch("INSERT INTO release_name(id,name, refcount)VALUES (1, 'Crocodiles', 0)");
         stmt.addBatch("INSERT INTO release_name(id,name, refcount)VALUES (2, 'Crocodiles (bonus disc)', 0)");
         stmt.addBatch("INSERT INTO release_group( id, gid,name,artist_credit,type,comment,editpending)" +
                   "    VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1, 2, null, 0)");
        stmt.addBatch("INSERT INTO annotation(id, editor, text, changelog,created) VALUES (1, 1, 'release group annotation', 'change',now())");
        stmt.addBatch("INSERT INTO release_group_annotation(release_group, annotation) VALUES (491240, 1)");


        stmt.executeBatch();
        stmt.close();
        conn.close();
    }


     /**
     * @throws Exception
     */
    private void addAnnotationThree() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist_type(id,name)VALUES (1, 'Person');");
        stmt.addBatch("INSERT INTO artist_type(id,name)VALUES (2, 'Group');");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (1,'Farming Incident',1)");
        stmt.addBatch("INSERT INTO artist(id,name, gid, sortname,comment, begindate_year,begindate_month,enddate_year,type,editpending)" +
             " VALUES (521316,1, '4302e264-1cf0-4d1f-aca7-2a6f89e34b36',1,null, 1999,4, null, 2, 0)");

        stmt.addBatch("INSERT INTO annotation(id, editor, text, changelog,created) VALUES (1, 1, 'artist annotation', 'change',now())");
        stmt.addBatch("INSERT INTO artist_annotation(artist, annotation) VALUES (521316, 1)");
        stmt.addBatch("INSERT INTO annotation(id, editor, text, changelog,created) VALUES (2, 1, 'artist annotation newer', 'change',now()+1)");
        stmt.addBatch("INSERT INTO artist_annotation(artist, annotation) VALUES (521316, 2)");


        stmt.executeBatch();
        stmt.close();
        conn.close();
    }

      /**
     * @throws Exception
     */
    private void addAnnotationFour() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO label_name (id, name) VALUES (1, '4AD')");
		stmt.addBatch("INSERT INTO label_name (id, name) VALUES (2, '4AD US')");
		stmt.addBatch("INSERT INTO label_type (id, name) VALUES (4, 'Original Production')");

        stmt.addBatch("INSERT INTO label(id, gid, name, sortname, type, labelcode, country, comment, " +
					"	begindate_year, begindate_month, begindate_day, enddate_year, enddate_month, enddate_day) " +
					"VALUES (1, 'a539bb1e-f2e1-4b45-9db8-8053841e7503', 1, 1, 4, 5807, null, null, " +
					"	1979, null, null, null, null, null)");
        stmt.addBatch("INSERT INTO annotation(id, editor, text, changelog,created) VALUES (1, 1, 'label annotation', 'change',now())");
        stmt.addBatch("INSERT INTO label_annotation(label, annotation) VALUES (1, 1)");
        stmt.addBatch("INSERT INTO annotation(id, editor, text, changelog,created) VALUES (2, 1, 'label annotation newer', 'change',now() + 1)");
        stmt.addBatch("INSERT INTO label_annotation(label, annotation) VALUES (1, 2)");


        stmt.executeBatch();
        stmt.close();
        conn.close();
    }

      /**
     * @throws Exception
     */
    private void addAnnotationFive() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();


        stmt.addBatch("INSERT INTO recording(id, gid, name, artist_credit, length, comment, editpending)"
                       + "VALUES (1, '2f250ed2-6285-40f1-aa2a-14f1c05e9765', 1,1,33000, null,1)");
        stmt.addBatch("INSERT INTO track_name(id, name, refcount)VALUES (1, 'Do It Clean', 1) ");
        stmt.addBatch("INSERT INTO annotation(id, editor, text, changelog,created) VALUES (1, 1, 'track annotation', 'change',now())");
        stmt.addBatch("INSERT INTO recording_annotation(recording, annotation) VALUES (1, 1)");
        stmt.addBatch("INSERT INTO annotation(id, editor, text, changelog,created) VALUES (2, 1, 'track annotation newer', 'change',now() + 1)");
        stmt.addBatch("INSERT INTO recording_annotation(recording, annotation) VALUES (1, 2)");

        stmt.executeBatch();
        stmt.close();
        conn.close();
    }

    /**
     * Basic test for Release Annotation
     *
     * @throws Exception
     */

    public void testReleaseIndexAnnotationFields() throws Exception {
        addAnnotationOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(AnnotationIndexField.MBID.getName()).length);
            assertEquals("c3b8dbc9-c1ff-4743-9015-8d762819134e", doc.getField(AnnotationIndexField.MBID.getName()).stringValue());
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
     * @throws Exception
     */

    public void testReleaseGroupIndexAnnotationFields() throws Exception {
        addAnnotationTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(AnnotationIndexField.MBID.getName()).length);
            assertEquals("efd2ace2-b3b9-305f-8a53-9803595c0e37", doc.getField(AnnotationIndexField.MBID.getName()).stringValue());
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
     * @throws Exception
     */

    public void testArtistIndexAnnotationFields() throws Exception {
        addAnnotationThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(AnnotationIndexField.MBID.getName()).length);
            assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.getField(AnnotationIndexField.MBID.getName()).stringValue());
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
     * @throws Exception
     */

    public void testLabelIndexAnnotationFields() throws Exception {
        addAnnotationFour();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(AnnotationIndexField.MBID.getName()).length);
            assertEquals("a539bb1e-f2e1-4b45-9db8-8053841e7503", doc.getField(AnnotationIndexField.MBID.getName()).stringValue());
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
     * @throws Exception
     */

    public void testRecordingIndexAnnotationFields() throws Exception {
        addAnnotationFive();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        //assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(AnnotationIndexField.MBID.getName()).length);
            assertEquals("2f250ed2-6285-40f1-aa2a-14f1c05e9765", doc.getField(AnnotationIndexField.MBID.getName()).stringValue());
            assertEquals(1, doc.getFields(AnnotationIndexField.NAME.getName()).length);
            assertEquals("Do It Clean", doc.getField(AnnotationIndexField.NAME.getName()).stringValue());
            assertEquals(1, doc.getFields(AnnotationIndexField.TEXT.getName()).length);
            assertEquals("track annotation newer", doc.getField(AnnotationIndexField.TEXT.getName()).stringValue());
            assertEquals(1, doc.getFields(AnnotationIndexField.TYPE.getName()).length);
            assertEquals("track", doc.getField(AnnotationIndexField.TYPE.getName()).stringValue());

        }
        ir.close();
    }

    public void testGetTypeByDbId () throws Exception {
        assertNull(AnnotationType.getByDbId(0));
        assertEquals(AnnotationType.ARTIST,AnnotationType.getByDbId(1));
    }
}


