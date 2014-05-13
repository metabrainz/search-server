package org.musicbrainz.search.index;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import java.sql.Statement;

import static org.junit.Assert.assertEquals;

public class EditorIndexTest extends AbstractIndexTest {


    private void createIndex(RAMDirectory ramDir) throws Exception {
        IndexWriter writer = createIndexWriter(ramDir,EditorIndexField.class);
        EditorIndex ti = new EditorIndex(conn);
        CommonTables ct = new CommonTables(conn, ti.getName());
        ct.createTemporaryTables(false);

        ti.init(writer, false);
        ti.addMetaInformation(writer);
        ti.indexData(writer, 0, Integer.MAX_VALUE);
        ti.destroy();
        writer.close();

    }

    private void addEditorOne() throws Exception {

        Statement stmt = conn.createStatement();
        stmt.addBatch("INSERT INTO editor (id, name, bio, email_confirm_date) VALUES (1,'frankf','frank the f', '2012-09-17 18:47:52.69')");
 
        stmt.executeBatch();
        stmt.close();
    }

    private void addEditorTwo() throws Exception {

        Statement stmt = conn.createStatement();
        stmt.addBatch("INSERT INTO editor (id, name, bio) VALUES (1,'jane','janet')");

        stmt.executeBatch();
        stmt.close();
    }

    @Test
    public void testIndexEditorName() throws Exception {

        addEditorOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, EditorIndexField.EDITOR, "frankf");

        }
        ir.close();
    }

    @Test
    public void testIndexEditorBio() throws Exception {

        addEditorOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, EditorIndexField.BIO, "f");
        }
        ir.close();
    }

    @Test
    public void testIndexEditorDontIgnoreBioIfNotConfirmed() throws Exception {

        addEditorTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, EditorIndexField.BIO, "janet");
        }
        ir.close();
    }
}