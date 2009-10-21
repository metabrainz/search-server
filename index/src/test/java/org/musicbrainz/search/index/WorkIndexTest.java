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
        ci.init();
        ci.indexData(writer, 0, Integer.MAX_VALUE);
        ci.destroy();
        writer.close();

    }

    private void addWorkOne() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO work(" +
                "    id, gid, name,artist_credit,type, iswc, comment, editpending)" +
                "    VALUES (1, 'a539bb1e-f2e1-4b45-9db8-8053841e7503', 1, 1, 1, null, null, 1);");

        stmt.addBatch("INSERT INTO work_name(id, name, refcount) " +
                      "VALUES (1, 'Work', 1);");
        stmt.addBatch("");

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
            assertEquals(0, doc.getFields(WorkIndexField.ARTIST.getName()).length);
    }
        ir.close();
    }

}