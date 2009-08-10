package org.musicbrainz.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.search.analysis.StandardUnaccentAnalyzer;

import java.sql.Connection;
import java.sql.Statement;


public class AnnotationIndexTest extends AbstractIndexTest {


    public void setUp() throws Exception {
        super.setup();

    }

    private void createIndex(RAMDirectory ramDir) throws Exception {
        IndexWriter writer = new IndexWriter(ramDir, new StandardUnaccentAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);
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


        stmt.addBatch("INSERT INTO annotation(" +
                "            id, moderator, type, rowid, text, changelog, created, moderation,modpending)" +
                "    VALUES (1, 51298, 2, 66, 'Formed in Oxford, UK.', 'test',null, 176097, 0)");

        stmt.addBatch("INSERT INTO album(" +
                "            id, artist, name, gid, modpending, page, language," +
                "            script, modpending_lang, quality, modpending_qual, release_group)" +
                "    VALUES (66, 16153, 'Crocodiles (bonus disc)', 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 0, 154669573, 120, " +
                "            28, null, -1, 0, 491240)");


        stmt.executeBatch();
        stmt.close();
        conn.close();
    }


    /**
     * Basic test of all fields
     *
     * @throws Exception
     */
    public void testIndexAnnotationFields() throws Exception {
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
            assertEquals("Formed in Oxford, UK.", doc.getField(AnnotationIndexField.TEXT.getName()).stringValue());
            assertEquals(1, doc.getFields(AnnotationIndexField.TYPE.getName()).length);
            assertEquals("release", doc.getField(AnnotationIndexField.TYPE.getName()).stringValue());

        }
        ir.close();

    }
}


