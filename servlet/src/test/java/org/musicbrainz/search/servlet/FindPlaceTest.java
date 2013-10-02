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
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Assumes an index has been built stored and in the data folder, I've picked a fairly obscure bside so hopefully
 * will not get added to another release
 */
public class FindPlaceTest {

    private AbstractSearchServer ss;
    private AbstractDismaxSearchServer sd;


    @Before
    public void setUp() throws Exception {
        ObjectFactory of = new ObjectFactory();
        RAMDirectory ramDir = new RAMDirectory();
        Analyzer analyzer = DatabaseIndex.getAnalyzer(PlaceIndexField.class);
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION, analyzer);
        writerConfig.setSimilarity(new MusicbrainzSimilarity());
        IndexWriter writer = new IndexWriter(ramDir, writerConfig);

        {
            MbDocument doc = new MbDocument();

            Place place = of.createPlace();
            doc.addField(PlaceIndexField.PLACE_ID, "ff571ff4-04cb-4b9c-8a1c-354c330f863c");
            place.setId("ff571ff4-04cb-4b9c-8a1c-354c330f863c");

            doc.addField(PlaceIndexField.PLACE, "Afghanistan");
            place.setName("Afghanistan");

            doc.addField(PlaceIndexField.ADDRESS, "1 new Street");
            place.setAddress("1 new Street");

            doc.addNumericField(PlaceIndexField.LAT, -180f);
            doc.addNumericField(PlaceIndexField.LONG, 120f);

            Coordinates coords = of.createCoordinates();
            coords.setLatitude("-180.45");
            coords.setLongitude("120");
            place.setCoordinates(coords);

            doc.addField(PlaceIndexField.ALIAS, "Afghany");
            AliasList aliasList = of.createAliasList();
            Alias alias = of.createAlias();
            aliasList.getAlias().add(alias);
            alias.setContent("Afghany");
            alias.setSortName("Afghan");
            place.setAliasList(aliasList);

            doc.addField(PlaceIndexField.BEGIN, "1993");
            doc.addField(PlaceIndexField.END, "2004");
            doc.addField(PlaceIndexField.ENDED, "true");
            LifeSpan lifespan = of.createLifeSpan();
            place.setLifeSpan(lifespan);
            lifespan.setBegin("1993");
            lifespan.setEnd("2004");
            lifespan.setEnded("true");

            doc.addField(PlaceIndexField.TYPE,"Country");
            place.setType("Country");


            doc.addField(PlaceIndexField.PLACE_STORE, MMDSerializer.serialize(place));
            writer.addDocument(doc.getLuceneDocument());
        }

        {
            MbDocument doc = new MbDocument();
            PlaceList placeList = of.createPlaceList();
            Place place = of.createPlace();
            placeList.getPlace().add(place);
            doc.addField(MetaIndexField.META, MetaIndexField.META_VALUE);
            doc.addNumericField(MetaIndexField.LAST_UPDATED, new Date().getTime());
            doc.addField(PlaceIndexField.PLACE_STORE, MMDSerializer.serialize(placeList));
            writer.addDocument(doc.getLuceneDocument());
        }


        writer.close();
        SearcherManager searcherManager = new SearcherManager(ramDir, new MusicBrainzSearcherFactory(ResourceType.PLACE));
        ss = new PlaceSearch(searcherManager);
        sd = new PlaceDismaxSearch(ss);
    }

    @Test
    public void testFindPlaceById() throws Exception {
        Results res = ss.search("pid:\"ff571ff4-04cb-4b9c-8a1c-354c330f863c\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(PlaceIndexField.PLACE_ID));
        assertEquals("Afghanistan", doc.get(PlaceIndexField.PLACE));
    }

    @Test
    public void testFindPlaceByName() throws Exception {
        Results res = ss.search("place:\"Afghanistan\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(PlaceIndexField.PLACE_ID));
        assertEquals("Afghanistan", doc.get(PlaceIndexField.PLACE));
    }

    @Test
    public void testFindPlaceByAddress() throws Exception {
        Results res = ss.search("address:\"1 new street\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(PlaceIndexField.PLACE_ID));
        assertEquals("Afghanistan", doc.get(PlaceIndexField.PLACE));
    }

    @Test
    public void testFindPlaceByDefaultName() throws Exception {
        Results res = ss.search("\"Afghanistan\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(PlaceIndexField.PLACE_ID));
        assertEquals("Afghanistan", doc.get(PlaceIndexField.PLACE));
    }


    @Test
    public void testFindPlaceByDefaultAlias() throws Exception {
        Results res = ss.search("\"Afghany\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(PlaceIndexField.PLACE_ID));
        assertEquals("Afghanistan", doc.get(PlaceIndexField.PLACE));
    }

    @Test
    public void testFindPlaceByAlias() throws Exception {
        Results res = ss.search("alias:\"Afghany\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(PlaceIndexField.PLACE_ID));
        assertEquals("Afghanistan", doc.get(PlaceIndexField.PLACE));
    }

    @Test
    public void testFindPlaceByType() throws Exception {
        Results res = ss.search("type:\"Country\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(PlaceIndexField.PLACE_ID));
        assertEquals("Afghanistan", doc.get(PlaceIndexField.PLACE));
    }

    @Test
    public void testFindPlaceByBegin() throws Exception {
        Results res = ss.search("begin:1993", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(PlaceIndexField.PLACE_ID));
        assertEquals("Afghanistan", doc.get(PlaceIndexField.PLACE));
    }

    @Test
    public void testFindPlaceByEnd() throws Exception {
        Results res = ss.search("end:2004", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(PlaceIndexField.PLACE_ID));
        assertEquals("Afghanistan", doc.get(PlaceIndexField.PLACE));
    }

    @Test
    public void testFindPlaceByEnded() throws Exception {
        Results res = ss.search("ended:true", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(PlaceIndexField.PLACE_ID));
        assertEquals("Afghanistan", doc.get(PlaceIndexField.PLACE));
    }

    @Test
    public void testFindPlaceByDismax1() throws Exception {
        Results res = sd.search("Afghanistan", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(PlaceIndexField.PLACE_ID));
        assertEquals("Afghanistan", doc.get(PlaceIndexField.PLACE));
    }



    @Test
    public void testFindPlaceByDismax2() throws Exception {
        Results res = sd.search("afghany", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(PlaceIndexField.PLACE_ID));
        assertEquals("Afghanistan", doc.get(PlaceIndexField.PLACE));
    }

    @Test
    public void testFindPlaceByDismax3() throws Exception {
        Results res = sd.search("new", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(PlaceIndexField.PLACE_ID));
        assertEquals("Afghanistan", doc.get(PlaceIndexField.PLACE));
    }

    @Test
    public void testFindPlaceByLat() throws Exception {
        Results res = ss.search("lat:\\-180", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(PlaceIndexField.PLACE_ID));
        assertEquals("Afghanistan", doc.get(PlaceIndexField.PLACE));
    }

    @Test
    public void testFindPlaceByLong() throws Exception {
        Results res = ss.search("long:120", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(PlaceIndexField.PLACE_ID));
        assertEquals("Afghanistan", doc.get(PlaceIndexField.PLACE));
    }

    @Test
    public void testFindPlaceByLatRange() throws Exception {
        Results res = ss.search("lat:[\\-190 TO -170]", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(PlaceIndexField.PLACE_ID));
        assertEquals("Afghanistan", doc.get(PlaceIndexField.PLACE));
    }

    @Test
    public void testFindPlaceByLatNotInRange() throws Exception {
        Results res = ss.search("lat:[\\-200 TO -190]", 0, 10);
        assertEquals(0, res.getTotalHits());
    }

    /**
     * Tests get same results as
     * http://musicbrainz.org/ws/1/place/?type=xml&query=%22Jockey%20Slut%22
     *
     * @throws Exception exception
     */
    @Test
    public void testOutputAsXml() throws Exception {

        Results res = ss.search("place:\"Afghanistan\"", 0, 1);
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

        assertTrue(output.contains("<begin>1993</begin"));
        assertTrue(output.contains("<end>2004</end>"));
        assertTrue(output.contains("<ended>true</ended>"));
        assertTrue(output.contains("<coordinates><latitude>-180.45</latitude><longitude>120</longitude></coordinates>"));

    }

    @Test
    public void testOutputAsXmlIdent() throws Exception {

        Results res = ss.search("place:\"Afghanistan\"", 0, 1);
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

        Results res = ss.search("place:\"Afghanistan\"", 0, 10);
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
        assertTrue(output.contains("\"ended\":\"true\""));
        assertTrue(output.contains("\"coordinates\":{\"latitude\":\"-180.45\",\"longitude\":\"120\"},"));

    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testOutputJsonNew() throws Exception {

        Results res = ss.search("place:\"Afghanistan\"", 0, 10);
        org.musicbrainz.search.servlet.mmd2.ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON_NEW);
        pr.close();

        String output = sw.toString();
        System.out.println("Json New is" + output);

        assertTrue(output.contains("id\":\"ff571ff4-04cb-4b9c-8a1c-354c330f863c\""));
        assertTrue(output.contains("\"type\":\"Country\""));
        assertTrue(output.contains("name\":\"Afghanistan\""));
        assertTrue(output.contains("\"sort-name\":\"Afghan\",\"name\":\"Afghany\",\"locale\":null,\"type\":null,\"begin-date\":null,\"end-date\":null}"));
        assertTrue(output.contains("life-span\":{\"begin\":\"1993\""));
        assertTrue(output.contains("\"ended\":true"));
        assertTrue(output.contains("\"end\":\"2004\""));
        assertTrue(output.contains("coordinates\":{\"latitude\":\"-180.45\",\"longitude\":\"120\"}"));
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testOutputJsonNewIdent() throws Exception {

        Results res = ss.search("place:\"Afghanistan\"", 0, 10);
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