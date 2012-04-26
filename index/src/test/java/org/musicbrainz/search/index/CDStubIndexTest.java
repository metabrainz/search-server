package org.musicbrainz.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import java.sql.Statement;

import static org.junit.Assert.assertEquals;

public class CDStubIndexTest extends AbstractIndexTest{


     private void createIndex(RAMDirectory ramDir) throws Exception {

        IndexWriter writer = createIndexWriter(ramDir,CDStubIndexField.class);
        CDStubIndex ci = new CDStubIndex(conn);
        CommonTables ct = new CommonTables(conn, ci.getName());
        ct.createTemporaryTables(false);
        ci.init(writer, false);
        ci.addMetaInformation(writer);
        ci.indexData(writer, 0, Integer.MAX_VALUE);
        ci.destroy();
        writer.close();

    }

    private void addCDStubOne() throws Exception {

         Statement stmt = conn.createStatement();

         stmt.addBatch("INSERT INTO cdtoc_raw (id, release, discid, track_count, leadout_offset) VALUES (1, 1, 'discid', 11, 1)");
         stmt.addBatch("INSERT INTO release_raw (id, title, artist, added, last_modified, barcode, comment) " +
                "VALUES (1, 'title', 'artist', now(), now(), '123456789','a comment')");
         stmt.addBatch("INSERT INTO track_raw (id, release, title, sequence)" +
                "VALUES (1, 1, 'tracktitle', 3)");

         stmt.executeBatch();
         stmt.close();
     }

    private void addCDStubTwo() throws Exception {

         Statement stmt = conn.createStatement();

         stmt.addBatch("INSERT INTO cdtoc_raw (id, release, discid, track_count, leadout_offset) VALUES (2, 2, 'w237dKURKperVfmckD5b_xo8BO8-', 7, 2)");
         stmt.addBatch("INSERT INTO release_raw (id, title, artist, added, last_modified) " +
                "VALUES (2, 'title', null, now(), now())");
         stmt.addBatch("INSERT INTO track_raw (id, release, title, sequence)" +
                "VALUES (2, 2, 'tracktitle', 3)");

         stmt.executeBatch();
         stmt.close();
     }

     @Test
     public void testIndexCDStub() throws Exception {

        addCDStubOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(CDStubIndexField.ARTIST.getName()).length);
            assertEquals("artist",doc.getFieldable(CDStubIndexField.ARTIST.getName()).stringValue());
            assertEquals(1, doc.getFieldables(CDStubIndexField.TITLE.getName()).length);
            assertEquals("title",doc.getFieldable(CDStubIndexField.TITLE.getName()).stringValue());
            assertEquals(1, doc.getFieldables(CDStubIndexField.BARCODE.getName()).length);
            assertEquals("123456789",doc.getFieldable(CDStubIndexField.BARCODE.getName()).stringValue());
            assertEquals(1, doc.getFieldables(CDStubIndexField.COMMENT.getName()).length);
            assertEquals("a comment",doc.getFieldable(CDStubIndexField.COMMENT.getName()).stringValue());
            assertEquals(1, doc.getFieldables(CDStubIndexField.NUM_TRACKS.getName()).length);
            assertEquals(1, doc.getFieldables(CDStubIndexField.DISCID.getName()).length);
            assertEquals("1",doc.getFieldable(CDStubIndexField.NUM_TRACKS.getName()).stringValue());
            assertEquals("discid",doc.getFieldable(CDStubIndexField.DISCID.getName()).stringValue());

        }
        ir.close();

    }

    @Test
    public void testArtist() throws Exception {

        addCDStubOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(CDStubIndexField.ARTIST.getName()).length);
            assertEquals("artist",doc.getFieldable(CDStubIndexField.ARTIST.getName()).stringValue());
        }
        ir.close();

    }

    @Test
    public void testNoArtist() throws Exception {

        addCDStubTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(0, doc.getFieldables(CDStubIndexField.ARTIST.getName()).length);
        }
        ir.close();

    }

    @Test
    public void testBarcode() throws Exception {

        addCDStubOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(CDStubIndexField.BARCODE.getName()).length);
            assertEquals("123456789",doc.getFieldable(CDStubIndexField.BARCODE.getName()).stringValue());
        }
        ir.close();

    }

    @Test
    public void testNoBarcode() throws Exception {

        addCDStubTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(0, doc.getFieldables(CDStubIndexField.BARCODE.getName()).length);
        }
        ir.close();

    }

    @Test
    public void testComment() throws Exception {

        addCDStubOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(CDStubIndexField.COMMENT.getName()).length);
            assertEquals("a comment",doc.getFieldable(CDStubIndexField.COMMENT.getName()).stringValue());
        }
        ir.close();

    }

    @Test
    public void testNoComment() throws Exception {

        addCDStubTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(CDStubIndexField.COMMENT.getName()).length);
            assertEquals("-",doc.getFieldable(CDStubIndexField.COMMENT.getName()).stringValue());
        }
        ir.close();

    }

}
