package org.musicbrainz.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.musicbrainz.search.analysis.StandardUnaccentAnalyzer;
import org.musicbrainz.search.index.ReleaseIndex;
import org.musicbrainz.search.index.ReleaseIndexField;

import java.sql.Connection;
import java.sql.Statement;


public class ReleaseIndexTest extends AbstractIndexTest {


    public void setUp() throws Exception {
        super.setup();

    }

    private void createIndex(RAMDirectory ramDir) throws Exception {
        PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new StandardUnaccentAnalyzer());
        IndexWriter writer = new IndexWriter(ramDir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
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



        stmt.addBatch("INSERT INTO artist_type(id,name)VALUES (1, 'Person');");
        stmt.addBatch("INSERT INTO artist_type(id,name)VALUES (2, 'Group');");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (1,'Echo & The Bunnymen',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (2,'Echo and The Bunnymen',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (3,'Echo & The Bunnyman',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (4,'Echo And The Bunnymen',1)");

        stmt.addBatch("INSERT INTO artist_alias(id, artist, name, editpending) VALUES(1,16153,2,0);");
        stmt.addBatch("INSERT INTO artist_alias(id, artist, name, editpending) VALUES(2,16153,3,0);");
        stmt.addBatch("INSERT INTO artist_alias(id, artist, name, editpending) VALUES(3,16153,4,0);");

        stmt.addBatch("INSERT INTO artist(id,name, gid, sortname,comment, begindate_year,begindate_month,enddate_year,type,editpending)" +
                   " VALUES (16153,1, 'ccd4879c-5e88-4385-b131-bf65296bf245',1,'a comment', 1978,null, 1995, 2, 0)");

        stmt.addBatch("INSERT INTO artist_credit( " +
                      " id, artistcount, refcount) " +
                      " VALUES (1, 1, 1)");

        stmt.addBatch("INSERT INTO artist_credit_name(" +
                "    artist_credit, position, artist,name, joinphrase)" +
                "    VALUES (1, 0, 16153, 1, null)");

        stmt.addBatch("INSERT INTO release_name(id,name, refcount)VALUES (1, 'Crocodiles', 0)");
        stmt.addBatch("INSERT INTO release_name(id,name, refcount)VALUES (2, 'Crocodiles (bonus disc)', 0)");
        stmt.addBatch("INSERT INTO release_group( id, gid,name,artist_credit,type,comment,editpending)" +
                  "    VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1, 2, null, 0)");

        stmt.addBatch("INSERT INTO release_group_type(id,name) VALUES (1,'Non Album Tracks')");
        stmt.addBatch("INSERT INTO release_group_type(id,name) VALUES (2,'Album')");
        stmt.addBatch("INSERT INTO release_group_type(id,name) VALUES (3,'Single')");

        stmt.addBatch("INSERT INTO release(id, gid, name, artist_credit, release_group, status, packaging,country, " +
                "language, script, date_year, date_month, date_day,barcode, comment, editpending) " +
            "  VALUES (491240,'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1,491240,1,1,1,1, 1, 1, 1, 1, null, null, 1)");

     /*   stmt.addBatch("INSERT INTO album(" +
                "            id, artist, name, gid, modpending,attributes, page, language," +
                "            script, modpending_lang, quality, modpending_qual, release_group)" +
                "    VALUES (491240, 16153, 'Crocodiles (bonus disc)', 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 0,(1,2,100), 154669573, 120, " +
                "            28, null, -1, 0, 491240)"); */
 /*
        stmt.addBatch("INSERT INTO release_meta(" +
                "            id, lastupdate, dateadded, coverarturl, infourl, amazonasin,amazonstore)" +
                "    VALUES (491240, null, null, null,null,null,null);");

   */
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



        stmt.addBatch("INSERT INTO artist_type(id,name)VALUES (1, 'Person');");
        stmt.addBatch("INSERT INTO artist_type(id,name)VALUES (2, 'Group');");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (1,'Echo & The Bunnymen',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (2,'Echo and The Bunnymen',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (3,'Echo & The Bunnyman',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (4,'Echo And The Bunnymen',1)");

        stmt.addBatch("INSERT INTO artist_alias(id, artist, name, editpending) VALUES(1,16153,2,0);");
        stmt.addBatch("INSERT INTO artist_alias(id, artist, name, editpending) VALUES(2,16153,3,0);");
        stmt.addBatch("INSERT INTO artist_alias(id, artist, name, editpending) VALUES(3,16153,4,0);");

        stmt.addBatch("INSERT INTO artist(id,name, gid, sortname,comment, begindate_year,begindate_month,enddate_year,type,editpending)" +
                   " VALUES (16153,1, 'ccd4879c-5e88-4385-b131-bf65296bf245',1,'a comment', 1978,null, 1995, 2, 0)");

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



        stmt.addBatch("INSERT INTO artist_type(id,name)VALUES (1, 'Person');");
        stmt.addBatch("INSERT INTO artist_type(id,name)VALUES (2, 'Group');");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (1,'Echo & The Bunnymen',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (2,'Echo and The Bunnymen',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (3,'Echo & The Bunnyman',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (4,'Echo And The Bunnymen',1)");

        stmt.addBatch("INSERT INTO artist_alias(id, artist, name, editpending) VALUES(1,16153,2,0);");
        stmt.addBatch("INSERT INTO artist_alias(id, artist, name, editpending) VALUES(2,16153,3,0);");
        stmt.addBatch("INSERT INTO artist_alias(id, artist, name, editpending) VALUES(3,16153,4,0);");

        stmt.addBatch("INSERT INTO artist(id,name, gid, sortname,comment, begindate_year,begindate_month,enddate_year,type,editpending)" +
                   " VALUES (16153,1, 'ccd4879c-5e88-4385-b131-bf65296bf245',1,'a comment', 1978,null, 1995, 2, 0)");

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


        stmt.addBatch("INSERT INTO artist_type(id,name)VALUES (1, 'Person');");
        stmt.addBatch("INSERT INTO artist_type(id,name)VALUES (2, 'Group');");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (1,'Echo & The Bunnymen',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (2,'Echo and The Bunnymen',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (3,'Echo & The Bunnyman',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (4,'Echo And The Bunnymen',1)");

        stmt.addBatch("INSERT INTO artist_alias(id, artist, name, editpending) VALUES(1,16153,2,0);");
        stmt.addBatch("INSERT INTO artist_alias(id, artist, name, editpending) VALUES(2,16153,3,0);");
        stmt.addBatch("INSERT INTO artist_alias(id, artist, name, editpending) VALUES(3,16153,4,0);");

        stmt.addBatch("INSERT INTO artist(id,name, gid, sortname,comment, begindate_year,begindate_month,enddate_year,type,editpending)" +
                   " VALUES (16153,1, 'ccd4879c-5e88-4385-b131-bf65296bf245',1,'a comment', 1978,null, 1995, 2, 0)");

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



        stmt.addBatch("INSERT INTO artist_type(id,name)VALUES (1, 'Person');");
        stmt.addBatch("INSERT INTO artist_type(id,name)VALUES (2, 'Group');");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (1,'Echo & The Bunnymen',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (2,'Echo and The Bunnymen',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (3,'Echo & The Bunnyman',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (4,'Echo And The Bunnymen',1)");

        stmt.addBatch("INSERT INTO artist_alias(id, artist, name, editpending) VALUES(1,16153,2,0);");
        stmt.addBatch("INSERT INTO artist_alias(id, artist, name, editpending) VALUES(2,16153,3,0);");
        stmt.addBatch("INSERT INTO artist_alias(id, artist, name, editpending) VALUES(3,16153,4,0);");

        stmt.addBatch("INSERT INTO artist(id,name, gid, sortname,comment, begindate_year,begindate_month,enddate_year,type,editpending)" +
                   " VALUES (16153,1, 'ccd4879c-5e88-4385-b131-bf65296bf245',1,'a comment', 1978,null, 1995, 2, 0)");

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

        stmt.addBatch("INSERT INTO label_alias(id, label, name, editpending)" +
                " VALUES (1327, 563, 1, 0)");
        stmt.addBatch("INSERT INTO label(id,name, gid, editpending, labelcode,sortname,country, begindate_year,enddate_year,type)" +
                "    VALUES (563, 1, 'a539bb1e-f2e1-4b45-9db8-8053841e7503',0,1,1,1,null, null,null)");
        stmt.addBatch("INSERT INTO label_name (id, name) VALUES (1, '4AD')");
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



        stmt.addBatch("INSERT INTO artist_type(id,name)VALUES (1, 'Person');");
        stmt.addBatch("INSERT INTO artist_type(id,name)VALUES (2, 'Group');");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (1,'Echo & The Bunnymen',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (2,'Echo and The Bunnymen',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (3,'Echo & The Bunnyman',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (4,'Echo And The Bunnymen',1)");

        stmt.addBatch("INSERT INTO artist_alias(id, artist, name, editpending) VALUES(1,16153,2,0);");
        stmt.addBatch("INSERT INTO artist_alias(id, artist, name, editpending) VALUES(2,16153,3,0);");
        stmt.addBatch("INSERT INTO artist_alias(id, artist, name, editpending) VALUES(3,16153,4,0);");

        stmt.addBatch("INSERT INTO artist(id,name, gid, sortname,comment, begindate_year,begindate_month,enddate_year,type,editpending)" +
                   " VALUES (16153,1, 'ccd4879c-5e88-4385-b131-bf65296bf245',1,'a comment', 1978,null, 1995, 2, 0)");
      
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

        stmt.addBatch("INSERT INTO label_alias(id, label, name, editpending)" +
                " VALUES (1327, 563, 1, 0)");
        stmt.addBatch("INSERT INTO label(id,name, gid, editpending, labelcode,sortname,country, begindate_year,enddate_year,type)" +
                "    VALUES (563, 1, 'a539bb1e-f2e1-4b45-9db8-8053841e7503',0,1,1,1,null, null,null)");
                
        stmt.executeBatch();
        stmt.close();
        conn.close();
    }


    /**
     * Basic test of all fields
     *
     * @throws Exception
     */
    /*
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
            assertEquals("Official", doc.getField(ReleaseIndexField.STATUS.getName()).stringValue());
            assertEquals(0, doc.getFields(ReleaseIndexField.LANGUAGE.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.SCRIPT.getName()).length);


        }
        ir.close();

    }
    */

    /**
     * @throws Exception
     */
    /*
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
    */

    /**
     * @throws Exception
     */
    /*
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
    */

    /**
     * @throws Exception
     */
    /*
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
      */

    /**
     * @throws Exception
     */
    /*
    public void testIndexReleaseNoFormat() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.FORMAT.getName()).length);
        }
        ir.close();

    }
    */
    /**
     * @throws Exception
     */
    /*
    public void testIndexReleaseNoBarcode() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.BARCODE.getName()).length);
        }
        ir.close();

    }
    */
    /**
     * @throws Exception
     */
    /*
    public void testIndexReleaseNoLabel() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.LABEL.getName()).length);
        }
        ir.close();

    }
    */

    /**
     * @throws Exception
     */
    /*
    public void testIndexReleaseNoCatalogNo() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.CATALOG_NO.getName()).length);
        }
        ir.close();

    }
    */

    /**
     * @throws Exception
     */
    /*
    public void testIndexReleaseNoCountry() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.COUNTRY.getName()).length);
        }
        ir.close();

    }
    */

    /**
     * @throws Exception
     */
    /*
    public void testIndexReleaseNoDate() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.DATE.getName()).length);
        }
        ir.close();

    }
    */

    /**
     * @throws Exception
     */
    /*
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
    */


    /**
     * @throws Exception
     */
    /*
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
    */

    /**
     * @throws Exception
     */
    /*
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
    */

    /**
     * @throws Exception
     */
    /*
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
    */

    /**
     * @throws Exception
     */
    /*
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
            assertEquals("Vinyl",doc.getField(ReleaseIndexField.FORMAT.getName()).stringValue());

        }
        ir.close();

    }
    */

    /**
     * @throws Exception
     */
    /*
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
            assertEquals("-",doc.getField(ReleaseIndexField.FORMAT.getName()).stringValue());


        }
        ir.close();

    }
    */


    /**
     * @throws Exception
     */
    /*
    public void testIndexNoReleaseEvent() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.COUNTRY.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.BARCODE.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.DATE.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.CATALOG_NO.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.LABEL.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.FORMAT.getName()).length);
        }
        ir.close();

    }
    */

    public void testGetTypeByDbId () throws Exception {
        assertNull(ReleaseType.getByDbId(0));
        assertEquals(ReleaseType.ALBUM,ReleaseType.getByDbId(1));
    }

    public void testGetFormatByDbId () throws Exception {
        assertNull(ReleaseFormat.getByDbId(0));
        assertEquals(ReleaseFormat.CD,ReleaseFormat.getByDbId(1));
    }

    public void testGetStatusByDbId () throws Exception {
        assertNull(ReleaseStatus.getByDbId(0));
        assertEquals(ReleaseStatus.OFFICIAL,ReleaseStatus.getByDbId(100));
    }

}