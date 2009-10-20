package org.musicbrainz.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;
import org.musicbrainz.search.index.LabelIndex;
import org.musicbrainz.search.index.LabelIndexField;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;


public class LabelIndexTest extends AbstractIndexTest {

    private static Class INDEX_FIELD_CLASS = LabelIndexField.class;
    
    private static String LABEL_ONE_GID = "a539bb1e-f2e1-4b45-9db8-8053841e7503";
    private static String LABEL_TWO_GID = "d8caa692-704d-412b-a410-4fbcf5b9c796";
    private static String LABEL_THREE_GID = "d8caa692-704d-412b-a410-4fbcf5b9c796";
    private static String ENTITY_TYPE = "label";
    
    public void setUp() throws Exception {
        super.setup();
    }

    private void createIndex(RAMDirectory ramDir) throws Exception {
        IndexWriter writer = new IndexWriter(ramDir, new PerFieldEntityAnalyzer(INDEX_FIELD_CLASS), true, IndexWriter.MaxFieldLength.LIMITED);
        LabelIndex li = new LabelIndex(createConnection());
        li.init();
        li.indexData(writer, 0, Integer.MAX_VALUE);
        li.destroy();
        writer.close();
    }

    /**
     * Some fields populated, has alias, but no country
     *
     * @throws Exception
     */
    private void addLabelOne() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO label_name (id, name) VALUES (1, '4AD')");
        stmt.addBatch("INSERT INTO label_name (id, name) VALUES (2, '4AD US')");

        stmt.addBatch("INSERT INTO label (id, gid, name, sortname, type, labelcode, country, comment, " + 
                "	begindate_year, begindate_month, begindate_day, enddate_year, enddate_month, enddate_day) " +
                "VALUES (1, '" + LABEL_ONE_GID + "', 1, 1, 4, 5807, null, null, " +
                "	1979, null, null, null, null, null)"
        );
        stmt.addBatch("INSERT INTO label_alias (label, name) VALUES (1, 2)");

        stmt.executeBatch();
        stmt.close();
        conn.close();
    }

    /**
     * Most fields populated, but no alias
     *
     * @throws Exception
     */
    private void addLabelTwo() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO country (id, isocode, name) VALUES (38, 'CA','Canada')");
        stmt.addBatch("INSERT INTO label_name (id, name) VALUES (3, 'MusicBrainz Data Testing Label')");
        stmt.addBatch("INSERT INTO label_name (id, name) VALUES (4, 'Data Testing Label, MusicBrainz')");

        stmt.addBatch("INSERT INTO label (id, gid, name, sortname, type, labelcode, country, comment, " + 
                "	begindate_year, begindate_month, begindate_day, enddate_year, enddate_month, enddate_day) " +
                "VALUES (2, '" + LABEL_TWO_GID + "', 3, 4, 1, 0099998, 38, 'DO NOT EDIT THIS LABEL', " +
                "	2009, 1, 1, 2009, 4, null)"
        );

        stmt.executeBatch();
        stmt.close();
        conn.close();
    }

    /**
     * Some fields, no alias but has country
     *
     * @throws Exception
     */
    private void addLabelThree() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO country (id, isocode, name) VALUES (1, 'AF','Afghanistan')");
        stmt.addBatch("INSERT INTO label_name (id, name) VALUES (1, '4AD')");
        stmt.addBatch("INSERT INTO label_name (id, name) VALUES (2, '4AD US')");

        stmt.addBatch("INSERT INTO label (id, gid, name, sortname, type, labelcode, country, comment, " + 
                "	begindate_year, begindate_month, begindate_day, enddate_year, enddate_month, enddate_day) " +
                "VALUES (3, '" + LABEL_THREE_GID + "', 1, 1, null, null, 1, null, " +
                "	null, null, null, null, null, null)"
        );
        stmt.addBatch("INSERT INTO label_alias (label, name) VALUES (3, 2)");

        stmt.executeBatch();
        stmt.close();
        conn.close();
    }

    public void testIndexLabelWithNoCountry() throws Exception {

        addLabelOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        // Check if something has been indexed
        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();

    }

    public void testIndexLabelWithCountry() throws Exception {

        addLabelThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        // Check if something has been indexed
        IndexReader ir = IndexReader.open(ramDir, true);
        ir.close();

        // Try to search using this piece of information
        String query = LabelIndexField.COUNTRY + ":af";
        List<Document> results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(LabelIndexField.ENTITY_GID.getName()).length);
            assertEquals(LABEL_THREE_GID, doc.getField(LabelIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(LabelIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(LabelIndexField.ENTITY_TYPE.getName()).stringValue());
        }
    }

    public void testIndexLabelWithComment() throws Exception {

        addLabelTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        // Check if something has been indexed
        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();     

        // Try to search using this piece of information
        String query; 
        List<Document> results;
        query = LabelIndexField.COMMENT + ":\"DO NOT EDIT THIS LABEL\"";
        results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(LabelIndexField.ENTITY_GID.getName()).length);
            assertEquals(LABEL_TWO_GID, doc.getField(LabelIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(LabelIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(LabelIndexField.ENTITY_TYPE.getName()).stringValue());
        }
        
        // Search again in an altered form
        query = LabelIndexField.COMMENT + ":\"NOT eDiT THIS label\"";
        results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(LabelIndexField.ENTITY_GID.getName()).length);
            assertEquals(LABEL_TWO_GID, doc.getField(LabelIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(LabelIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(LabelIndexField.ENTITY_TYPE.getName()).stringValue());
        }
        
    }

    public void testIndexLabelWithNoLabelCode() throws Exception {

        addLabelThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();
    }

    public void testIndexLabelWithLabelCode() throws Exception {

        addLabelOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        ir.close();
        
        // Try to search using this piece of information
        String query = LabelIndexField.CODE + ":5807";
        List<Document> results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(LabelIndexField.ENTITY_GID.getName()).length);
            assertEquals(LABEL_ONE_GID, doc.getField(LabelIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(LabelIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(LabelIndexField.ENTITY_TYPE.getName()).stringValue());
        }
    }

    public void testIndexLabelWithLabelCodeWithZeroes() throws Exception {

        addLabelTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        ir.close();
        
        // Try to search using this piece of information
        String query = LabelIndexField.CODE + ":99998";
        List<Document> results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(LabelIndexField.ENTITY_GID.getName()).length);
            assertEquals(LABEL_TWO_GID, doc.getField(LabelIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(LabelIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(LabelIndexField.ENTITY_TYPE.getName()).stringValue());
        }
    }

    /**
     * Checks fields are indexed correctly for label with alias
     *
     * @throws Exception
     */
    public void testIndexLabelWithAlias() throws Exception {

        addLabelOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        ir.close();
        
        // Try to search using this piece of information
        String query = LabelIndexField.ALIAS + ":\"4AD US\"";
        List<Document> results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(LabelIndexField.ENTITY_GID.getName()).length);
            assertEquals(LABEL_ONE_GID, doc.getField(LabelIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(LabelIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(LabelIndexField.ENTITY_TYPE.getName()).stringValue());
        }
    }

    /**
     * Checks zeroes are removed from date
     *
     * @throws Exception
     */
    public void testIndexLabelWithBeginDate() throws Exception {

        addLabelOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        ir.close();
        
        // Try to search using this piece of information
        String query = LabelIndexField.BEGIN + ":\"1979\"";
        List<Document> results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(LabelIndexField.ENTITY_GID.getName()).length);
            assertEquals(LABEL_ONE_GID, doc.getField(LabelIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(LabelIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(LabelIndexField.ENTITY_TYPE.getName()).stringValue());
        }
    }
    
    /**
     * Checks record with begin date = null is not indexed
     *
     * @throws Exception
     */
    public void testIndexLabelWithNoBeginDate() throws Exception {

        addLabelThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();
    }

    /**
     * Checks zeroes are removed from date
     *
     * @throws Exception
     */
    public void testIndexLabelWithEndDate() throws Exception {

        addLabelTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();
        
        // Try to search using this piece of information
        String query = LabelIndexField.END + ":\"2009-04\"";
        List<Document> results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(LabelIndexField.ENTITY_GID.getName()).length);
            assertEquals(LABEL_TWO_GID, doc.getField(LabelIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(LabelIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(LabelIndexField.ENTITY_TYPE.getName()).stringValue());
        }
    }

    public void testIndexLabelWithType() throws Exception {

        addLabelOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();
        
        // Try to search using this piece of information
        String query = LabelIndexField.TYPE + ":\"Bootleg Production\"";
        List<Document> results = search(INDEX_FIELD_CLASS, ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(LabelIndexField.ENTITY_GID.getName()).length);
            assertEquals(LABEL_ONE_GID, doc.getField(LabelIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(LabelIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(LabelIndexField.ENTITY_TYPE.getName()).stringValue());
        }
    }

    /**
     * Checks record with no type is indexed
     *
     * @throws Exception
     */
    public void testIndexLabelWithNoType() throws Exception {

        addLabelThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();
    }

}