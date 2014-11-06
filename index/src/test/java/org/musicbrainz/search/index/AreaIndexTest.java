package org.musicbrainz.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;
import org.musicbrainz.mmd2.*;

import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
        stmt.addBatch("INSERT INTO area (id, gid,name, comment, type, begin_date_year, end_date_year) VALUES (1, 'aa95182f-df0a-3ad6-8bfb-4b63482cd276', 'Afghanistan','A Country in Asia',1,1830,2020)");
        stmt.addBatch("INSERT INTO area_type(id, name) VALUES (1, 'Country')");
        stmt.addBatch("INSERT INTO area_type(id, name) VALUES (2, 'Subdivision')");
        stmt.addBatch("INSERT INTO area_alias (id, area, sort_name, name, primary_for_locale, locale, type ) VALUES (3, 1, 'Afghan', 'Afghany', true, 'en',1)");
        stmt.addBatch("INSERT INTO iso_3166_1(area, code) VALUES (1,'AF')");
        stmt.addBatch("INSERT INTO iso_3166_2(area, code) VALUES (1,'North')");
        stmt.addBatch("INSERT INTO iso_3166_2(area, code) VALUES (1,'West')");
        stmt.addBatch("INSERT INTO iso_3166_3(area, code) VALUES (1,'Kabu')");
        stmt.addBatch("INSERT INTO tag (id, name, ref_count) VALUES (1, 'Groovy', 2);");
        stmt.addBatch("INSERT INTO area_tag (area, tag, count) VALUES (1, 1, 10)");
        stmt.executeBatch();
        stmt.close();
    }

    private void addAreaTwo() throws Exception {

        Statement stmt = conn.createStatement();
        stmt.addBatch("INSERT INTO area_type(id, name) VALUES (1, 'Country')");
        stmt.addBatch("INSERT INTO area_type(id, name) VALUES (2, 'Subdivision')");
        stmt.addBatch("INSERT INTO link (id, link_type) values (1,10)");
        stmt.addBatch("INSERT INTO link_type (id, name, gid) values (10,'part of','bb95182f-df0a-3ad6-8bfb-4b63482cd276')");


        stmt.addBatch("INSERT INTO area (id, gid,name, comment, type, begin_date_year, end_date_year) VALUES (1, 'aa95182f-df0a-3ad6-8bfb-4b63482cd276', 'San Francisco','San Francisco, Argentina',2,1830,2020)");
        stmt.addBatch("INSERT INTO area_alias (id, area, sort_name, name, primary_for_locale, locale, type ) VALUES (3, 1, 'San Francisco', 'San Francisco', true, 'en',1)");
        stmt.addBatch("INSERT INTO iso_3166_1(area, code) VALUES (1,'AR')");

        stmt.addBatch("INSERT INTO area (id, gid,name, comment, type, begin_date_year, end_date_year) VALUES (2, 'aa95182f-df0a-3ad6-8bfb-4b63482cd400', 'Argentina','Argentina',1,1330,2020)");

        stmt.addBatch("INSERT INTO l_area_area (id, link, entity0, entity1) values (1,1,2,1)");

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
    public void testIndexAreaComment() throws Exception {

        addAreaOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, AreaIndexField.COMMENT, "a");
            checkTermX(ir, AreaIndexField.COMMENT, "asia",1);
            checkTermX(ir, AreaIndexField.COMMENT, "country",2);
            checkTermX(ir, AreaIndexField.COMMENT, "in",3);
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
    public void testIndexAreaAlias() throws Exception {

        addAreaOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, AreaIndexField.ALIAS, "afghan");

        }
        ir.close();
    }

    @Test
    public void testIndexAreaBegin() throws Exception {

        addAreaOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, AreaIndexField.BEGIN, "1830");

        }
        ir.close();
    }

    @Test
    public void testIndexAreaEnd() throws Exception {

        addAreaOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, AreaIndexField.END, "2020");

        }
        ir.close();
    }

    @Test
    public void testIndexAreaIso1() throws Exception {

        addAreaOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, AreaIndexField.ISO1, "af");

        }
        ir.close();
    }

    @Test
    public void testIndexAreaIso2() throws Exception {

        addAreaOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, AreaIndexField.ISO2, "north");

        }
        ir.close();
    }

    @Test
    public void testIndexAreaIso3() throws Exception {

        addAreaOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, AreaIndexField.ISO3, "kabu");

        }
        ir.close();
    }

    @Test
    public void testIndexAreaIso() throws Exception {

        addAreaOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, AreaIndexField.ISO, "af");

        }
        ir.close();
    }

    @Test
    public void testStoredIndexArea() throws Exception {

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
            assertEquals("A Country in Asia", area.getDisambiguation());
            assertEquals("Country", area.getType());
            assertNotNull(area.getAliasList());
            assertEquals(1,area.getAliasList().getAlias().size());

            Alias alias =  area.getAliasList().getAlias().get(0);
            assertEquals("Afghany",alias.getContent());
            assertEquals("Afghan", alias.getSortName());
            assertEquals("en",alias.getLocale());
            assertEquals("AliasType",alias.getType());

            assertNotNull(area.getTagList());
            assertEquals(1,area.getTagList().getTag().size());
            Tag tag =  area.getTagList().getTag().get(0);
            assertEquals("Groovy",tag.getName());

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

    @Test
    public void testIndexAreaParent() throws Exception {

        addAreaTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(3, ir.numDocs());
        {
            checkTerm(ir, AreaIndexField.AREA, "argentina");

        }
        ir.close();
    }

    @Test
    public void testStoredIndexAreaWithParent() throws Exception {

        addAreaTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(3, ir.numDocs());
        {

            Document doc = ir.document(1);
            AreaList arealist = (AreaList) MMDSerializer.unserialize(doc.get(AreaIndexField.AREA_STORE.getName()), AreaList.class);
            org.musicbrainz.mmd2.DefAreaElementInner area = arealist.getArea().get(0);
            assertEquals("aa95182f-df0a-3ad6-8bfb-4b63482cd276", area.getId());
            assertEquals("San Francisco", area.getName());
            assertEquals("San Francisco", area.getSortName());
            assertEquals("San Francisco, Argentina", area.getDisambiguation());
            assertEquals("Subdivision", area.getType());
            assertNotNull(area.getAliasList());
            assertEquals(1,area.getAliasList().getAlias().size());

            Alias alias =  area.getAliasList().getAlias().get(0);
            assertEquals("San Francisco",alias.getContent());
            assertEquals("San Francisco", alias.getSortName());
            assertEquals("en",alias.getLocale());
            assertEquals("AliasType",alias.getType());

            assertNotNull(area.getLifeSpan());
            LifeSpan lifeSpan = area.getLifeSpan();
            assertEquals("1830",lifeSpan.getBegin());
            assertEquals("2020",lifeSpan.getEnd());
            assertEquals("false",lifeSpan.getEnded());

            assertNotNull(area.getIso31661CodeList());
            assertEquals("AR", area.getIso31661CodeList().getIso31661Code().get(0));

            assertNotNull(area.getRelationList());
            assertNotNull(area.getRelationList().get(0));
            assertNotNull(area.getRelationList().get(0).getRelation());
            assertNotNull(area.getRelationList().get(0).getRelation().get(0));
            assertEquals("area",area.getRelationList().get(0).getTargetType());
            Relation relation = area.getRelationList().get(0).getRelation().get(0);
            assertEquals("bb95182f-df0a-3ad6-8bfb-4b63482cd276",relation.getTypeId());
            assertEquals("part of",relation.getType());
            assertEquals("aa95182f-df0a-3ad6-8bfb-4b63482cd400",relation.getTarget().getValue());
            assertEquals("backward",relation.getDirection().value());
            assertEquals("aa95182f-df0a-3ad6-8bfb-4b63482cd400", relation.getArea().getId());
            assertEquals("Argentina", relation.getArea().getName());
            assertEquals("Argentina", relation.getArea().getSortName());

            //The parent in its own right
            doc = ir.document(2);
            arealist = (AreaList) MMDSerializer.unserialize(doc.get(AreaIndexField.AREA_STORE.getName()), AreaList.class);
            area = arealist.getArea().get(0);
            assertEquals("aa95182f-df0a-3ad6-8bfb-4b63482cd400", area.getId());
            assertEquals("Argentina", area.getName());
            assertEquals("Argentina", area.getSortName());
            assertEquals("Argentina", area.getDisambiguation());
            assertEquals("Country", area.getType());

        }
        ir.close();
    }


    /**
     * Checks fields with different sort name to name is indexed correctly
     *
     * @throws Exception
     */
    @Test
    public void testIndexAreaWithTag() throws Exception {

        addAreaOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, AreaIndexField.TAG, "groovy");
        }
        ir.close();
    }
}