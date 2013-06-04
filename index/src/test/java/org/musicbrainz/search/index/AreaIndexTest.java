package org.musicbrainz.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;
import org.musicbrainz.mmd2.AreaList;
import org.musicbrainz.mmd2.DefAreaElementInner;

import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AreaIndexTest extends AbstractIndexTest {


    private void createIndex(RAMDirectory ramDir) throws Exception {
        IndexWriter writer = createIndexWriter(ramDir,AreaIndexField.class);
        AreaIndex ti = new AreaIndex(conn);
        CommonTables ct = new CommonTables(conn, ti.getName());
        ct.createTemporaryTables(false);

        ti.init(writer, false);
        ti.addMetaInformation(writer);
        ti.indexData(writer, 0, Integer.MAX_VALUE);
        ti.destroy();
        writer.close();

    }

    private void addAreaOne() throws Exception {

        Statement stmt = conn.createStatement();
        stmt.addBatch("INSERT INTO area (id, gid,name,sort_name, type) VALUES (1, 'aa95182f-df0a-3ad6-8bfb-4b63482cd276', 'Afghanistan','Afghanistan', 1)");
        stmt.addBatch("INSERT INTO area_type(id, name) VALUES (1, 'Country')");
        stmt.executeBatch();
        stmt.close();
    }

    @Test
    public void testIndexAreaName() throws Exception {

        addAreaOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, AreaIndexField.AREA, "afghanistan");

        }
        ir.close();
    }

    @Test
    public void testIndexAreaSortName() throws Exception {

        addAreaOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, AreaIndexField.SORTNAME, "afghanistan");

        }
        ir.close();
    }

    @Test
    public void testIndexAreaGuid() throws Exception {

        addAreaOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, AreaIndexField.AREA_ID, "aa95182f-df0a-3ad6-8bfb-4b63482cd276");

        }
        ir.close();
    }


    @Test
    public void testIndexArea() throws Exception {

        addAreaOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {

            Document doc = ir.document(1);
            AreaList arealist = (AreaList) MMDSerializer.unserialize(doc.get(AreaIndexField.AREA_STORE.getName()), AreaList.class);
            org.musicbrainz.mmd2.DefAreaElementInner area = arealist.getArea().get(0);
            assertEquals("aa95182f-df0a-3ad6-8bfb-4b63482cd276", area.getId());
            assertEquals("Afghanistan", area.getName());
            assertEquals("Afghanistan", area.getSortName());
            assertEquals("Country", area.getType());
        }
        ir.close();
    }


}