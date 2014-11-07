package org.musicbrainz.search.servlet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Test;
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.MusicbrainzSimilarity;
import org.musicbrainz.search.index.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Assumes an index has been built stored and in the data folder, I've picked a fairly obscure bside so hopefully
 * will not get added to another release
 */
public class FindEventTest
{

    private AbstractSearchServer ss;
    private AbstractDismaxSearchServer sd;


    @Before
    public void setUp() throws Exception {
        ObjectFactory of = new ObjectFactory();
        RAMDirectory ramDir = new RAMDirectory();
        Analyzer analyzer = DatabaseIndex.getAnalyzer(EventIndexField.class);
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION, analyzer);
        writerConfig.setSimilarity(new MusicbrainzSimilarity());
        IndexWriter writer = new IndexWriter(ramDir, writerConfig);

        {
            MbDocument doc = new MbDocument();

            Event event = of.createEvent();
            doc.addField(EventIndexField.EVENT_ID, "ff571ff4-04cb-4b9c-8a1c-354c330f863c");
            event.setId("ff571ff4-04cb-4b9c-8a1c-354c330f863c");

            doc.addField(EventIndexField.EVENT, "Afghanistan");
            event.setName("Afghanistan");

            doc.addField(EventIndexField.ALIAS, "Afghany");
            AliasList aliasList = of.createAliasList();
            Alias alias = of.createAlias();
            aliasList.getAlias().add(alias);
            alias.setContent("Afghany");
            alias.setSortName("Afghan");
            event.setAliasList(aliasList);

            doc.addField(EventIndexField.BEGIN, "1993");
            doc.addField(EventIndexField.END, "2004");
            org.musicbrainz.mmd2.Event.LifeSpan lifespan = of.createEventLifeSpan();
            event.setLifeSpan(lifespan);
            lifespan.setBegin("1993");
            lifespan.setEnd("2004");

            doc.addField(EventIndexField.TYPE,"Country");
            event.setType("Country");

            doc.addField(EventIndexField.COMMENT,"A comment");
            event.setDisambiguation("A comment");

           

            doc.addField(LabelIndexField.TAG, "desert");
            TagList tagList = of.createTagList();
            Tag tag = of.createTag();
            tag.setName("desert");
            tag.setCount(BigInteger.valueOf(22));
            tagList.getTag().add(tag);
            event.setTagList(tagList);

            {
                RelationList rl = of.createRelationList();
                rl.setTargetType("artist");
                {
                    Relation relation = of.createRelation();
                    Relation.AttributeList al = of.createRelationAttributeList();
                    Artist artist1 = of.createArtist();
                    artist1.setId("1f9df192-a621-4f54-8850-2c5373b7eac9");
                    artist1.setName("Пётр Ильич Чайковский");
                    artist1.setSortName("Пётр Ильич Чайковский");
                    relation.setArtist(artist1);
                    relation.setType("composer");
                    relation.setDirection(DefDirection.BACKWARD);
                    Relation.AttributeList.Attribute attribute = of.createRelationAttributeListAttribute();
                    attribute.setContent("additional");
                    al.getAttribute().add(attribute);
                    relation.setAttributeList(al);
                    rl.getRelation().add(relation);
                }
                event.getRelationList().add(rl);
                doc.addField(EventIndexField.ARTIST_ID, "1f9df192-a621-4f54-8850-2c5373b7eac9");
                doc.addField(EventIndexField.ARTIST, "Пётр Ильич Чайковский");
            }

            {
                RelationList rl = of.createRelationList();
                rl.setTargetType("place");
                {
                    Relation relation = of.createRelation();
                    Relation.AttributeList al = of.createRelationAttributeList();
                    Place place1 = of.createPlace();
                    place1.setId("abcdef");
                    place1.setName("netherbury");
                    relation.setPlace(place1);
                    relation.setType("village");
                    relation.setDirection(DefDirection.BACKWARD);
                    rl.getRelation().add(relation);
                }
                event.getRelationList().add(rl);
                doc.addField(EventIndexField.PLACE_ID, "abcdef");
                doc.addField(EventIndexField.PLACE, "netherbury");
            }


            doc.addField(EventIndexField.EVENT_STORE, MMDSerializer.serialize(event));
            writer.addDocument(doc.getLuceneDocument());
        }

        Event event = of.createEvent();
        MbDocument doc = new MbDocument();

        {
            EventList eventList = of.createEventList();
            eventList.getEvent().add(event);
            doc.addField(MetaIndexField.META, MetaIndexField.META_VALUE);
            doc.addNumericField(MetaIndexField.LAST_UPDATED, new Date().getTime());
            doc.addField(EventIndexField.EVENT_STORE, MMDSerializer.serialize(eventList));
            writer.addDocument(doc.getLuceneDocument());
        }


        writer.close();
        SearcherManager searcherManager = new SearcherManager(ramDir, new MusicBrainzSearcherFactory(ResourceType.EVENT));
        ss = new EventSearch(searcherManager);
        sd = new EventDismaxSearch(ss);
    }

    @Test
    public void testFindEventById() throws Exception {
        Results res = ss.search("eid:\"ff571ff4-04cb-4b9c-8a1c-354c330f863c\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(EventIndexField.EVENT_ID));
        assertEquals("Afghanistan", doc.get(EventIndexField.EVENT));
    }

    @Test
    public void testFindEventByName() throws Exception {
        Results res = ss.search("event:\"Afghanistan\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(EventIndexField.EVENT_ID));
        assertEquals("Afghanistan", doc.get(EventIndexField.EVENT));
    }



    @Test
    public void testFindEventByDefaultName() throws Exception {
        Results res = ss.search("\"Afghanistan\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(EventIndexField.EVENT_ID));
        assertEquals("Afghanistan", doc.get(EventIndexField.EVENT));
    }

/*
    @Test
    public void testFindEventByDefaultAlias() throws Exception {
        Results res = ss.search("\"Afghany\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(EventIndexField.EVENT_ID));
        assertEquals("Afghanistan", doc.get(EventIndexField.EVENT));
    }
*/
    @Test
    public void testFindEventByAlias() throws Exception {
        Results res = ss.search("alias:\"Afghany\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(EventIndexField.EVENT_ID));
        assertEquals("Afghanistan", doc.get(EventIndexField.EVENT));
    }

    @Test
    public void testFindEventByArtist() throws Exception {
        Results res = ss.search("artist:\"Пётр Ильич Чайковский\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(EventIndexField.EVENT_ID));
        assertEquals("Afghanistan", doc.get(EventIndexField.EVENT));
    }

    @Test
    public void testFindEventByPlace() throws Exception {
        Results res = ss.search("place:netherbury", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(EventIndexField.EVENT_ID));
        assertEquals("Afghanistan", doc.get(EventIndexField.EVENT));
    }

    @Test
    public void testFindEventByPlaceId() throws Exception {
        Results res = ss.search("pid:abcdef", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(EventIndexField.EVENT_ID));
        assertEquals("Afghanistan", doc.get(EventIndexField.EVENT));
    }

    @Test
    public void testFindEventByType() throws Exception {
        Results res = ss.search("type:\"Country\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(EventIndexField.EVENT_ID));
        assertEquals("Afghanistan", doc.get(EventIndexField.EVENT));
    }

    @Test
    public void testFindEventByComment() throws Exception {
        Results res = ss.search("comment:comment", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(EventIndexField.EVENT_ID));
        assertEquals("Afghanistan", doc.get(EventIndexField.EVENT));
    }


    @Test
    public void testFindEventByBegin() throws Exception {
        Results res = ss.search("begin:1993", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(EventIndexField.EVENT_ID));
        assertEquals("Afghanistan", doc.get(EventIndexField.EVENT));
    }

    @Test
    public void testFindEventByEnd() throws Exception {
        Results res = ss.search("end:2004", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(EventIndexField.EVENT_ID));
        assertEquals("Afghanistan", doc.get(EventIndexField.EVENT));
    }


    @Test
    public void testFindEventByDismax1() throws Exception {
        Results res = sd.search("Afghanistan", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(EventIndexField.EVENT_ID));
        assertEquals("Afghanistan", doc.get(EventIndexField.EVENT));
    }



    @Test
    public void testFindEventByDismax2() throws Exception {
        Results res = sd.search("afghany", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(EventIndexField.EVENT_ID));
        assertEquals("Afghanistan", doc.get(EventIndexField.EVENT));
    }

    @Test
    public void testFindEventByTag() throws Exception {
        Results res = ss.search("tag:desert", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(EventIndexField.EVENT_ID));
        assertEquals("Afghanistan", doc.get(EventIndexField.EVENT));
    }

    /**
     * Tests get same results as
     * http://musicbrainz.org/ws/1/event/?type=xml&query=%22Jockey%20Slut%22
     *
     * @throws Exception exception
     */
    @Test
    public void testOutputAsXml() throws Exception {

        Results res = ss.search("event:\"Afghanistan\"", 0, 1);
        ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();
        String output = sw.toString();
        System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("xmlns:ext=\"http://musicbrainz.org/ns/ext#-2.0\""));
        assertTrue(output.contains("id=\"ff571ff4-04cb-4b9c-8a1c-354c330f863c\""));
        assertTrue(output.contains("type=\"Country\""));
        assertTrue(output.contains("<name>Afghanistan</name>"));
        assertTrue(output.contains("<alias sort-name=\"Afghan\">Afghany</alias></alias-list>"));
        assertTrue(output.contains("<begin>1993</begin>"));
        assertTrue(output.contains("<end>2004</end>"));
        assertTrue(output.contains("<disambiguation>A comment</disambiguation>"));
        assertTrue(output.contains("<tag-list><tag count=\"22\"><name>desert</name></tag></tag-list>"));
    }

    @Test
    public void testOutputAsXmlIdent() throws Exception {

        Results res = ss.search("event:\"Afghanistan\"", 0, 1);
        ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_XML, true);
        pr.close();
        String output = sw.toString();
        System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testOutputJson() throws Exception {

        Results res = ss.search("event:\"Afghanistan\"", 0, 10);
        org.musicbrainz.search.servlet.mmd2.ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON);
        pr.close();

        String output = sw.toString();
        System.out.println("Json is" + output);

        assertTrue(output.contains("id\":\"ff571ff4-04cb-4b9c-8a1c-354c330f863c\""));
        assertTrue(output.contains("\"count\":1"));
        assertTrue(output.contains("\"offset\":0,"));
        assertTrue(output.contains("\"type\":\"Country\""));
        assertTrue(output.contains("name\":\"Afghanistan\""));
        assertTrue(output.contains("life-span\":{\"begin\":\"1993\""));
        assertTrue(output.contains("\"disambiguation\":\"A comment\","));
        assertTrue(output.contains("\"tag-list\":{\"tag\":[{\"count\":22,\"name\":\"desert\"}]}"));
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testOutputJsonNew() throws Exception {

        Results res = ss.search("event:\"Afghanistan\"", 0, 10);
        org.musicbrainz.search.servlet.mmd2.ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON_NEW);
        pr.close();

        String output = sw.toString();
        System.out.println("Json New is" + output);
        assertTrue(output.contains("events"));
        assertTrue(output.contains("tags"));
        assertTrue(output.contains("id\":\"ff571ff4-04cb-4b9c-8a1c-354c330f863c\""));
        assertTrue(output.contains("\"type\":\"Country\""));
        assertTrue(output.contains("name\":\"Afghanistan\""));
        assertTrue(output.contains("{\"sort-name\":\"Afghan\",\"name\":\"Afghany\",\"locale\":null,\"type\":null,\"primary\":null,\"begin-date\":null,\"end-date\":null}]"));
        assertTrue(output.contains("life-span\":{\"begin\":\"1993\""));
        assertTrue(output.contains("\"end\":\"2004\""));
        assertTrue(output.contains("\"disambiguation\":\"A comment\","));
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testOutputJsonNewIdent() throws Exception {

        Results res = ss.search("event:\"Afghanistan\"", 0, 10);
        org.musicbrainz.search.servlet.mmd2.ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON_NEW, true);
        pr.close();

        String output = sw.toString();
        System.out.println("Json New Ident is" + output);
        assertTrue(output.contains("\"offset\" : 0"));

    }

}