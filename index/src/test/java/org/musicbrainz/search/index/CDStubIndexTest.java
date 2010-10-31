package org.musicbrainz.search.index;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.sql.Connection;
import java.sql.Statement;


public class CDStubIndexTest extends AbstractIndexTest{

    public void setUp() throws Exception {
        super.setup();
    }

     private void createIndex(RAMDirectory ramDir) throws Exception {
        PerFieldAnalyzerWrapper analyzer = new PerFieldEntityAnalyzer(CDStubIndexField.class);
        IndexWriter writer = new IndexWriter(ramDir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        CDStubIndex ci = new CDStubIndex(createConnection());
        ci.init(writer);
        ci.writeMetaInformation(writer);
        ci.indexData(writer, 0, Integer.MAX_VALUE);
        ci.destroy();
        writer.close();

    }

    private void addCDStubOne() throws Exception {
         Connection conn = createConnection();
         conn.setAutoCommit(true);

         Statement stmt = conn.createStatement();

         stmt.addBatch("INSERT INTO cdtoc_raw(id, release, discid, trackcount, leadoutoffset)VALUES (1, 1, 'discid', 11, 1)");
         stmt.addBatch("INSERT INTO release_raw(id, title, artist, added, lastmodified, lookupcount, modifycount,source, barcode, comment)" +
                "VALUES (1,'title', 'artist', now(), now(), null, null,null, null, null)");
         stmt.addBatch("INSERT INTO track_raw(id, release, title, artist, sequence)" +
                "VALUES (1, 1, 'tracktitle', null, 3)");

         stmt.executeBatch();
         stmt.close();
     }

     public void testIndexCDStub() throws Exception {

        addCDStubOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(CDStubIndexField.ARTIST.getName()).length);
            assertEquals("artist",doc.getField(CDStubIndexField.ARTIST.getName()).stringValue());
            assertEquals(1, doc.getFields(CDStubIndexField.TITLE.getName()).length);
            assertEquals("title",doc.getField(CDStubIndexField.TITLE.getName()).stringValue());
            assertEquals(0, doc.getFields(CDStubIndexField.BARCODE.getName()).length);
            assertEquals(0, doc.getFields(CDStubIndexField.COMMENT.getName()).length);
            assertEquals(1, doc.getFields(CDStubIndexField.NUM_TRACKS.getName()).length);
            assertEquals(1, doc.getFields(CDStubIndexField.DISCID.getName()).length);
            assertEquals("1",doc.getField(CDStubIndexField.NUM_TRACKS.getName()).stringValue());
            assertEquals("discid",doc.getField(CDStubIndexField.DISCID.getName()).stringValue());

        }
        ir.close();

    }

}
