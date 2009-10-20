package org.musicbrainz.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;
import org.musicbrainz.search.index.ArtistIndex;
import org.musicbrainz.search.index.ArtistIndexField;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;


public class ArtistIndexTest extends AbstractIndexTest {

    private static Class INDEX_FIELD_CLASS = ArtistIndexField.class;
    
    private static String ARTIST_ONE_GID = "4302e264-1cf0-4d1f-aca7-2a6f89e34b36";
    private static String ARTIST_TWO_GID = "ccd4879c-5e88-4385-b131-bf65296bf245";
    private static String ARTIST_THREE_GID = "ae8707b6-684c-4d4a-95c5-d117970a6dfe";
    private static String ENTITY_TYPE = "artist";

    public void setUp() throws Exception {
        super.setup();
    }

    private void createIndex(RAMDirectory ramDir) throws Exception {
        IndexWriter writer = new IndexWriter(ramDir, new PerFieldEntityAnalyzer(INDEX_FIELD_CLASS), true, IndexWriter.MaxFieldLength.LIMITED);
        ArtistIndex ai = new ArtistIndex(createConnection());
        ai.init();
        ai.indexData(writer, 0, Integer.MAX_VALUE);
        ai.destroy();
        writer.close();
    }


    private void addArtistOne() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();
        
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Farming Incident')");
        
        stmt.addBatch(
            "INSERT INTO artist (id, gid, name, sortname, comment, type, gender, country, " + 
            "  begindate_year, begindate_month, begindate_day, enddate_year, enddate_month, enddate_day) " +
            "VALUES (1, '" + ARTIST_ONE_GID + "', 1, 1, null, 2, null, null, " +
            "  1999, 04, null, null, null, null)");

        stmt.executeBatch();
        stmt.close();
    }


    private void addArtistTwo() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();
        
        stmt.addBatch("INSERT INTO country (id, isocode, name) VALUES (1, 'AF','Afghanistan')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Echo & The Bunnymen')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (2, 'Echo & The Bunnyman')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (3, 'Echo and The Bunnymen')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (4, 'Echo And The Bunnymen ACN')");
        
        stmt.addBatch("INSERT INTO artist_alias (artist, name) VALUES (2, 1)");
        stmt.addBatch("INSERT INTO artist_alias (artist, name) VALUES (2, 2)");
        stmt.addBatch("INSERT INTO artist_alias (artist, name) VALUES (2, 3)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name, joinphrase) " +
                "VALUES (1, 1, 2, 4, null)"
        );

        stmt.addBatch(
                "INSERT INTO artist (id, gid, name, sortname, comment, type, gender, country, " + 
                "  begindate_year, begindate_month, begindate_day, enddate_year, enddate_month, enddate_day) " +
                "VALUES (2, '" + ARTIST_TWO_GID + "', 1, 1, 'a comment', 2, null, 1, " +
                "  1978, null, null, 1995, null, null)"
        );
        stmt.executeBatch();
        stmt.close();
    }

    private void addArtistThree() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();
        
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Siobhan Lynch')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (2, 'Lynch, Siobhan')");
        
        stmt.addBatch(
                "INSERT INTO artist (id, gid, name, sortname, comment, type, gender, country, " + 
                "  begindate_year, begindate_month, begindate_day, enddate_year, enddate_month, enddate_day) " +
                "VALUES (3, '" + ARTIST_THREE_GID + "', 1, 2, null, null, 1, null, " +
                "  null, null, null, null, null, null)"
        );

        stmt.executeBatch();
        stmt.close();
    }


    /**
     * Checks date are indexed correctly for artist with no alias
     *
     * @throws Exception
     */
    public void testIndexArtistWithNoAlias() throws Exception {

        addArtistOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        System.out.println(ir.numDocs());
        assertEquals(1, ir.numDocs());
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
        ir.close();
        
        // Try to search using this piece of information
        String query = ArtistIndexField.ALIAS + ":\"Echo & The Bunnyman\"";
        List<Document> results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(ArtistIndexField.ENTITY_GID.getName()).length);
            assertEquals(ARTIST_TWO_GID, doc.getField(ArtistIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(ArtistIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(ArtistIndexField.ENTITY_TYPE.getName()).stringValue());
        }
    }

    public void testIndexArtistWithType() throws Exception {

        addArtistOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        // Check if something has been indexed
        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            System.out.println(doc.getField(ArtistIndexField.TYPE.getName()).stringValue());
            
        }
        ir.close();
        
        // Try to search using this piece of information
        String query = ArtistIndexField.TYPE + ":Group";
        List<Document> results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(ArtistIndexField.ENTITY_GID.getName()).length);
            assertEquals(ARTIST_ONE_GID, doc.getField(ArtistIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(ArtistIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(ArtistIndexField.ENTITY_TYPE.getName()).stringValue());
        }
    }

    public void testIndexArtistWithComment() throws Exception {

        addArtistTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();
    }

    /**
     * Checks fields are indexed correctly for artist with credit name
     *
     * @throws Exception
     */
    public void testIndexArtistWithCreditName() throws Exception {

        addArtistTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();
        
        // Try to search using this piece of information
        String query = ArtistIndexField.ALIAS + ":\"Echo And The Bunnymen ACN\"";
        List<Document> results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(ArtistIndexField.ENTITY_GID.getName()).length);
            assertEquals(ARTIST_TWO_GID, doc.getField(ArtistIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(ArtistIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(ArtistIndexField.ENTITY_TYPE.getName()).stringValue());
        }
    }
    
    /**
     * Checks zeroes are removed from date
     *
     * @throws Exception
     */
    public void testIndexArtistWithBeginDate() throws Exception {

        addArtistTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();
        
        // Try to search using this piece of information
        String query = ArtistIndexField.BEGIN + ":1978";
        List<Document> results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(ArtistIndexField.ENTITY_GID.getName()).length);
            assertEquals(ARTIST_TWO_GID, doc.getField(ArtistIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(ArtistIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(ArtistIndexField.ENTITY_TYPE.getName()).stringValue());
        }
    }

    /**
     * Checks zeroes are removed from date
     *
     * @throws Exception
     */
    public void testIndexArtistWithEndDate() throws Exception {

        addArtistTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        ir.close();
        
        // Try to search using this piece of information
        String query = ArtistIndexField.END + ":1995";
        List<Document> results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(ArtistIndexField.ENTITY_GID.getName()).length);
            assertEquals(ARTIST_TWO_GID, doc.getField(ArtistIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(ArtistIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(ArtistIndexField.ENTITY_TYPE.getName()).stringValue());
        }
    }

    /**
     * Checks record with type null is indexed
     *
     * @throws Exception
     */
    public void testIndexArtistWithNoType() throws Exception {

        addArtistThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
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
        ir.close();
        
        // Try to search using this piece of information
        String query = ArtistIndexField.SORTNAME + ":\"Lynch, Siobhan\"";
        List<Document> results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(ArtistIndexField.ENTITY_GID.getName()).length);
            assertEquals(ARTIST_THREE_GID, doc.getField(ArtistIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(ArtistIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(ArtistIndexField.ENTITY_TYPE.getName()).stringValue());
        }
    }

    /**
     * Checks record with country null is indexed
     *
     * @throws Exception
     */
    public void testIndexArtistWithNoCountry() throws Exception {

        addArtistOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();
    }

    public void testIndexArtistWithCountry() throws Exception {

        addArtistTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();
        
        // Try to search using this piece of information
        String query = ArtistIndexField.COUNTRY + ":af";
        List<Document> results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(ArtistIndexField.ENTITY_GID.getName()).length);
            assertEquals(ARTIST_TWO_GID, doc.getField(ArtistIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(ArtistIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(ArtistIndexField.ENTITY_TYPE.getName()).stringValue());
        }
    }    
    
    /**
     * Checks record with genre null is indexed
     *
     * @throws Exception
     */
    public void testIndexArtistWithNoGenre() throws Exception {

        addArtistOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();
    }
    
    public void testIndexArtistWithGenre() throws Exception {

        addArtistThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();
        
        // Try to search using this piece of information
        String query = ArtistIndexField.GENDER + ":Male";
        List<Document> results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(ArtistIndexField.ENTITY_GID.getName()).length);
            assertEquals(ARTIST_THREE_GID, doc.getField(ArtistIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(ArtistIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(ArtistIndexField.ENTITY_TYPE.getName()).stringValue());
        }
    } 
    
}