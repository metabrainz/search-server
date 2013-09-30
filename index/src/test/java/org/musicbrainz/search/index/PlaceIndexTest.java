package org.musicbrainz.search.index;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;
import java.sql.Statement;
import static org.junit.Assert.assertEquals;

public class PlaceIndexTest extends AbstractIndexTest {


    private void createIndex(RAMDirectory ramDir) throws Exception {
        IndexWriter writer = createIndexWriter(ramDir,PlaceIndexField.class);
        PlaceIndex ti = new PlaceIndex(conn);
        CommonTables ct = new CommonTables(conn, ti.getName());
        ct.createTemporaryTables(false);

        ti.init(writer, false);
        ti.addMetaInformation(writer);
        ti.indexData(writer, 0, Integer.MAX_VALUE);
        ti.destroy();
        writer.close();

    }

    private void addPlaceOne() throws Exception {

        Statement stmt = conn.createStatement();
        stmt.addBatch("INSERT INTO place (coordinates, id, gid,name,address,type, begin_date_year, end_date_year) VALUES ((180.56,120),1, 'aa95182f-df0a-3ad6-8bfb-4b63482cd276', 'Manor Studios','1 New Street',1,1830,2020)");
        stmt.addBatch("INSERT INTO place_type(id, name) VALUES (1, 'Studio')");
        stmt.addBatch("INSERT INTO place_alias (id, place, name, sort_name, primary_for_locale, locale, type ) VALUES (3, 1, 'Manox','Manoy', true, 'en',1)");
        stmt.executeBatch();
        stmt.close();
    }

    private void addPlaceTwo() throws Exception {

        Statement stmt = conn.createStatement();
        stmt.addBatch("INSERT INTO place (id, gid,name,address,type, begin_date_year, end_date_year) VALUES (1, 'aa95182f-df0a-3ad6-8bfb-4b63482cd276', 'Manor Studios','1 New Street',1,1830,2020)");
        stmt.addBatch("INSERT INTO place_type(id, name) VALUES (1, 'Studio')");
        stmt.addBatch("INSERT INTO place_alias (id, place, name, sort_name, primary_for_locale, locale, type ) VALUES (3, 1, 'Manox','Manoy', true, 'en',1)");
        stmt.executeBatch();
        stmt.close();
    }


    @Test
    public void testIndexPlaceName() throws Exception {

        addPlaceOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, PlaceIndexField.PLACE, "manor");

        }
        ir.close();
    }

    @Test
    public void testIndexPlaceAddress() throws Exception {

        addPlaceOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, PlaceIndexField.ADDRESS, "1");
            checkTermX(ir, PlaceIndexField.ADDRESS, "new", 1);
            checkTermX(ir, PlaceIndexField.ADDRESS, "street", 2);
        }
        ir.close();
    }

    @Test
    public void testIndexPlaceGuid() throws Exception {

        addPlaceOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, PlaceIndexField.PLACE_ID, "aa95182f-df0a-3ad6-8bfb-4b63482cd276");

        }
        ir.close();
    }

    @Test
    public void testIndexPlaceAlias() throws Exception {

        addPlaceOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, PlaceIndexField.ALIAS, "manox");
            checkTermX(ir, PlaceIndexField.ALIAS, "manoy", 1);

        }
        ir.close();
    }

    @Test
    public void testIndexPlaceLat() throws Exception {

        addPlaceOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, PlaceIndexField.LAT, 180.56f);

        }
        ir.close();
    }

    @Test
    public void testIndexPlaceLong() throws Exception {

        addPlaceOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, PlaceIndexField.LONG, 120f);

        }
        ir.close();
    }

    @Test
    public void testIndexPlaceBegin() throws Exception {

        addPlaceOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, PlaceIndexField.BEGIN, "1830");

        }
        ir.close();
    }

    @Test
    public void testIndexPlaceEnd() throws Exception {

        addPlaceOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, PlaceIndexField.END, "2020");

        }
        ir.close();
    }



    /*
    @Test
    public void testStoredIndexPlace() throws Exception {

        addPlaceOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {

            Document doc = ir.document(1);
            PlaceList arealist = (PlaceList) MMDSerializer.unserialize(doc.get(PlaceIndexField.PLACE_STORE.getName()), PlaceList.class);
            DefPlaceElementInner area = arealist.getPlace().get(0);
            assertEquals("aa95182f-df0a-3ad6-8bfb-4b63482cd276", area.getId());
            assertEquals("Afghanistan", area.getName());
            assertEquals("Afghanistan", area.getSortName());
            assertEquals("Country", area.getType());
            assertNotNull(area.getAliasList());
            assertEquals(1,area.getAliasList().getAlias().size());

            Alias alias =  area.getAliasList().getAlias().get(0);
            assertEquals("Afghany",alias.getContent());
            assertEquals("Afghan", alias.getSortName());
            assertEquals("en",alias.getLocale());
            assertEquals("AliasType",alias.getType());

            assertNotNull(area.getLifeSpan());
            LifeSpan lifeSpan = area.getLifeSpan();
            assertEquals("1830",lifeSpan.getBegin());
            assertEquals("2020",lifeSpan.getEnd());
            assertEquals("false",lifeSpan.getEnded());

            assertNotNull(area.getIso31661CodeList());
            assertEquals("AF", area.getIso31661CodeList().getIso31661Code().get(0));

            assertNotNull(area.getIso31662CodeList());
            assertEquals("North", area.getIso31662CodeList().getIso31662Code().get(0));
            assertEquals("West", area.getIso31662CodeList().getIso31662Code().get(1));

            assertNotNull(area.getIso31663CodeList());
            assertEquals("Kabu", area.getIso31663CodeList().getIso31663Code().get(0));


        }
        ir.close();
    }
      */

}