package org.musicbrainz.search.index;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.sql.Statement;


public class WorkIndexTest extends AbstractIndexTest {

    public void setUp() throws Exception {
        super.setup();
    }

    private void createIndex(RAMDirectory ramDir) throws Exception {
        CommonTables ct = new CommonTables(conn);
        ct.createTemporaryTables();
        PerFieldAnalyzerWrapper analyzer = new PerFieldEntityAnalyzer(WorkIndexField.class);
        IndexWriter writer = new IndexWriter(ramDir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        WorkIndex wi = new WorkIndex(conn);
        wi.init(writer, false);
        wi.addMetaInformation(writer);
        wi.indexData(writer, 0, Integer.MAX_VALUE);
        wi.destroy();
        writer.close();

    }

    private void addWorkOne() throws Exception {

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Echo & The Bunnymen')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (2, 'Echo and The Bunnymen')");

        stmt.addBatch("INSERT INTO artist (id, gid, name, sort_name, comment)" +
                " VALUES (16153, 'ccd4879c-5e88-4385-b131-bf65296bf245', 1, 2, 'a comment')");
        stmt.addBatch("INSERT INTO artist_credit (id, name, artist_count, ref_count) VALUES (1, 1, 1, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name) " +
                " VALUES (1, 0, 16153, 1)");

        stmt.addBatch("INSERT INTO work_name (id, name) VALUES (1, 'Work')");
        stmt.addBatch("INSERT INTO work_name (id, name) VALUES (2, 'Play')");
        stmt.addBatch("INSERT INTO work (id, gid, name, artist_credit, iswc)" +
                " VALUES (1, 'a539bb1e-f2e1-4b45-9db8-8053841e7503', 1, 1, 'T-101779304-1')");
        stmt.addBatch("INSERT INTO work_alias (work, name) VALUES (1, 2)");

        stmt.addBatch("INSERT INTO tag (id, name, ref_count) VALUES (1, 'Classical', 2);");
        stmt.addBatch("INSERT INTO work_tag (work, tag, count) VALUES (1, 1, 10)");


        stmt.executeBatch();
        stmt.close();
    }

    private void addWorkTwo() throws Exception {

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Echo & The Bunnymen')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (2, 'Echo and The Bunnymen')");

        stmt.addBatch("INSERT INTO artist (id, gid, name, sort_name, comment)" +
                " VALUES (16153, 'ccd4879c-5e88-4385-b131-bf65296bf245', 1, 2, 'a comment')");

        stmt.addBatch("INSERT INTO artist_credit (id, name, artist_count, ref_count) VALUES (1, 1, 1, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name) " +
                " VALUES (1, 0, 16153, 1)");

        stmt.addBatch("INSERT INTO work_name (id, name) VALUES (1, 'Work')");
        stmt.addBatch("INSERT INTO work_type (id, name) VALUES (1, 'Opera')");
        
        stmt.addBatch("INSERT INTO work (id, gid, name, artist_credit, type, iswc)" +
                " VALUES (1, 'a539bb1e-f2e1-4b45-9db8-8053841e7503', 1, 1, 1, 'T-101779304-1')");

        stmt.executeBatch();
        stmt.close();
    }

    public void testIndexWork() throws Exception {

        addWorkOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);
        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(WorkIndexField.WORK.getName()).length);
            assertEquals("Work", doc.getField(WorkIndexField.WORK.getName()).stringValue());
            assertEquals(1, doc.getFields(WorkIndexField.ISWC.getName()).length);
            assertEquals("T-101779304-1", doc.getField(WorkIndexField.ISWC.getName()).stringValue());
            assertEquals(0, doc.getFields(WorkIndexField.TYPE.getName()).length);
            ir.close();
        }
    }

    public void testIndexWorkWithoutType() throws Exception {

        addWorkOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);
        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(0, doc.getFields(WorkIndexField.TYPE.getName()).length);
            ir.close();
        }
    }

    public void testIndexWorkWithType() throws Exception {

        addWorkTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);
        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(WorkIndexField.TYPE.getName()).length);
            assertEquals("opera", doc.getField(WorkIndexField.TYPE.getName()).stringValue());
            ir.close();
        }
    }

     public void testIndexWorkWithAlias() throws Exception {

        addWorkOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);
        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(WorkIndexField.ALIAS.getName()).length);
            assertEquals("Play", doc.getField(WorkIndexField.ALIAS.getName()).stringValue());
            ir.close();
        }
    }

    public void testIndexWorkWithNoAlias() throws Exception {

        addWorkTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);
        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(0, doc.getFields(WorkIndexField.ALIAS.getName()).length);
            ir.close();
        }
    }

    /**
     * Checks fields with different sort name to name is indexed correctly
     *
     * @throws Exception exception
     */
    public void testIndexWorkWithTag() throws Exception {

        addWorkOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(WorkIndexField.WORK.getName()).length);
            assertEquals(1, doc.getFields(WorkIndexField.TAG.getName()).length);
            assertEquals("Classical", doc.getField(WorkIndexField.TAG.getName()).stringValue());
            assertEquals(1, doc.getFields(WorkIndexField.TAGCOUNT.getName()).length);
            assertEquals("10", doc.getField(LabelIndexField.TAGCOUNT.getName()).stringValue());
        }
        ir.close();
    }

}