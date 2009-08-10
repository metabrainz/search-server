package org.musicbrainz.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.search.analysis.StandardUnaccentAnalyzer;

import java.sql.Connection;
import java.sql.Statement;


public class ReleaseIndexTest extends AbstractIndexTest {


    public void setUp() throws Exception {
        super.setup();

    }

    private void createIndex(RAMDirectory ramDir) throws Exception {
        IndexWriter writer = new IndexWriter(ramDir, new StandardUnaccentAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);
        ReleaseIndex li = new ReleaseIndex(createConnection());
        li.indexData(writer, 0, Integer.MAX_VALUE);
        writer.close();

    }


    /**
     * Minimum plus type and status
     *
     * @throws Exception
     */
    private void addReleaseOne() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();


        stmt.addBatch("INSERT INTO artist(id,name, gid, modpending, sortname, page, resolution, begindate,enddate,type,quality,modpending_qual)" +
                "    VALUES (16153, 'Echo & The Bunnymen', 'ccd4879c-5e88-4385-b131-bf65296bf245',0, 'Echo & The Bunnymen', 205832224,'a comment', '1978-00-00','1995-00-00', 2, -1, 0)");

        stmt.addBatch("INSERT INTO album(" +
                "            id, artist, name, gid, modpending,attributes, page, language," +
                "            script, modpending_lang, quality, modpending_qual, release_group)" +
                "    VALUES (491240, 16153, 'Crocodiles (bonus disc)', 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 0,(1,2,100), 154669573, 120, " +
                "            28, null, -1, 0, 491240)");

        stmt.addBatch("INSERT INTO albummeta(" +
                "            id, tracks, discids, puids, firstreleasedate, asin, coverarturl, " +
                "            lastupdate, rating, rating_count, dateadded)" +
                "    VALUES (491240, 2, 0, 0, '1980-07-00', null, null, " +
                "            null, 5, 1, null)");


        stmt.executeBatch();
        stmt.close();
        conn.close();
    }


    /**
     * No Release Type
     *
     * @throws Exception
     */
    private void addReleaseTwo() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();


        stmt.addBatch("INSERT INTO artist(id,name, gid, modpending, sortname, page, resolution, begindate,enddate,type,quality,modpending_qual)" +
                "    VALUES (16153, 'Echo & The Bunnymen', 'ccd4879c-5e88-4385-b131-bf65296bf245',0, 'Echo & The Bunnymen', 205832224,'a comment', '1978-00-00','1995-00-00', 2, -1, 0)");

        stmt.addBatch("INSERT INTO album(" +
                "            id, artist, name, gid, modpending,attributes, page, language," +
                "            script, modpending_lang, quality, modpending_qual, release_group)" +
                "    VALUES (491240, 16153, 'Crocodiles (bonus disc)', 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 0,(0,100), 154669573, 120, " +
                "            28, null, -1, 0, 491240)");

        stmt.addBatch("INSERT INTO albummeta(" +
                "            id, tracks, discids, puids, firstreleasedate, asin, coverarturl, " +
                "            lastupdate, rating, rating_count, dateadded)" +
                "    VALUES (491240, 2, 0, 0, '1980-07-00', null, null, " +
                "            null, 5, 1, null)");


        stmt.executeBatch();
        stmt.close();
        conn.close();
    }


    /**
     * No Release Status
     *
     * @throws Exception
     */
    private void addReleaseThree() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();


        stmt.addBatch("INSERT INTO artist(id,name, gid, modpending, sortname, page, resolution, begindate,enddate,type,quality,modpending_qual)" +
                "    VALUES (16153, 'Echo & The Bunnymen', 'ccd4879c-5e88-4385-b131-bf65296bf245',0, 'Echo & The Bunnymen', 205832224,'a comment', '1978-00-00','1995-00-00', 2, -1, 0)");

        stmt.addBatch("INSERT INTO album(" +
                "            id, artist, name, gid, modpending,attributes, page, language," +
                "            script, modpending_lang, quality, modpending_qual, release_group)" +
                "    VALUES (491240, 16153, 'Crocodiles (bonus disc)', 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 0,(0,1), 154669573, 120, " +
                "            28, null, -1, 0, 491240)");

        stmt.addBatch("INSERT INTO albummeta(" +
                "            id, tracks, discids, puids, firstreleasedate, asin, coverarturl, " +
                "            lastupdate, rating, rating_count, dateadded)" +
                "    VALUES (491240, 2, 0, 0, '1980-07-00', null, null, " +
                "            null, 5, 1, null)");


        stmt.executeBatch();
        stmt.close();
        conn.close();
    }


    /**
     * All Basic Fields
     *
     * @throws Exception
     */
    private void addReleaseFour() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();


        stmt.addBatch("INSERT INTO artist(id,name, gid, modpending, sortname, page, resolution, begindate,enddate,type,quality,modpending_qual)" +
                "    VALUES (16153, 'Echo & The Bunnymen', 'ccd4879c-5e88-4385-b131-bf65296bf245',0, 'Echo & The Bunnymen', 205832224,'a comment', '1978-00-00','1995-00-00', 2, -1, 0)");

        stmt.addBatch("INSERT INTO album(" +
                "            id, artist, name, gid, modpending,attributes, page, language," +
                "            script, modpending_lang, quality, modpending_qual, release_group)" +
                "    VALUES (491240, 16153, 'Crocodiles (bonus disc)', 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 0,(1,2,100), 154669573, 120, " +
                "            28, null, -1, 0, 491240)");

        stmt.addBatch("INSERT INTO albummeta(" +
                "            id, tracks, discids, puids, firstreleasedate, asin, coverarturl, " +
                "            lastupdate, rating, rating_count, dateadded)" +
                "    VALUES (491240, 2, 0, 0, '1980-07-00', 123456789, null, " +
                "            null, 5, 1, null)");

        stmt.addBatch("INSERT INTO language(" +
                "            id, isocode_3t, isocode_3b, isocode_2, name, frequency)" +
                "    VALUES (120, 'eng', 'eng', 'en', 'English', 2);");

        stmt.addBatch("INSERT INTO script(" +
                "            id, isocode, isonumber, name, frequency)" +
                "    VALUES (28, 'Latn', 215, 'Latn', 4);");

        stmt.executeBatch();
        stmt.close();
        conn.close();
    }


    /**
     * All Basic Fields Plus Release Events
     *
     * @throws Exception
     */
    private void addReleaseFive() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();


        stmt.addBatch("INSERT INTO artist(id,name, gid, modpending, sortname, page, resolution, begindate,enddate,type,quality,modpending_qual)" +
                "    VALUES (16153, 'Echo & The Bunnymen', 'ccd4879c-5e88-4385-b131-bf65296bf245',0, 'Echo & The Bunnymen', 205832224,'a comment', '1978-00-00','1995-00-00', 2, -1, 0)");

        stmt.addBatch("INSERT INTO album(" +
                "            id, artist, name, gid, modpending,attributes, page, language," +
                "            script, modpending_lang, quality, modpending_qual, release_group)" +
                "    VALUES (491240, 16153, 'Crocodiles (bonus disc)', 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 0,(1,2,100), 154669573, 120, " +
                "            28, null, -1, 0, 491240)");

        stmt.addBatch("INSERT INTO albummeta(" +
                "            id, tracks, discids, puids, firstreleasedate, asin, coverarturl, " +
                "            lastupdate, rating, rating_count, dateadded)" +
                "    VALUES (491240, 2, 0, 0, '1980-07-00', null, null, " +
                "            null, 5, 1, null)");

        stmt.addBatch("INSERT INTO language(" +
                "            id, isocode_3t, isocode_3b, isocode_2, name, frequency)" +
                "    VALUES (120, 'eng', 'eng', 'en', 'English', 2);");

        stmt.addBatch("INSERT INTO script(" +
                "            id, isocode, isonumber, name, frequency)" +
                "    VALUES (28, 'Latn', 215, 'Latn', 4);");

        stmt.addBatch("INSERT INTO country(id, isocode, name)" +
                " VALUES (221, 'AF','Afghanistan')");

        stmt.addBatch("INSERT INTO release(" +
                "            id, album, country, releasedate, modpending, label, catno, barcode,format)" +
                "    VALUES (1, 491240, 221, '1970-01-01', null, 563, 'ECHO1', '1212121212',7)");

        stmt.addBatch("INSERT INTO labelalias(id, ref, name, timesused, modpending, lastused)" +
                " VALUES (1327, 563, '4AD US', 0, 0, '1970-01-01')");
        stmt.addBatch("INSERT INTO label(id,name, gid, modpending, labelcode,sortname,country, page, resolution, begindate,enddate,type)" +
                "    VALUES (563, '4AD', 'a539bb1e-f2e1-4b45-9db8-8053841e7503',0,null,'4AD',1,2260992,null, null, null,null)");
        stmt.executeBatch();
        stmt.close();
        conn.close();
    }


    /**
     * All Basic Fields Plus Release Events
     *
     * @throws Exception
     */
    private void addReleaseSix() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();


        stmt.addBatch("INSERT INTO artist(id,name, gid, modpending, sortname, page, resolution, begindate,enddate,type,quality,modpending_qual)" +
                "    VALUES (16153, 'Echo & The Bunnymen', 'ccd4879c-5e88-4385-b131-bf65296bf245',0, 'Echo & The Bunnymen', 205832224,'a comment', '1978-00-00','1995-00-00', 2, -1, 0)");

        stmt.addBatch("INSERT INTO album(" +
                "            id, artist, name, gid, modpending,attributes, page, language," +
                "            script, modpending_lang, quality, modpending_qual, release_group)" +
                "    VALUES (491240, 16153, 'Crocodiles (bonus disc)', 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 0,(1,2,100), 154669573, 120, " +
                "            28, null, -1, 0, 491240)");

        stmt.addBatch("INSERT INTO albummeta(" +
                "            id, tracks, discids, puids, firstreleasedate, asin, coverarturl, " +
                "            lastupdate, rating, rating_count, dateadded)" +
                "    VALUES (491240, 2, 0, 0, '1980-07-00', null, null, " +
                "            null, 5, 1, null)");

        stmt.addBatch("INSERT INTO language(" +
                "            id, isocode_3t, isocode_3b, isocode_2, name, frequency)" +
                "    VALUES (120, 'eng', 'eng', 'en', 'English', 2);");

        stmt.addBatch("INSERT INTO script(" +
                "            id, isocode, isonumber, name, frequency)" +
                "    VALUES (28, 'Latn', 215, 'Latn', 4);");

        stmt.addBatch("INSERT INTO country(id, isocode, name)" +
                " VALUES (221, 'AF','Afghanistan')");

        stmt.addBatch("INSERT INTO release(" +
                "            id, album, country, releasedate, modpending, label, catno, barcode,format)" +
                "    VALUES (1, 491240, 221, '1970-01-01', null, null,null,null,null)");

        stmt.addBatch("INSERT INTO labelalias(id, ref, name, timesused, modpending, lastused)" +
                " VALUES (1327, 563, '4AD US', 0, 0, '1970-01-01')");
        stmt.addBatch("INSERT INTO label(id,name, gid, modpending, labelcode,sortname,country, page, resolution, begindate,enddate,type)" +
                "    VALUES (563, '4AD', 'a539bb1e-f2e1-4b45-9db8-8053841e7503',0,null,'4AD',1,2260992,null, null, null,null)");
        stmt.executeBatch();
        stmt.close();
        conn.close();
    }


    /**
     * Basic test of all fields
     *
     * @throws Exception
     */
    public void testIndexReleaseMinPlusTypeAndStatusFields() throws Exception {

        addReleaseOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals("Crocodiles (bonus disc)", doc.getField(ReleaseIndexField.RELEASE.getName()).stringValue());
            assertEquals("c3b8dbc9-c1ff-4743-9015-8d762819134e", doc.getField(ReleaseIndexField.RELEASE_ID.getName()).stringValue());
            assertEquals("Echo & The Bunnymen", doc.getField(ReleaseIndexField.ARTIST.getName()).stringValue());
            assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", doc.getField(ReleaseIndexField.ARTIST_ID.getName()).stringValue());
            assertEquals(1, doc.getFields(ReleaseIndexField.TYPE.getName()).length);
            assertEquals("single", doc.getField(ReleaseIndexField.TYPE.getName()).stringValue());
            assertEquals(1, doc.getFields(ReleaseIndexField.STATUS.getName()).length);
            assertEquals("official", doc.getField(ReleaseIndexField.STATUS.getName()).stringValue());
            assertEquals(0, doc.getFields(ReleaseIndexField.LANGUAGE.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.SCRIPT.getName()).length);


        }
        ir.close();

    }


    /**
     * @throws Exception
     */
    public void testIndexReleaseNoType() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.TYPE.getName()).length);
        }
        ir.close();

    }


    /**
     * @throws Exception
     */
    public void testIndexReleaseNoLanguage() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.LANGUAGE.getName()).length);
        }
        ir.close();

    }


    /**
     * @throws Exception
     */
    public void testIndexReleaseNoScript() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.SCRIPT.getName()).length);
        }
        ir.close();

    }

    /**
     * @throws Exception
     */
    public void testIndexReleaseNoStatus() throws Exception {

        addReleaseThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.STATUS.getName()).length);
        }
        ir.close();

    }


    /**
     * @throws Exception
     */
    public void testIndexReleaseLanguage() throws Exception {

        addReleaseFour();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFields(ReleaseIndexField.LANGUAGE.getName()).length);
            assertEquals("eng", doc.getField(ReleaseIndexField.LANGUAGE.getName()).stringValue());

        }
        ir.close();

    }

    /**
     * @throws Exception
     */
    public void testIndexReleaseASIN() throws Exception {

        addReleaseFour();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFields(ReleaseIndexField.AMAZON_ID.getName()).length);
            assertEquals("123456789", doc.getField(ReleaseIndexField.AMAZON_ID.getName()).stringValue());

        }
        ir.close();

    }


    /**
     * @throws Exception
     */
    public void testIndexReleaseScript() throws Exception {

        addReleaseFour();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFields(ReleaseIndexField.SCRIPT.getName()).length);
            assertEquals("Latn", doc.getField(ReleaseIndexField.SCRIPT.getName()).stringValue());

        }
        ir.close();
    }

    /**
     * @throws Exception
     */
    public void testIndexFullReleaseEvent() throws Exception {

        addReleaseFive();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFields(ReleaseIndexField.COUNTRY.getName()).length);
            assertEquals(1, doc.getFields(ReleaseIndexField.BARCODE.getName()).length);
            assertEquals(1, doc.getFields(ReleaseIndexField.DATE.getName()).length);
            assertEquals(1, doc.getFields(ReleaseIndexField.CATALOG_NO.getName()).length);
            assertEquals(1, doc.getFields(ReleaseIndexField.LABEL.getName()).length);

            assertEquals("af", doc.getField(ReleaseIndexField.COUNTRY.getName()).stringValue());
            assertEquals("1212121212", doc.getField(ReleaseIndexField.BARCODE.getName()).stringValue());
            assertEquals("1970-01-01", doc.getField(ReleaseIndexField.DATE.getName()).stringValue());
            assertEquals("ECHO1", doc.getField(ReleaseIndexField.CATALOG_NO.getName()).stringValue());
            assertEquals("4AD", doc.getField(ReleaseIndexField.LABEL.getName()).stringValue());


        }
        ir.close();

    }

    /**
     * @throws Exception
     */
    public void testIndexEmptyReleaseEvent() throws Exception {

        addReleaseSix();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFields(ReleaseIndexField.COUNTRY.getName()).length);
            assertEquals(1, doc.getFields(ReleaseIndexField.BARCODE.getName()).length);
            assertEquals(1, doc.getFields(ReleaseIndexField.DATE.getName()).length);
            assertEquals(1, doc.getFields(ReleaseIndexField.CATALOG_NO.getName()).length);
            assertEquals(1, doc.getFields(ReleaseIndexField.LABEL.getName()).length);

            assertEquals("af", doc.getField(ReleaseIndexField.COUNTRY.getName()).stringValue());
            assertEquals("-", doc.getField(ReleaseIndexField.BARCODE.getName()).stringValue());
            assertEquals("1970-01-01", doc.getField(ReleaseIndexField.DATE.getName()).stringValue());
            assertEquals("-", doc.getField(ReleaseIndexField.CATALOG_NO.getName()).stringValue());
            assertEquals("-", doc.getField(ReleaseIndexField.LABEL.getName()).stringValue());


        }
        ir.close();

    }
}