package org.musicbrainz.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.musicbrainz.search.analysis.StandardUnaccentAnalyzer;
import org.musicbrainz.search.index.ReleaseGroupIndex;
import org.musicbrainz.search.index.ReleaseGroupIndexField;

import java.sql.Connection;
import java.sql.Statement;


public class ReleaseGroupIndexTest extends AbstractIndexTest {


    public void setUp() throws Exception {
        super.setup();

    }

    private void createIndex(RAMDirectory ramDir) throws Exception {
        PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new StandardUnaccentAnalyzer());
        IndexWriter writer = new IndexWriter(ramDir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        ReleaseGroupIndex li = new ReleaseGroupIndex(createConnection());
        li.indexData(writer, 0, Integer.MAX_VALUE);
        writer.close();

    }


    /**
     * @throws Exception
     */
    private void addReleaseGroupOne() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();


        stmt.addBatch("INSERT INTO artist(id,name, gid, modpending, sortname, page, resolution, begindate,enddate,type,quality,modpending_qual)" +
                "    VALUES (16153, 'Echo & The Bunnymen', 'ccd4879c-5e88-4385-b131-bf65296bf245',0, 'Echo & The Bunnymen', 205832224,'a comment', '1978-00-00','1995-00-00', 2, -1, 0)");

        stmt.addBatch("INSERT INTO album(" +
                "            id, artist, name, gid, modpending, page, language," +
                "            script, modpending_lang, quality, modpending_qual, release_group)" +
                "    VALUES (491240, 16153, 'Crocodiles (bonus disc)', 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 0, 154669573, 120, " +
                "            28, null, -1, 0, 491240)");

        stmt.addBatch("INSERT INTO release_group(" +
                "            id, gid, name, page, artist, type, modpending)" +
                "    VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 'Crocodiles', 154669573, 16153, 2, 0)");


        stmt.executeBatch();
        stmt.close();
        conn.close();
    }


    /**
     * No Type
     *
     * @throws Exception
     */
    private void addReleaseGroupTwo() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist(id,name, gid, modpending, sortname, page, resolution, begindate,enddate,type,quality,modpending_qual)" +
                "    VALUES (16153, 'Echo & The Bunnymen', 'ccd4879c-5e88-4385-b131-bf65296bf245',0, 'Echo & The Bunnymen', 205832224,'a comment', '1978-00-00','1995-00-00', 2, -1, 0)");


        stmt.addBatch("INSERT INTO album(" +
                "            id, artist, name, gid, modpending,  page, language," +
                "            script, modpending_lang, quality, modpending_qual, release_group)" +
                "    VALUES (491240, 16153, 'Crocodiles (bonus disc)', 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 0, 154669573, 120, " +
                "            28, null, -1, 0, 491240)");

        stmt.addBatch("INSERT INTO release_group(" +
                "            id, gid, name, page, artist, type, modpending)" +
                "    VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 'Crocodiles', 154669573, 16153, 0, 0)");

        stmt.executeBatch();
        stmt.close();
        conn.close();
    }

    /**
     * Two Albums
     *
     * @throws Exception
     */
    private void addReleaseGroupThree() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist(id,name, gid, modpending, sortname, page, resolution, begindate,enddate,type,quality,modpending_qual)" +
                "    VALUES (16153, 'Echo & The Bunnymen', 'ccd4879c-5e88-4385-b131-bf65296bf245',0, 'Echo & The Bunnymen', 205832224,'a comment', '1978-00-00','1995-00-00', 2, -1, 0)");


        stmt.addBatch("INSERT INTO album(" +
                "            id, artist, name, gid, modpending,page, language," +
                "            script, modpending_lang, quality, modpending_qual, release_group)" +
                "    VALUES (491240, 16153, 'Crocodiles (bonus disc)', 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 0, 154669573, 120, " +
                "            28, null, -1, 0, 491240)");
        stmt.addBatch("INSERT INTO album(" +
                "            id, artist, name, gid, modpending,page, language," +
                "            script, modpending_lang, quality, modpending_qual, release_group)" +
                "    VALUES (491241, 16153, 'Crocodiles (Special disc)', 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 0, 154669573, 120, " +
                "            28, null, -1, 0, 491240)");


        stmt.addBatch("INSERT INTO release_group(" +
                "            id, gid, name, page, artist, type, modpending)" +
                "    VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 'Crocodiles', 154669573, 16153, 2, 0)");

        stmt.executeBatch();
        stmt.close();
        conn.close();
    }


    /**
     * Basic test of all fields
     *
     * @throws Exception
     */
    public void testIndexReleaseGroupFields() throws Exception {

        addReleaseGroupOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.RELEASEGROUP.getName()).length);
            assertEquals("Crocodiles", doc.getField(ReleaseGroupIndexField.RELEASEGROUP.getName()).stringValue());
            assertEquals("efd2ace2-b3b9-305f-8a53-9803595c0e37", doc.getField(ReleaseGroupIndexField.RELEASEGROUP_ID.getName()).stringValue());
            assertEquals("Echo & The Bunnymen", doc.getField(ReleaseGroupIndexField.ARTIST.getName()).stringValue());
            assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", doc.getField(ReleaseGroupIndexField.ARTIST_ID.getName()).stringValue());
            assertEquals("single", doc.getField(ReleaseGroupIndexField.TYPE.getName()).stringValue());
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.RELEASES.getName()).length);
            assertEquals("Crocodiles (bonus disc)", doc.getField(ReleaseGroupIndexField.RELEASES.getName()).stringValue());

        }
        ir.close();

    }


    public void testIndexReleaseGroupWithType() throws Exception {

        addReleaseGroupOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.TYPE.getName()).length);
            assertEquals("single", doc.getField(ReleaseGroupIndexField.TYPE.getName()).stringValue());
        }
        ir.close();


    }

    /**
     * Checks record with type = null is set to unknown
     *
     * @throws Exception
     */
    public void testIndexReleaseGroupWithNoType() throws Exception {

        addReleaseGroupTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.TYPE.getName()).length);
            assertEquals("nat", doc.getField(ReleaseGroupIndexField.TYPE.getName()).stringValue());
        }
        ir.close();
    }


    /**
     * Checks record with multiple releases
     *
     * @throws Exception
     */
    public void testIndexReleaseGroupWithMultipleReleases() throws Exception {

        addReleaseGroupThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(2, doc.getFields(ReleaseGroupIndexField.RELEASES.getName()).length);
            String val1 = doc.getFields(ReleaseGroupIndexField.RELEASES.getName())[0].stringValue();
            String val2 = doc.getFields(ReleaseGroupIndexField.RELEASES.getName())[1].stringValue();
            assertTrue("Crocodiles (bonus disc)".equals(val1) || "Crocodiles (bonus disc)".equals(val2));
            assertTrue("Crocodiles (Special disc)".equals(val1) || "Crocodiles (Special disc)".equals(val2));
        }
        ir.close();
    }

    public void testGetTypeByDbId () throws Exception {        
        assertEquals(ReleaseGroupType.ALBUM,ReleaseGroupType.getByDbId(1));
    }

}