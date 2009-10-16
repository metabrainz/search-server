package org.musicbrainz.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.musicbrainz.search.index.ArtistIndex;
import org.musicbrainz.search.index.ArtistIndexField;

import java.sql.Connection;
import java.sql.Statement;


public class ArtistIndexTest extends AbstractIndexTest {


    public void setUp() throws Exception {
        super.setup();
    }

    private void createIndex(RAMDirectory ramDir) throws Exception {
        PerFieldAnalyzerWrapper analyzer = new ArtistAnalyzer();
        IndexWriter writer = new IndexWriter(ramDir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        ArtistIndex ai = new ArtistIndex(createConnection());
        ai.indexData(writer, 0, Integer.MAX_VALUE);
        writer.close();

    }


    private void addArtistOne() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist_type(id,name)VALUES (1, 'Person');");
        stmt.addBatch("INSERT INTO artist_type(id,name)VALUES (2, 'Group');");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (1,'Farming Incident',1)");
        stmt.addBatch("INSERT INTO artist(id,name, gid, sortname,comment, begindate_year,begindate_month,enddate_year,type,editpending,gender,country)" +
            " VALUES (521316,1, '4302e264-1cf0-4d1f-aca7-2a6f89e34b36',1,null, 1999,4, null, 2, 0,1,1)");
        stmt.addBatch("INSERT INTO gender(id, name)VALUES (1,'Male')");
        stmt.addBatch("INSERT INTO gender(id, name)VALUES (2,'Female')");
        stmt.addBatch("INSERT INTO country( id, isocode, name)VALUES (1,'AF','Afghanistan')");

        stmt.executeBatch();
        stmt.close();
    }


    private void addArtistTwo() throws Exception {
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
        stmt.executeBatch();
        stmt.close();
    }

    private void addArtistThree() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();
        stmt.addBatch("INSERT INTO artist_type(id,name)VALUES (1, 'Person');");
        stmt.addBatch("INSERT INTO artist_type(id,name)VALUES (2, 'Group');");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (1,'Farming Incident',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (2,'Siobhan Lynch',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (3,'Lynch, Siobhan',1)");

        stmt.addBatch("INSERT INTO artist(id,name, gid, sortname,comment, begindate_year,begindate_month,enddate_year,type,editpending)" +
            " VALUES (76834,2, 'ae8707b6-684c-4d4a-95c5-d117970a6dfe',3,null, null, null, null, null, 0)");

        stmt.executeBatch();
        stmt.close();
    }


    /**
     * Checks fields are indexed correctly for artist with no alias
     *
     * @throws Exception
     */
    public void testIndexArtistWithNoAlias() throws Exception {

        addArtistOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(0, doc.getFields(ArtistIndexField.ALIAS.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.ARTIST.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.ARTIST_ID.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.SORTNAME.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.BEGIN.getName()).length);
            assertEquals(0, doc.getFields(ArtistIndexField.END.getName()).length);
            assertEquals(0, doc.getFields(ArtistIndexField.COMMENT.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.TYPE.getName()).length);

            assertEquals("Farming Incident", doc.getField(ArtistIndexField.ARTIST.getName()).stringValue());
            assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.getField(ArtistIndexField.ARTIST_ID.getName()).stringValue());
            assertEquals("Farming Incident", doc.getField(ArtistIndexField.SORTNAME.getName()).stringValue());
            assertEquals("1999-04", doc.getField(ArtistIndexField.BEGIN.getName()).stringValue());
            assertEquals("group", doc.getField(ArtistIndexField.TYPE.getName()).stringValue());
        }
        ir.close();

    }


    public void testIndexArtistWithType() throws Exception {

        addArtistOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ArtistIndexField.TYPE.getName()).length);
            assertEquals("group", doc.getField(ArtistIndexField.TYPE.getName()).stringValue());
        }
        ir.close();
    }


    public void testIndexArtistWithComment() throws Exception {

        addArtistTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ArtistIndexField.COMMENT.getName()).length);
            assertEquals("a comment", doc.getField(ArtistIndexField.COMMENT.getName()).stringValue());
        }
        ir.close();
    }

    public void testIndexArtistWithCountry() throws Exception {

            addArtistOne();
            RAMDirectory ramDir = new RAMDirectory();
            createIndex(ramDir);

            IndexReader ir = IndexReader.open(ramDir, true);
            assertEquals(1, ir.numDocs());
            {
                Document doc = ir.document(0);
                assertEquals(1, doc.getFields(ArtistIndexField.COUNTRY.getName()).length);
                assertEquals("af", doc.getField(ArtistIndexField.COUNTRY.getName()).stringValue());
            }
            ir.close();
        }

    public void testIndexArtistWithNoCountry() throws Exception {

            addArtistTwo();
            RAMDirectory ramDir = new RAMDirectory();
            createIndex(ramDir);

            IndexReader ir = IndexReader.open(ramDir, true);
            assertEquals(1, ir.numDocs());
            {
                Document doc = ir.document(0);
                assertEquals(0, doc.getFields(ArtistIndexField.COUNTRY.getName()).length);
             }
            ir.close();
        }
    public void testIndexArtistWithGender() throws Exception {

        addArtistOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ArtistIndexField.GENDER.getName()).length);
            assertEquals("male", doc.getField(ArtistIndexField.GENDER.getName()).stringValue());
        }
        ir.close();
    }

    public void testIndexArtistWithNoGender() throws Exception {

        addArtistTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(0, doc.getFields(ArtistIndexField.GENDER.getName()).length);
        }
        ir.close();
    }


    /**
     * Checks fields are indexed correctly for artist with alias (the aliases are not stored)
     *
     * @throws Exception
     */
    public void testIndexArtistWithAlias() throws Exception {

        addArtistTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(0, doc.getFields(ArtistIndexField.ALIAS.getName()).length); //aliases are searchable but not stored
            assertEquals(1, doc.getFields(ArtistIndexField.ARTIST.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.ARTIST_ID.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.SORTNAME.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.BEGIN.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.END.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.COMMENT.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.TYPE.getName()).length);

            assertEquals("Echo & The Bunnymen", doc.getField(ArtistIndexField.ARTIST.getName()).stringValue());
            assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", doc.getField(ArtistIndexField.ARTIST_ID.getName()).stringValue());
            assertEquals("Echo & The Bunnymen", doc.getField(ArtistIndexField.SORTNAME.getName()).stringValue());
            assertEquals("1978", doc.getField(ArtistIndexField.BEGIN.getName()).stringValue());
            assertEquals("group", doc.getField(ArtistIndexField.TYPE.getName()).stringValue());
            assertEquals("a comment", doc.getField(ArtistIndexField.COMMENT.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * Checks zeroes are removed from date
     *
     * @throws Exception
     */
    public void testBeginDate() throws Exception {

        addArtistTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ArtistIndexField.BEGIN.getName()).length);
            assertEquals("1978", doc.getField(ArtistIndexField.BEGIN.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * Checks zeroes are removed from date
     *
     * @throws Exception
     */
    public void testEndDate() throws Exception {

        addArtistTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ArtistIndexField.END.getName()).length);
            assertEquals("1995", doc.getField(ArtistIndexField.END.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * Checks record with type = null is set to unknown
     *
     * @throws Exception
     */
    public void testIndexArtistWithNoType() throws Exception {

        addArtistThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ArtistIndexField.TYPE.getName()).length);
            assertEquals("unknown", doc.getField(ArtistIndexField.TYPE.getName()).stringValue());
        }
        ir.close();
    }


    /**
     * Checks record with comment = null is not indexed
     *
     * @throws Exception
     */
    public void testIndexArtistWithNoComment() throws Exception {

        addArtistThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(0, doc.getFields(ArtistIndexField.COMMENT.getName()).length);
        }
        ir.close();
    }


    /**
     * Checks record with begin date = null is not indexed
     *
     * @throws Exception
     */
    public void testIndexArtistWithNoBeginDate() throws Exception {

        addArtistThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(0, doc.getFields(ArtistIndexField.BEGIN.getName()).length);
        }
        ir.close();
    }


    /**
     * Checks record with end date = null is not indexed
     *
     * @throws Exception
     */
    public void testIndexArtistWithNoEndDate() throws Exception {

        addArtistThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(0, doc.getFields(ArtistIndexField.END.getName()).length);
            assertEquals(0, doc.getFields(ArtistIndexField.COMMENT.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.TYPE.getName()).length);

            assertEquals("Siobhan Lynch", doc.getField(ArtistIndexField.ARTIST.getName()).stringValue());
            assertEquals("ae8707b6-684c-4d4a-95c5-d117970a6dfe", doc.getField(ArtistIndexField.ARTIST_ID.getName()).stringValue());
            assertEquals("Lynch, Siobhan", doc.getField(ArtistIndexField.SORTNAME.getName()).stringValue());
            assertEquals("unknown", doc.getField(ArtistIndexField.TYPE.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * Checks fields with different sort name to name is indexed correctly
     *
     * @throws Exception
     */
    public void testIndexArtistWithDifferentSortName() throws Exception {

        addArtistThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ArtistIndexField.ARTIST.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.SORTNAME.getName()).length);
            assertEquals("Siobhan Lynch", doc.getField(ArtistIndexField.ARTIST.getName()).stringValue());
            assertEquals("Lynch, Siobhan", doc.getField(ArtistIndexField.SORTNAME.getName()).stringValue());
        }
        ir.close();
    }


    public void testGetTypeByDbId () throws Exception {        
        assertEquals(ArtistType.PERSON,ArtistType.getBySearchId(1));
    }
}