package org.musicbrainz.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;
import org.musicbrainz.search.index.ReleaseGroupIndex;
import org.musicbrainz.search.index.ReleaseGroupIndexField;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;


public class ReleaseGroupIndexTest extends AbstractIndexTest {

    private static Class INDEX_FIELD_CLASS = RecordingIndexField.class;
    
    private static String RELEASEGROUP_ONE_GID = "148a0024-1cf3-3b7c-a0f4-d653dc2ba44b";
    private static String RELEASEGROUP_TWO_GID = "ccd4879c-5e88-4385-b131-bf65296bf245";
    private static String ENTITY_TYPE = "release-group";

    public void setUp() throws Exception {
        super.setup();

    }

    private void createIndex(RAMDirectory ramDir) throws Exception {
        IndexWriter writer = new IndexWriter(ramDir, new PerFieldEntityAnalyzer(INDEX_FIELD_CLASS), true, IndexWriter.MaxFieldLength.LIMITED);
        ReleaseGroupIndex rgi = new ReleaseGroupIndex(createConnection());
        rgi.init();
        rgi.indexData(writer, 0, Integer.MAX_VALUE);
        rgi.destroy();
        writer.close();
    }

    /**
     * @throws Exception
     */
    private void addReleaseGroupOne() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist_name (id, name, refcount) values (1, 'Jacques Dutronc', 1)");
        stmt.addBatch("INSERT INTO artist_name (id, name, refcount) values (2, 'Dutronc', 1)");
        stmt.addBatch("INSERT INTO artist_name (id, name, refcount) values (3, 'Jacques', 1)");
        stmt.addBatch("INSERT INTO artist_name (id, name, refcount) values (4, 'Francoise Hardy', 1)");
        stmt.addBatch("INSERT INTO artist_name (id, name, refcount) values (5, 'Hardy', 1)");
        stmt.addBatch("INSERT INTO artist_name (id, name, refcount) values (6, 'Francoise', 1)");

        stmt.addBatch("INSERT INTO artist (id, gid, name, sortname, comment, begindate_year, begindate_month, enddate_year, type)" +
                " VALUES (39567, 'a65f4f19-9f4c-4a44-906f-87aa4ceee900', 1, 1, 'French male singer', 1943, 4, null, null)"
        );
        stmt.addBatch("INSERT INTO artist (id, gid, name, sortname, comment, begindate_year, begindate_month, enddate_year, type)" +
                " VALUES (33532, 'd2a79d20-1304-47fd-a998-b4fe1ec78373', 4, 1, 'French female singer', 1944, 1, null, null)"
        );

        stmt.addBatch("INSERT INTO artist_credit (id, artistcount, refcount) VALUES (1, 2, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name, joinphrase) " +
                "    VALUES (1, 0, 39567, 2, ' et ')," +
                "           (1, 1, 33532, 5, null)"
        );
        
        stmt.addBatch("INSERT INTO artist_credit (id, artistcount, refcount) VALUES (2, 2, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name, joinphrase) " +
                "    VALUES (2, 0, 39567, 3, ' et ')," +
                "           (2, 1, 33532, 6, null)"
        );

        stmt.addBatch("INSERT INTO release_name (id, name, refcount) VALUES (1, 'Le Meilleur des deux', 0)");
        stmt.addBatch("INSERT INTO release_name (id, name, refcount) VALUES (2, 'Le Meilleur des deux : integrale', 0)");
        
        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit, type, comment)" +
                "    VALUES (49191, '" + RELEASEGROUP_ONE_GID + "', 1, 1, 5, 'a rg comment')"
        );

        stmt.addBatch("INSERT INTO release_group_meta (id, releasecount) VALUES (49191, 1)");
        
        stmt.addBatch("INSERT INTO release (id, gid, name, artist_credit, release_group, status, packaging, country, " +
                "language, script, date_year, date_month, date_day, barcode, comment) " +
                "  VALUES (49191, '9a88cac2-0c3f-4aee-a742-38b661734f42', 2, 2, 49191, 1, 1, 1, 1, 1, 1, 1, 1, null, null)"
        );
       
        stmt.executeBatch();
        stmt.close();
        conn.close();
    }


    /**
     * No Type, no release
     *
     * @throws Exception
     */
    private void addReleaseGroupTwo() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist_name (id, name, refcount) values (1, 'Jacques Dutronc', 1)");
        stmt.addBatch("INSERT INTO artist_name (id, name, refcount) values (2, 'Jacques', 1)");
        stmt.addBatch("INSERT INTO artist_name (id, name, refcount) values (3, 'Francoise Hardy', 1)");
        stmt.addBatch("INSERT INTO artist_name (id, name, refcount) values (4, 'Francoise', 1)");

        stmt.addBatch("INSERT INTO artist (id, gid, name, sortname, comment, begindate_year, begindate_month, enddate_year, type)" +
                " VALUES (39567, 'a65f4f19-9f4c-4a44-906f-87aa4ceee900', 1, 1, 'French male singer', 1943, 4, null, null)"
        );
        stmt.addBatch("INSERT INTO artist (id, gid, name, sortname, comment, begindate_year, begindate_month, enddate_year, type)" +
                " VALUES (33532, 'd2a79d20-1304-47fd-a998-b4fe1ec78373', 3, 1, 'French female singer', 1944, 1, null, null)"
        );

        stmt.addBatch("INSERT INTO artist_credit (id, artistcount, refcount) VALUES (1, 2, 1)");

        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name, joinphrase) " +
                "    VALUES (1, 0, 39567, 2, ' et ')," +
                "           (1, 1, 33532, 4, null)"
        );

        stmt.addBatch("INSERT INTO release_name (id, name, refcount) VALUES (1, 'Le Meilleur des deux', 0)");
        stmt.addBatch("INSERT INTO release_name (id, name, refcount) VALUES (2, 'Le Meilleur des deux : integrale)', 0)");

        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit, type, comment)" +
                "    VALUES (49191, '" + RELEASEGROUP_TWO_GID + "', 1, 1, null, null)"
        );

        stmt.addBatch("INSERT INTO release_group_meta (id, releasecount, firstreleasedate_year, firstreleasedate_month, firstreleasedate_day)" +
                "    VALUES (49191, 0, 1998, 4, null)"
        );
        
        stmt.executeBatch();
        stmt.close();
        conn.close();
    }
 
    /**
     * Checks record with type = null is indexed
     *
     * @throws Exception
     */
    public void testIndexReleaseGroupWithNoType() throws Exception {

        addReleaseGroupTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();
    }

    public void testSearchByReleaseGroupType() throws Exception {

        addReleaseGroupOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();
        
        String query = ReleaseGroupIndexField.TYPE + ":\"Compilation\"";
        List<Document> results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ENTITY_GID.getName()).length);
            assertEquals(RELEASEGROUP_ONE_GID, doc.getField(ReleaseGroupIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(ReleaseGroupIndexField.ENTITY_TYPE.getName()).stringValue());
        }
        
    }
    
    public void testSearchByReleaseGroupComment() throws Exception {

        addReleaseGroupOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();
        
        String query = ReleaseGroupIndexField.COMMENT + ":\"a rg comment\"";
        List<Document> results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ENTITY_GID.getName()).length);
            assertEquals(RELEASEGROUP_ONE_GID, doc.getField(ReleaseGroupIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(ReleaseGroupIndexField.ENTITY_TYPE.getName()).stringValue());
        }
        
    }
    
    public void testSearchByReleaseGroupName() throws Exception {

        addReleaseGroupTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();
        
        String query = ReleaseGroupIndexField.RELEASEGROUP + ":\"Le Meilleur des deux\"";
        List<Document> results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ENTITY_GID.getName()).length);
            assertEquals(RELEASEGROUP_TWO_GID, doc.getField(ReleaseGroupIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(ReleaseGroupIndexField.ENTITY_TYPE.getName()).stringValue());
        }
        
    }
    
    public void testSearchByReleaseName() throws Exception {

        addReleaseGroupOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();
        
        String query = ReleaseGroupIndexField.RELEASES + ":\"Le Meilleur des deux : integrale\"";
        List<Document> results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ENTITY_GID.getName()).length);
            assertEquals(RELEASEGROUP_ONE_GID, doc.getField(ReleaseGroupIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(ReleaseGroupIndexField.ENTITY_TYPE.getName()).stringValue());
        }
        
    }

    public void testSearchByReleaseGroupArtistName() throws Exception {

        addReleaseGroupTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        // Check if something has been indexed
        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();     

        // Try to search using this piece of information
        String query = ReleaseGroupIndexField.ARTIST + ":\"Jacques Dutronc\"";
        List<Document> results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ENTITY_GID.getName()).length);
            assertEquals(RELEASEGROUP_TWO_GID, doc.getField(ReleaseGroupIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(ReleaseGroupIndexField.ENTITY_TYPE.getName()).stringValue());
        }
        
        // Try to search using this piece of information
        query = ReleaseGroupIndexField.ARTIST + ":\"Francoise Hardy\"";
        results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ENTITY_GID.getName()).length);
            assertEquals(RELEASEGROUP_TWO_GID, doc.getField(ReleaseGroupIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(ReleaseGroupIndexField.ENTITY_TYPE.getName()).stringValue());
        }
        
    }
    
    public void testSearchByReleaseArtistName() throws Exception {

        addReleaseGroupOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        // Check if something has been indexed
        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();     

        // Try to search using this piece of information
        // Try to search using this piece of information
        String query = ReleaseGroupIndexField.ARTIST + ":\"Jacques Dutronc\"";
        List<Document> results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ENTITY_GID.getName()).length);
            assertEquals(RELEASEGROUP_ONE_GID, doc.getField(ReleaseGroupIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(ReleaseGroupIndexField.ENTITY_TYPE.getName()).stringValue());
        }
        
        // Try to search using this piece of information
        query = ReleaseGroupIndexField.ARTIST + ":\"Francoise Hardy\"";
        results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ENTITY_GID.getName()).length);
            assertEquals(RELEASEGROUP_ONE_GID, doc.getField(ReleaseGroupIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(ReleaseGroupIndexField.ENTITY_TYPE.getName()).stringValue());
        }
        
    }
    
    public void testSearchByReleaseGroupArtistCreditName() throws Exception {

        addReleaseGroupTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        // Check if something has been indexed
        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();     

        // Try to search using this piece of information
        String query = ReleaseGroupIndexField.ARTIST + ":\"Jacques\"";
        List<Document> results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ENTITY_GID.getName()).length);
            assertEquals(RELEASEGROUP_TWO_GID, doc.getField(ReleaseGroupIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(ReleaseGroupIndexField.ENTITY_TYPE.getName()).stringValue());
        }
        
        query = ReleaseGroupIndexField.ARTIST + ":\"Francoise\"";
        results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ENTITY_GID.getName()).length);
            assertEquals(RELEASEGROUP_TWO_GID, doc.getField(ReleaseGroupIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(ReleaseGroupIndexField.ENTITY_TYPE.getName()).stringValue());
        }
        
    }
    
    public void testSearchByReleaseArtistCreditName() throws Exception {

        addReleaseGroupOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        // Check if something has been indexed
        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();     

        // Try to search using this piece of information
        String query = ReleaseGroupIndexField.ARTIST + ":\"Jacques\"";
        List<Document> results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ENTITY_GID.getName()).length);
            assertEquals(RELEASEGROUP_ONE_GID, doc.getField(ReleaseGroupIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(ReleaseGroupIndexField.ENTITY_TYPE.getName()).stringValue());
        }
        
        query = ReleaseGroupIndexField.ARTIST + ":\"Francoise\"";
        results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ENTITY_GID.getName()).length);
            assertEquals(RELEASEGROUP_ONE_GID, doc.getField(ReleaseGroupIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(ReleaseGroupIndexField.ENTITY_TYPE.getName()).stringValue());
        }
        
    }
    
    /**
     * In order to check that join phrase is correctly indexed
     */
    public void testSearchByReleaseGroupFullArtistCreditName() throws Exception {

        addReleaseGroupTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        // Check if something has been indexed
        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();     

        // Try to search using this piece of information
        String query = ReleaseGroupIndexField.ARTIST + ":\"Jacques et Francoise\"";
        List<Document> results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ENTITY_GID.getName()).length);
            assertEquals(RELEASEGROUP_TWO_GID, doc.getField(ReleaseGroupIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(ReleaseGroupIndexField.ENTITY_TYPE.getName()).stringValue());
        }
        
    }

    /**
     * In order to check that join phrase is correctly indexed
     */
    public void testSearchByReleaseFullArtistCreditName() throws Exception {

        addReleaseGroupOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        // Check if something has been indexed
        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();     

        // Try to search using this piece of information
        String query = ReleaseGroupIndexField.ARTIST + ":\"Jacques et Francoise\"";
        List<Document> results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ENTITY_GID.getName()).length);
            assertEquals(RELEASEGROUP_ONE_GID, doc.getField(ReleaseGroupIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(ReleaseGroupIndexField.ENTITY_TYPE.getName()).stringValue());
        }

    }
    
    public void testSearchByReleaseGroupFirstReleaseDate() throws Exception {

        addReleaseGroupTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();
        
        // Try to search using this piece of information
        String query = ReleaseGroupIndexField.FIRST_RELEASE_DATE + ":1998-04";
        List<Document> results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(ArtistIndexField.ENTITY_GID.getName()).length);
            assertEquals(RELEASEGROUP_TWO_GID, doc.getField(ArtistIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(ArtistIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(ArtistIndexField.ENTITY_TYPE.getName()).stringValue());
        }
    }
    
}