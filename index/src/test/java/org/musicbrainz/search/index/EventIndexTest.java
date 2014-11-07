package org.musicbrainz.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;
import org.musicbrainz.mmd2.Event;
import org.musicbrainz.mmd2.RelationList;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class EventIndexTest extends AbstractIndexTest {


    private void createIndex(RAMDirectory ramDir) throws Exception {
        IndexWriter writer = createIndexWriter(ramDir,EventIndexField.class);
        EventIndex ti = new EventIndex(conn);
        CommonTables ct = new CommonTables(conn, ti.getName());
        ct.createTemporaryTables(false);

        ti.init(writer, false);
        ti.addMetaInformation(writer);
        ti.indexData(writer, 0, Integer.MAX_VALUE);
        ti.destroy();
        writer.close();

    }

    private void addEventOne() throws Exception {

        Statement stmt = conn.createStatement();
        stmt.addBatch("INSERT INTO area (id, gid, name) VALUES (38, 'b8caa692-704d-412b-a410-4fbcf5b9c796','County of OxfordShire')");
        stmt.addBatch("INSERT INTO event (comment,id, gid,name,time,type, begin_date_year, end_date_year) VALUES ('comment',1, 'aa95182f-df0a-3ad6-8bfb-4b63482cd276','Manor Studios','07:30:00',1,1830,2020)");
        stmt.addBatch("INSERT INTO event_type(id, name) VALUES (1, 'Studio')");
        stmt.addBatch("INSERT INTO event_alias (id, event, name, sort_name, primary_for_locale, locale, type ) VALUES (3, 1, 'Manox','Manoy', true, 'en',1)");

        stmt.addBatch("INSERT INTO tag (id, name, ref_count) VALUES (1, 'Groovy', 2);");
        stmt.addBatch("INSERT INTO event_tag (event, tag, count) VALUES (1, 1, 10)");

        stmt.addBatch("INSERT INTO artist (id, gid, name, sort_name, comment)" +
                " VALUES (1, 'ccd4879c-5e88-4385-b131-bf65296bf245', 'Blur', 'Blur', 'a comment')");

        stmt.addBatch("INSERT INTO l_artist_event(id, link, entity0, entity1) VALUES (1, 1, 1, 1)");
        stmt.addBatch("INSERT INTO link(id, link_type)VALUES (1, 1)");
        stmt.addBatch("INSERT INTO link_type(id,name) VALUES (1, 'composer')");


        stmt.addBatch("INSERT INTO place (comment,coordinates, id, gid,name,address,type, begin_date_year, end_date_year,area) VALUES ('comment',(180.56,120),1, 'aa95182f-df0a-3ad6-8bfb-4b63482cd276', 'Manor Studios','1 New Street',1,1830,2020,38)");
        stmt.addBatch("INSERT INTO l_event_place(id, link, entity0, entity1) VALUES (2, 2, 1, 1)");
        stmt.addBatch("INSERT INTO link(id, link_type)VALUES (2, 2)");
        stmt.addBatch("INSERT INTO link_type(id,name) VALUES (2, 'town')");

        stmt.executeBatch();
        stmt.close();
    }



    @Test
    public void testIndexEventName() throws Exception {

        addEventOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, EventIndexField.EVENT, "manor");

        }
        ir.close();
    }



    @Test
    public void testIndexEventGuid() throws Exception {

        addEventOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, EventIndexField.EVENT_ID, "aa95182f-df0a-3ad6-8bfb-4b63482cd276");

        }
        ir.close();
    }

    @Test
    public void testIndexEventAlias() throws Exception {

        addEventOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, EventIndexField.ALIAS, "manox");
            checkTermX(ir, EventIndexField.ALIAS, "manoy", 1);

        }
        ir.close();
    }

    @Test
    public void testIndexEventBegin() throws Exception {

        addEventOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, EventIndexField.BEGIN, "1830");

        }
        ir.close();
    }

    @Test
    public void testIndexEventEnd() throws Exception {

        addEventOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, EventIndexField.END, "2020");

        }
        ir.close();
    }



    @Test
    public void testIndexEventComment() throws Exception {

        addEventOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, EventIndexField.COMMENT, "comment");
        }
        ir.close();
    }

    /**
     * Checks fields with different sort name to name is indexed correctly
     *
     * @throws Exception
     */
    @Test
    public void testIndexEventWithTag() throws Exception {

        addEventOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, EventIndexField.TAG, "groovy");
        }
        ir.close();
    }



    @Test
    public void testIndexWithArtistRelationWithNoAttributes() throws Exception {

        addEventOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);
        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            Event event = (Event) MMDSerializer
                    .unserialize(doc.get(EventIndexField.EVENT_STORE.getName()), Event.class);
            System.out.println(doc.get(EventIndexField.EVENT_STORE.getName()));


            RelationList artistList = event.getRelationList().get(0);
            assertNotNull(artistList);
            assertEquals("artist", artistList.getTargetType());
            assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", event.getRelationList().get(0).getRelation().get(0).getArtist().getId());
            assertEquals("Blur", event.getRelationList().get(0).getRelation().get(0).getArtist().getName());
            assertEquals("composer", event.getRelationList().get(0).getRelation().get(0).getType());
            assertNull(event.getRelationList().get(0).getRelation().get(0).getAttributeList());
            ir.close();
        }
    }

    @Test
    public void testIndexWithAPlaceRelationWithNoAttributes() throws Exception {

        addEventOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);
        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            Event event = (Event) MMDSerializer
                    .unserialize(doc.get(EventIndexField.EVENT_STORE.getName()), Event.class);

            RelationList placeList = event.getRelationList().get(1);
            assertNotNull(placeList);
            assertEquals("place", placeList.getTargetType());
            assertEquals("aa95182f-df0a-3ad6-8bfb-4b63482cd276", placeList.getRelation().get(0).getPlace().getId());
            assertEquals("Manor Studios", placeList.getRelation().get(0).getPlace().getName());
            assertEquals("town", placeList.getRelation().get(0).getType());
            assertNull(placeList.getRelation().get(0).getAttributeList());
            ir.close();
        }
    }
    @Test
    public void testStoredIndexEvent() throws Exception {

        addEventOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {

            Document doc = ir.document(1);
            Event event = (Event) MMDSerializer.unserialize(doc.get(EventIndexField.EVENT_STORE.getName()), Event.class);
            assertEquals("aa95182f-df0a-3ad6-8bfb-4b63482cd276", event.getId());
            assertEquals("Manor Studios", event.getName());

            assertEquals("comment",event.getDisambiguation());
            assertEquals("Studio", event.getType());
            assertEquals("Manox",event.getAliasList().getAlias().get(0).getContent());
            assertEquals("Manoy",event.getAliasList().getAlias().get(0).getSortName());
            assertEquals("en",event.getAliasList().getAlias().get(0).getLocale());
            assertEquals("Groovy",event.getTagList().getTag().get(0).getName());
            assertEquals("07:30:00",event.getTime());
        }
        ir.close();
    }

}