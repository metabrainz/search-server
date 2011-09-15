package org.musicbrainz.search.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.NumericUtils;

import java.sql.Statement;


public class LabelIndexTest extends AbstractIndexTest {

    public void setUp() throws Exception {
        super.setup();
    }

    private void createIndex(RAMDirectory ramDir) throws Exception {

        IndexWriter writer = createIndexWriter(ramDir,LabelIndexField.class);
        LabelIndex li = new LabelIndex(conn);
        CommonTables ct = new CommonTables(conn,  li.getName());
        ct.createTemporaryTables(false);
        li.init(writer, false);
        li.addMetaInformation(writer);
        li.indexData(writer, 0, Integer.MAX_VALUE);
        li.destroy();
        writer.close();
    }

    /**
     * Some fields populated has alias, but no country
     *
     * @throws Exception
     */
    private void addLabelOne() throws Exception {

        Statement stmt = conn.createStatement();

	stmt.addBatch("INSERT INTO label_name (id, name) VALUES (1, '4AD')");
	stmt.addBatch("INSERT INTO label_name (id, name) VALUES (2, '4AD US')");
		
        stmt.addBatch("INSERT INTO label (id, gid, name, sort_name, type, label_code, begin_date_year,ipi_code) " +
					"VALUES (1, 'a539bb1e-f2e1-4b45-9db8-8053841e7503', 1, 1, 4, 5807, 1979,'1001')");
        stmt.addBatch("INSERT INTO label_alias (label, name) VALUES (1, 2)");

        stmt.executeBatch();
        stmt.close();
    }


    /**
     * Most fields populated, but no alias
     *
     * @throws Exception
     */
    private void addLabelTwo() throws Exception {

        Statement stmt = conn.createStatement();
		
	stmt.addBatch("INSERT INTO country (id, iso_code, name) VALUES (38, 'CA','Canada')");
	stmt.addBatch("INSERT INTO label_name (id, name) VALUES (3, 'MusicBrainz Data Testing Label')");
	stmt.addBatch("INSERT INTO label_name (id, name) VALUES (4, 'Data Testing Label, MusicBrainz')");
		
        stmt.addBatch("INSERT INTO label(id, gid, name, sort_name, type, label_code, country, comment, " + 
					"	begin_date_year, begin_date_month, begin_date_day, end_date_year, end_date_month) " +
					"VALUES (2, 'd8caa692-704d-412b-a410-4fbcf5b9c796', 3, 4, 1, 0099998, 38, 'DO NOT EDIT THIS LABEL', " +
					"	2009, 1, 1, 2009, 4)");

        stmt.executeBatch();
        stmt.close();
    }

    /**
     * Some fields, no alias but has country
     *
     * @throws Exception
     */
    private void addLabelThree() throws Exception {

        Statement stmt = conn.createStatement();
		
		stmt.addBatch("INSERT INTO country (id, iso_code, name) VALUES (1, 'AF','Afghanistan')");
		stmt.addBatch("INSERT INTO label_name (id, name) VALUES (1, '4AD')");
		stmt.addBatch("INSERT INTO label_name (id, name) VALUES (2, '4AD US')");
		
        stmt.addBatch("INSERT INTO label (id, gid, name, sort_name, country)" +
					"VALUES (3, 'a539bb1e-f2e1-4b45-9db8-8053841e7503', 1, 1, 1)");
        stmt.addBatch("INSERT INTO label_alias (label, name) VALUES (3, 2)");

        stmt.addBatch("INSERT INTO tag (id, name, ref_count) VALUES (1, 'Goth', 2);");
        stmt.addBatch("INSERT INTO label_tag (label, tag, count) VALUES (3, 1, 10)");

        stmt.executeBatch();
        stmt.close();
    }

    public void testIndexLabel() throws Exception {

           addLabelThree();
           RAMDirectory ramDir = new RAMDirectory();
           createIndex(ramDir);

           IndexReader ir = IndexReader.open(ramDir, true);
           assertEquals(2, ir.numDocs());
           {
               Document doc = ir.document(1);
               assertEquals(1, doc.getFields(LabelIndexField.LABEL_ID.getName()).length);
               assertEquals("a539bb1e-f2e1-4b45-9db8-8053841e7503", doc.getField(LabelIndexField.LABEL_ID.getName()).stringValue());               
               assertEquals(1, doc.getFields(LabelIndexField.COUNTRY.getName()).length);
               assertEquals("af", doc.getField(LabelIndexField.COUNTRY.getName()).stringValue());
           }
           ir.close();

       }


    public void testIndexLabelWithNoCountry() throws Exception {

        addLabelOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(LabelIndexField.COUNTRY.getName()).length);
            assertEquals("unknown", doc.getField(LabelIndexField.COUNTRY.getName()).stringValue());
        }
        ir.close();

    }

    public void testIndexLabelWithCountry() throws Exception {

        addLabelThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(LabelIndexField.COUNTRY.getName()).length);
            assertEquals("af", doc.getField(LabelIndexField.COUNTRY.getName()).stringValue());
        }
        ir.close();

    }

    public void testIndexLabelWithIPI() throws Exception {

        addLabelOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(LabelIndexField.IPI.getName()).length);
            assertEquals("1001", doc.getField(LabelIndexField.IPI.getName()).stringValue());
        }
        ir.close();

    }

    public void testIndexLabelWithNoComment() throws Exception {

        addLabelOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(0, doc.getFields(LabelIndexField.COMMENT.getName()).length);
        }
        ir.close();
    }

    public void testIndexLabelWithComment() throws Exception {

        addLabelTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
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
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(0, doc.getFields(LabelIndexField.CODE.getName()).length);
        }
        ir.close();
    }


    public void testIndexLabelWithLabelCode() throws Exception {

        addLabelOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(LabelIndexField.CODE.getName()).length);
            assertEquals(5807, NumericUtils.prefixCodedToInt(doc.getField(LabelIndexField.CODE.getName()).stringValue()));
        }
        ir.close();
    }

    public void testIndexLabelWithLabelCodeWithZeroes() throws Exception {

        addLabelTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(LabelIndexField.CODE.getName()).length);
            assertEquals(99998, NumericUtils.prefixCodedToInt(doc.getField(LabelIndexField.CODE.getName()).stringValue()));
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
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(LabelIndexField.ALIAS.getName()).length); 
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
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
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
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
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
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
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
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(0, doc.getFields(LabelIndexField.END.getName()).length);
        }
        ir.close();
    }


    public void testIndexLabelWithType() throws Exception {

        addLabelOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(LabelIndexField.TYPE.getName()).length);
            assertEquals("Original Production", doc.getField(LabelIndexField.TYPE.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * Checks fields with different sort name to name is indexed correctly
     *
     * @throws Exception
     */
    public void testIndexLabelWithTag() throws Exception {

        addLabelThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(LabelIndexField.LABEL.getName()).length);
            assertEquals(1, doc.getFields(LabelIndexField.TAG.getName()).length);
            assertEquals("Goth", doc.getField(LabelIndexField.TAG.getName()).stringValue());
            assertEquals(1, doc.getFields(LabelIndexField.TAGCOUNT.getName()).length);
            assertEquals("10", doc.getField(LabelIndexField.TAGCOUNT.getName()).stringValue());
        }
        ir.close();
    }


//    /**
//     * Checks record with type = null is set to unknown
//     *
//     * @throws Exception
//     */
//    public void testIndexLabelWithNoType() throws Exception {
//
//        addLabelThree();
//        RAMDirectory ramDir = new RAMDirectory();
//        createIndex(ramDir);
//
//        IndexReader ir = IndexReader.open(ramDir, true);
//        assertEquals(2, ir.numDocs());
//        {
//            Document doc = ir.document(1);
//            assertEquals(1, doc.getFields(LabelIndexField.TYPE.getName()).length);
//            assertEquals("unknown", doc.getField(LabelIndexField.TYPE.getName()).stringValue());
//        }
//        ir.close();
//    }

}