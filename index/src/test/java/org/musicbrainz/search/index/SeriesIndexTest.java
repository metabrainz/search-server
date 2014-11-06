package org.musicbrainz.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;
import org.musicbrainz.mmd2.Series;

import java.sql.Statement;

import static org.junit.Assert.assertEquals;

public class SeriesIndexTest extends AbstractIndexTest {


    private void createIndex(RAMDirectory ramDir) throws Exception {
        IndexWriter writer = createIndexWriter(ramDir,SeriesIndexField.class);
        SeriesIndex ti = new SeriesIndex(conn);
        CommonTables ct = new CommonTables(conn, ti.getName());
        ct.createTemporaryTables(false);

        ti.init(writer, false);
        ti.addMetaInformation(writer);
        ti.indexData(writer, 0, Integer.MAX_VALUE);
        ti.destroy();
        writer.close();

    }

    private void addSeriesOne() throws Exception {

        Statement stmt = conn.createStatement();
        stmt.addBatch("INSERT INTO link_type(id,name) VALUES (1, 'composer')");
        stmt.addBatch("INSERT INTO link_attribute(link, attribute_type, created) VALUES (1, 1, null)");
        stmt.addBatch("INSERT INTO link_attribute_type( id, parent, root, child_order, gid, name, description, last_updated) " +
                "VALUES (1, 1, 1, 1, 'ccd4879c-5e88-4385-b131-bf65296bf245', 'additional', null,null);");
        stmt.addBatch("INSERT INTO series (comment, id, gid,name,type,ordering_attribute) VALUES ('comment',1, 'aa95182f-df0a-3ad6-8bfb-4b63482cd276', 'Trumpet',1,1)");
        stmt.addBatch("INSERT INTO series_type(id, name) VALUES (1, 'Brass')");
        stmt.addBatch("INSERT INTO series_alias (id, name, sort_name, series, primary_for_locale, locale, type ) VALUES (3,  'tromba','tromba sort', 1, true, 'it',1)");

        stmt.addBatch("INSERT INTO tag (id, name, ref_count) VALUES (1, 'Groovy', 2);");
        stmt.addBatch("INSERT INTO series_tag (series, tag, count) VALUES (1, 1, 10)");
        stmt.executeBatch();
        stmt.close();
    }



    @Test
    public void testIndexSeriesName() throws Exception {

        addSeriesOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, SeriesIndexField.SERIES, "trumpet");

        }
        ir.close();
    }

    @Test
    public void testIndexSeriesGuid() throws Exception {

        addSeriesOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, SeriesIndexField.SERIES_ID, "aa95182f-df0a-3ad6-8bfb-4b63482cd276");

        }
        ir.close();
    }

    @Test
    public void testIndexSeriesAlias() throws Exception {

        addSeriesOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, SeriesIndexField.ALIAS, "sort");
            checkTermX(ir, SeriesIndexField.ALIAS, "tromba", 1);

        }
        ir.close();
    }


    @Test
    public void testIndexSeriesComment() throws Exception {

        addSeriesOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, SeriesIndexField.COMMENT, "comment");
        }
        ir.close();
    }


    @Test
    public void testIndexSeriesOrderingAttribute() throws Exception {

        addSeriesOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, SeriesIndexField.ORDERING_ATTRIBUTE, "additional");
        }
        ir.close();
    }

    @Test
    public void testStoredIndexSeries() throws Exception {

        addSeriesOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {

            Document doc = ir.document(1);
            Series series = (Series) MMDSerializer.unserialize(doc.get(SeriesIndexField.SERIES_STORE.getName()), Series.class);
            assertEquals("aa95182f-df0a-3ad6-8bfb-4b63482cd276", series.getId());
            assertEquals("Trumpet", series.getName());

            assertEquals("comment",series.getDisambiguation());
            assertEquals("Brass", series.getType());
            assertEquals("tromba",series.getAliasList().getAlias().get(0).getContent());
            assertEquals("tromba sort",series.getAliasList().getAlias().get(0).getSortName());
            assertEquals("it",series.getAliasList().getAlias().get(0).getLocale());

        }
        ir.close();
    }


    /**
     * Checks fields with different sort name to name is indexed correctly
     *
     * @throws Exception
     */
    @Test
    public void testIndexInstrumentWithTag() throws Exception {

        addSeriesOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, SeriesIndexField.TAG, "groovy");
        }
        ir.close();
    }
}