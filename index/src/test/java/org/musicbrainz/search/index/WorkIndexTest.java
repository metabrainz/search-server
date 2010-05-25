package org.musicbrainz.search.index;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.document.Document;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.sql.Connection;
import java.sql.Statement;


public class WorkIndexTest extends AbstractIndexTest {

    public void setUp() throws Exception {
        super.setup();
    }

    private void createIndex(RAMDirectory ramDir) throws Exception {
        PerFieldAnalyzerWrapper analyzer = new PerFieldEntityAnalyzer(WorkIndexField.class);
        IndexWriter writer = new IndexWriter(ramDir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        WorkIndex ci = new WorkIndex(createConnection());
        ci.init(writer);
        ci.indexData(writer, 0, Integer.MAX_VALUE);
        ci.destroy();
        writer.close();

    }

    private void addWorkOne() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Echo & The Bunnymen')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (2, 'Echo and The Bunnymen')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (3, 'Echo & The Bunnyman')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (4, 'Echo And The Bunnymen')");

        stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES (1, 16153, 2)");
        stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES (2, 16153, 3)");
        stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES (3, 16153, 4)");

        stmt.addBatch("INSERT INTO artist (id, name, gid, sortname, comment, begindate_year, begindate_month, enddate_year, type)" +
                " VALUES (16153, 1, 'ccd4879c-5e88-4385-b131-bf65296bf245', 2, 'a comment', 1978, null, 1995, 2)");
        stmt.addBatch("INSERT INTO artist_credit (id, name, artistcount, refcount) VALUES (1, 1, 1, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name, joinphrase) " +
                " VALUES (1, 0, 16153, 1, null)");

        stmt.addBatch("INSERT INTO work_name (id, name) VALUES (1, 'Work')");
        stmt.addBatch("INSERT INTO work_name (id, name) VALUES (2, 'Play')");
        stmt.addBatch("INSERT INTO work (id, gid, name, artist_credit, type, iswc, comment)" +
                " VALUES (1, 'a539bb1e-f2e1-4b45-9db8-8053841e7503', 1, 1, null, 'T-101779304-1', null)");
        stmt.addBatch("INSERT INTO work_alias (work, name) VALUES (1, 2)");


        stmt.executeBatch();
        stmt.close();
        conn.close();
    }

    private void addWorkTwo() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Echo & The Bunnymen')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (2, 'Echo and The Bunnymen')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (3, 'Echo & The Bunnyman')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (4, 'Echo And The Bunnymen')");

        stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES (1, 16153, 2)");
        stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES (2, 16153, 3)");
        stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES (3, 16153, 4)");

        stmt.addBatch("INSERT INTO artist(id,name, gid, sortname,comment, begindate_year,begindate_month,enddate_year,type,editpending)" +
                " VALUES (16153,1, 'ccd4879c-5e88-4385-b131-bf65296bf245',2,'a comment', 1978,null, 1995, 2, 0)");

        stmt.addBatch("INSERT INTO artist_credit (id, name, artistcount, refcount) VALUES (1, 1, 1, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist,name, joinphrase) " +
                " VALUES (1, 0, 16153, 1, null)");

        stmt.addBatch("INSERT INTO work_name (id, name) VALUES (1, 'Work')");
        stmt.addBatch("INSERT INTO work_type (id, name) VALUES (1, 'Opera')");
        
        stmt.addBatch("INSERT INTO work (id, gid, name, artist_credit, type, iswc, comment)" +
                " VALUES (1, 'a539bb1e-f2e1-4b45-9db8-8053841e7503', 1, 1, 1, 'T-101779304-1', null)");

        stmt.executeBatch();
        stmt.close();
        conn.close();
    }

    public void testIndexWork() throws Exception {

        addWorkOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);
        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
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
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(0, doc.getFields(WorkIndexField.TYPE.getName()).length);
            ir.close();
        }
    }

    public void testIndexWorkWithType() throws Exception {

        addWorkTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);
        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
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
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
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
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(0, doc.getFields(WorkIndexField.ALIAS.getName()).length);
            ir.close();
        }
    }

}