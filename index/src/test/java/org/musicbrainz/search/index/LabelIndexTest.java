package org.musicbrainz.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.search.analysis.StandardUnaccentAnalyzer;
import org.musicbrainz.search.index.LabelIndex;
import org.musicbrainz.search.index.LabelIndexField;

import java.sql.Connection;
import java.sql.Statement;


public class LabelIndexTest extends AbstractIndexTest {


    public void setUp() throws Exception {
        super.setup();
    }

    private void createIndex(RAMDirectory ramDir) throws Exception {
        IndexWriter writer = new IndexWriter(ramDir, new StandardUnaccentAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);
        LabelIndex li = new LabelIndex(createConnection());
        li.indexData(writer, 0, Integer.MAX_VALUE);
        writer.close();

    }


    /**
     * Some fields populated has alias,but no country
     *
     * @throws Exception
     */
    private void addLabelOne() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO labelalias(id, ref, name, timesused, modpending, lastused)" +
                " VALUES (1327, 563, '4AD US', 0, 0, '1970-01-01 01:00:00')");
        stmt.addBatch("INSERT INTO label(id,name, gid, modpending, labelcode,sortname,country, page, resolution, begindate,enddate,type)" +
                "    VALUES (563, '4AD', 'a539bb1e-f2e1-4b45-9db8-8053841e7503',0,5807,'4AD',null,2260992,null, '1979-00-00', null,4)");

        stmt.executeBatch();
        stmt.close();
        conn.close();
    }


    /**
     * Most field populated, but no alias
     *
     * @throws Exception
     */
    private void addLabelTwo() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();
        stmt.addBatch("INSERT INTO country(id, isocode, name)" +
                " VALUES (38, 'CA','Canada')");
        stmt.addBatch("INSERT INTO label(id,name, gid, modpending, labelcode,sortname,country, page, resolution, begindate,enddate,type)" +
                "    VALUES (35269, 'MusicBrainz Data Testing Label', 'd8caa692-704d-412b-a410-4fbcf5b9c796',0,0099998,'Data Testing Label, MusicBrainz'," +
                "38,170559541,'DO NOT EDIT THIS LABEL', '2009-01-01', '2009-04-00',7)");

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
        stmt.addBatch("INSERT INTO country(id, isocode, name)" +
                " VALUES (1, 'AF','Afghanistan')");

        stmt.addBatch("INSERT INTO labelalias(id, ref, name, timesused, modpending, lastused)" +
                " VALUES (1327, 563, '4AD US', 0, 0, '1970-01-01 01:00:00')");
        stmt.addBatch("INSERT INTO label(id,name, gid, modpending, labelcode,sortname,country, page, resolution, begindate,enddate,type)" +
                "    VALUES (563, '4AD', 'a539bb1e-f2e1-4b45-9db8-8053841e7503',0,null,'4AD',1,2260992,null, null, null,null)");

        stmt.executeBatch();
        stmt.close();
        conn.close();
    }


    public void testIndexLabelWithNoCountry() throws Exception {

        addLabelOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(0, doc.getFields(LabelIndexField.COUNTRY.getName()).length);
        }
        ir.close();

    }

    public void testIndexLabelWithCountry() throws Exception {

        addLabelThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(LabelIndexField.COUNTRY.getName()).length);
            assertEquals("af", doc.getField(LabelIndexField.COUNTRY.getName()).stringValue());
        }
        ir.close();

    }


    public void testIndexLabelWithNoComment() throws Exception {

        addLabelOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(0, doc.getFields(LabelIndexField.COMMENT.getName()).length);
        }
        ir.close();
    }

    public void testIndexLabelWithComment() throws Exception {

        addLabelTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(LabelIndexField.COMMENT.getName()).length);
            assertEquals("DO NOT EDIT THIS LABEL", doc.getField(LabelIndexField.COMMENT.getName()).stringValue());
        }
        ir.close();
    }


    public void testIndexLabelWithNoLabelCode() throws Exception {

        addLabelThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(0, doc.getFields(LabelIndexField.CODE.getName()).length);
        }
        ir.close();
    }


    public void testIndexLabelWithLabelCode() throws Exception {

        addLabelOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(LabelIndexField.CODE.getName()).length);
            assertEquals("5807", doc.getField(LabelIndexField.CODE.getName()).stringValue());
        }
        ir.close();
    }

    public void testIndexLabelWithLabelCodeWithZeroes() throws Exception {

        addLabelTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(LabelIndexField.CODE.getName()).length);
            assertEquals("99998", doc.getField(LabelIndexField.CODE.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * Checks fields are indexed correctly for label with alias (the aliases are not stored)
     *
     * @throws Exception
     */
    public void testIndexLabelWithAlias() throws Exception {

        addLabelOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(0, doc.getFields(LabelIndexField.ALIAS.getName()).length); //aliases are searchable but not stored
        }
        ir.close();
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
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(LabelIndexField.BEGIN.getName()).length);
            assertEquals("1979", doc.getField(LabelIndexField.BEGIN.getName()).stringValue());
        }
        ir.close();
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
        {
            Document doc = ir.document(0);
            assertEquals(0, doc.getFields(LabelIndexField.BEGIN.getName()).length);
        }
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
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(LabelIndexField.END.getName()).length);
            assertEquals("2009-04", doc.getField(LabelIndexField.END.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * Checks record with end date = null is not indexed
     *
     * @throws Exception
     */
    public void testIndexLabelWithNoEndDate() throws Exception {

        addLabelThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(0, doc.getFields(LabelIndexField.END.getName()).length);
        }
        ir.close();
    }


    public void testIndexLabelWithType() throws Exception {

        addLabelOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(LabelIndexField.TYPE.getName()).length);
            assertEquals("orig. prod.", doc.getField(LabelIndexField.TYPE.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * Checks record with type = null is set to unknown
     *
     * @throws Exception
     */
    public void testIndexLabelWithNoType() throws Exception {

        addLabelThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(LabelIndexField.TYPE.getName()).length);
            assertEquals("unknown", doc.getField(LabelIndexField.TYPE.getName()).stringValue());
        }
        ir.close();
    }

    public void testGetTypeByDbId () throws Exception {        
        assertEquals(LabelType.DISTRIBUTOR,LabelType.getByDbId(1));
    }

}