package org.musicbrainz.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.musicbrainz.search.index.ArtistIndex;
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.sql.Connection;
import java.sql.Statement;


public class ArtistIndexTest extends AbstractIndexTest {


    public void setUp() throws Exception {
        super.setup();
    }

    private void createIndex(RAMDirectory ramDir) throws Exception {
        PerFieldAnalyzerWrapper analyzer = new PerFieldEntityAnalyzer(ArtistIndexField.class);
        IndexWriter writer = new IndexWriter(ramDir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        ArtistIndex ai = new ArtistIndex(createConnection());
        ai.init(writer);
        ai.indexData(writer, 0, Integer.MAX_VALUE);
        ai.destroy();
        writer.close();

    }


    private void addArtistOne() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Farming Incident')");
        stmt.addBatch("INSERT INTO artist (id, name, gid, sortname, comment, begindate_year, begindate_month, enddate_year, type, gender, country)" +
            " VALUES (521316, 1, '4302e264-1cf0-4d1f-aca7-2a6f89e34b36', 1, null, 1999,4, null, 2, 1, 1)");
        stmt.addBatch("INSERT INTO country (id, isocode, name) VALUES (1, 'AF', 'Afghanistan')");

        stmt.executeBatch();
        stmt.close();
    }


    private void addArtistTwo() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1,'Echo & The Bunnymen')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (2,'Echo and The Bunnymen')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (3,'Echo & The Bunnyman')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (4,'Echo And The Bunnymen')");

        stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES(1, 16153, 2)");
        stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES(2, 16153, 3)");
        stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES(3, 16153, 4)");

        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (5,'Bunnymen Orchestra')");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name, joinphrase) " +
                " VALUES (1, 0, 16153, 5, null)");

        //This is same as alias, so should be ignored
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name, joinphrase) " +
                " VALUES (1, 0, 16153, 3, null)");

        stmt.addBatch("INSERT INTO artist (id, name, gid, sortname, comment, begindate_year, begindate_month, enddate_year, type)" +
                   " VALUES (16153, 1, 'ccd4879c-5e88-4385-b131-bf65296bf245', 1, 'a comment', 1978, null, 1995, 2)");
        stmt.executeBatch();
        stmt.close();
    }

    private void addArtistThree() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Farming Incident')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (2,'Siobhan Lynch')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (3,'Lynch, Siobhan')");

        stmt.addBatch("INSERT INTO artist (id, name, gid, sortname, comment, begindate_year, begindate_month, enddate_year, type)" +
            " VALUES (76834,2, 'ae8707b6-684c-4d4a-95c5-d117970a6dfe', 3, null, null, null, null, null)");

        stmt.addBatch("INSERT INTO tag (id, name, refcount) VALUES (1, 'Goth', 2)");
        stmt.addBatch("INSERT INTO artist_tag (artist, tag, count) VALUES (76834, 1, 10)");
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
     * Checks fields are indexed correctly for artist with alias and artistCredit (the aliases are not stored)
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
            assertEquals(4, doc.getFields(ArtistIndexField.ALIAS.getName()).length); //aliases are searchable but not stored
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


    /**
     * Checks fields with different sort name to name is indexed correctly
     *
     * @throws Exception
     */
    public void testIndexArtistWithTag() throws Exception {

        addArtistThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ArtistIndexField.ARTIST.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.TAG.getName()).length);
            assertEquals("Goth", doc.getField(ArtistIndexField.TAG.getName()).stringValue());
            assertEquals(1, doc.getFields(ArtistIndexField.TAGCOUNT.getName()).length);
            assertEquals("10", doc.getField(ArtistIndexField.TAGCOUNT.getName()).stringValue());
        }
        ir.close();
    }


    public void testGetTypeByDbId () throws Exception {        
        assertEquals(ArtistType.PERSON,ArtistType.getBySearchId(1));
    }
}