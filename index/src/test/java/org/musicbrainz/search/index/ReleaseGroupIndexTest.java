package org.musicbrainz.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.musicbrainz.search.index.ReleaseGroupIndex;
import org.musicbrainz.search.index.ReleaseGroupIndexField;

import java.sql.Connection;
import java.sql.Statement;


public class ReleaseGroupIndexTest extends AbstractIndexTest {


    public void setUp() throws Exception {
        super.setup();

    }

    private void createIndex(RAMDirectory ramDir) throws Exception {
        PerFieldAnalyzerWrapper analyzer = new ReleaseGroupAnalyzer();
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
                " VALUES (16153,1, 'ccd4879c-5e88-4385-b131-bf65296bf245',2,'a comment', 1978,null, 1995, 2, 0)");

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
                "    VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1, null, null, 0);");

        stmt.addBatch("INSERT INTO release_group_type(id,name) VALUES (1,'Non Album Tracks')");
        stmt.addBatch("INSERT INTO release_group_type(id,name) VALUES (2,'Album')");
        stmt.addBatch("INSERT INTO release_group_type(id,name) VALUES (3,'Single')");

        stmt.addBatch("INSERT INTO release(id, gid, name, artist_credit, release_group, status, packaging,country, " +
                "language, script, date_year, date_month, date_day,barcode, comment, editpending) " +
                "  VALUES (491240,'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1,491240,1,1,1,1, 1, 1, 1, 1, null, null, 1)");

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


        stmt.addBatch("INSERT INTO release_name(id,name, refcount)VALUES (1, 'Crocodiles', 0)");
        stmt.addBatch("INSERT INTO release_name(id,name, refcount)VALUES (2, 'Crocodiles (Bonus disc)', 0)");
        stmt.addBatch("INSERT INTO release_name(id,name, refcount)VALUES (3, 'Crocodiles (Special disc)', 0)");


        stmt.addBatch("INSERT INTO release_group( id, gid,name,artist_credit,type,comment,editpending)" +
                "    VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1, 2, null, 0);");

        stmt.addBatch("INSERT INTO release_group_type(id,name) VALUES (1,'Non Album Tracks')");
        stmt.addBatch("INSERT INTO release_group_type(id,name) VALUES (2,'Album')");
        stmt.addBatch("INSERT INTO release_group_type(id,name) VALUES (3,'Single')");

        stmt.addBatch("INSERT INTO release(id, gid, name, artist_credit, release_group, status, packaging,country, " +
                "language, script, date_year, date_month, date_day,barcode, comment, editpending) " +
                "  VALUES (491240,'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1,491240,1,1,1,1, 1, 1, 1, 1, null, null, 1)");

        stmt.addBatch("INSERT INTO release(id, gid, name, artist_credit, release_group, status, packaging,country, " +
                "language, script, date_year, date_month, date_day,barcode, comment, editpending) " +
                "  VALUES (491241,'c3b8dbc9-c1ff-4743-9015-8d762819134e', 3, 1,491240,1,1,1,1, 1, 1, 1, 1, null, null, 1)");

        stmt.executeBatch();
        stmt.close();
        conn.close();
    }

    /**
     * @throws Exception
     */
    private void addReleaseGroupFour() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist_type(id,name)VALUES (1, 'Person');");
        stmt.addBatch("INSERT INTO artist_type(id,name)VALUES (2, 'Group');");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (1,'Erich Kunzel',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (2,'Kunzel, Eric',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (3,'The Cincinnati Pops Orchestra',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (4,'Cincinnati Pops Orchestra, The',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (5,'Cincinnati Pops',1)");

        stmt.addBatch("INSERT INTO artist(id,name, gid, sortname,comment, begindate_year,begindate_month,enddate_year,type,editpending)" +
                " VALUES (1,1, '99845d0c-f239-4051-a6b1-4b5e9f7ede0b',2,'a comment', 1978,null, 1995, 2, 0)");

        stmt.addBatch("INSERT INTO artist(id,name, gid, sortname,comment, begindate_year,begindate_month,enddate_year,type,editpending)" +
                    " VALUES (2,3, 'd8fbd94c-cd06-4e8b-a559-761ad969d07e',4,'a comment', 1978,null, 1995, 2, 0)");


        stmt.addBatch("INSERT INTO artist_credit( id, artistcount, refcount) VALUES (1, 1, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name( artist_credit, position, artist,name, joinphrase)" +
                "    VALUES (1, 0, 1, 1, 'and')");
        stmt.addBatch("INSERT INTO artist_credit_name( artist_credit, position, artist,name, joinphrase)" +
                "    VALUES (1, 1, 2, 5, null)");
        stmt.addBatch("INSERT INTO release_name(id,name, refcount)VALUES (1, 'Epics', 0)");
        stmt.addBatch("INSERT INTO release_group( id, gid,name,artist_credit,type,comment,editpending)" +
                "    VALUES (1, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1, 2, null, 0)");

        stmt.addBatch("INSERT INTO release_group_type(id,name) VALUES (1,'Non Album Tracks')");
        stmt.addBatch("INSERT INTO release_group_type(id,name) VALUES (2,'Album')");
        stmt.addBatch("INSERT INTO release_group_type(id,name) VALUES (3,'Single')");

        stmt.addBatch("INSERT INTO release(id, gid, name, artist_credit, release_group, status, packaging,country, " +
                "language, script, date_year, date_month, date_day,barcode, comment, editpending) " +
                "  VALUES (1,'c3b8dbc9-c1ff-4743-9015-8d762819134e', 1, 1,1,1,1,1,1, 1, 1, 1, 1, null, null, 1)");

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

        addReleaseGroupTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.RELEASEGROUP.getName()).length);
            assertEquals("Crocodiles", doc.getField(ReleaseGroupIndexField.RELEASEGROUP.getName()).stringValue());
            assertEquals("efd2ace2-b3b9-305f-8a53-9803595c0e37", doc.getField(ReleaseGroupIndexField.RELEASEGROUP_ID.getName()).stringValue());
            assertEquals("Echo & The Bunnymen", doc.getField(ReleaseGroupIndexField.ARTIST_NAME.getName()).stringValue());
            assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", doc.getField(ReleaseGroupIndexField.ARTIST_ID.getName()).stringValue());
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.RELEASE.getName()).length);
            assertEquals("Crocodiles (bonus disc)", doc.getField(ReleaseGroupIndexField.RELEASE.getName()).stringValue());

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
            assertEquals("album", doc.getField(ReleaseGroupIndexField.TYPE.getName()).stringValue());
        }
        ir.close();


    }

    public void testIndexReleaseGroupSortname() throws Exception {

        addReleaseGroupOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ARTIST_SORTNAME.getName()).length);
            assertEquals("Echo and The Bunnymen", doc.getField(ReleaseGroupIndexField.ARTIST_SORTNAME.getName()).stringValue());
        }
        ir.close();


    }

    /**
     * Checks record with type = null is set to unknown
     *
     * @throws Exception
     */
    /*
    //TODO what do we want to do in this situation, in NGS now returns no record
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
    */

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
            assertEquals(2, doc.getFields(ReleaseGroupIndexField.RELEASE.getName()).length);
            String val1 = doc.getFields(ReleaseGroupIndexField.RELEASE.getName())[0].stringValue();
            String val2 = doc.getFields(ReleaseGroupIndexField.RELEASE.getName())[1].stringValue();

            assertTrue("Crocodiles (Bonus disc)".equals(val1) || "Crocodiles (Bonus disc)".equals(val2));
            assertTrue("Crocodiles (Special disc)".equals(val1) || "Crocodiles (Special disc)".equals(val2));
        }
        ir.close();
    }


    public void testIndexReleaseGroupMultipleArtists() throws Exception {

        addReleaseGroupFour();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(2, doc.getFields(ReleaseGroupIndexField.ARTIST_SORTNAME.getName()).length);
            assertEquals("Kunzel, Eric",doc.getFields(ReleaseGroupIndexField.ARTIST_SORTNAME.getName())[0].stringValue());
            assertEquals("Cincinnati Pops Orchestra, The",doc.getFields(ReleaseGroupIndexField.ARTIST_SORTNAME.getName())[1].stringValue());
            assertEquals("Erich Kunzel",doc.getFields(ReleaseGroupIndexField.ARTIST_NAME.getName())[0].stringValue());
            assertEquals("The Cincinnati Pops Orchestra",doc.getFields(ReleaseGroupIndexField.ARTIST_NAME.getName())[1].stringValue());
            assertEquals("Erich Kunzel",doc.getFields(ReleaseGroupIndexField.ARTIST_NAMECREDIT.getName())[0].stringValue());
            assertEquals("Cincinnati Pops",doc.getFields(ReleaseGroupIndexField.ARTIST_NAMECREDIT.getName())[1].stringValue());
            assertEquals("99845d0c-f239-4051-a6b1-4b5e9f7ede0b",doc.getFields(ReleaseGroupIndexField.ARTIST_ID.getName())[0].stringValue());
            assertEquals("d8fbd94c-cd06-4e8b-a559-761ad969d07e",doc.getFields(ReleaseGroupIndexField.ARTIST_ID.getName())[1].stringValue());
            assertEquals("Erich Kunzel and Cincinnati Pops",doc.getFields(ReleaseGroupIndexField.ARTIST.getName())[0].stringValue());
            assertEquals("Epics",doc.getFields(ReleaseGroupIndexField.RELEASEGROUP.getName())[0].stringValue());
            assertEquals("efd2ace2-b3b9-305f-8a53-9803595c0e37",doc.getFields(ReleaseGroupIndexField.RELEASEGROUP_ID.getName())[0].stringValue());

        }
        ir.close();


    }

}