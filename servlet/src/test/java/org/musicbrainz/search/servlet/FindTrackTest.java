package org.musicbrainz.search.servlet;
import junit.framework.TestCase;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.util.NumericUtils;
import org.apache.velocity.app.Velocity;
import org.musicbrainz.search.index.*;
import org.musicbrainz.search.servlet.mmd2.TrackXmlWriter;
import org.musicbrainz.search.servlet.mmd1.TrackMmd1XmlWriter;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.EnumMap;

/**
 * Assumes an index has been built stored and in the data folder, I've picked a fairly obscure bside so hopefully
 * will not get added to another release
 */
public class FindTrackTest extends TestCase {


    private SearchServer ss;


    public FindTrackTest(String testName) {
        super(testName);
    }


    @Override
    protected void setUp() throws Exception {
        SearchServerServlet.setUpVelocity();
        Velocity.init();
        RAMDirectory ramDir = new RAMDirectory();
        PerFieldAnalyzerWrapper analyzer = new PerFieldEntityAnalyzer(TrackIndexField.class);
        IndexWriter writer = new IndexWriter(ramDir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);

        Document doc = new Document();
        Index.addFieldToDocument(doc, TrackIndexField.TRACK_ID, "7ca7782b-a602-448b-b108-bb881a7be2d6");
        Index.addFieldToDocument(doc, TrackIndexField.TRACK, "Gravitational Lenz");
        Index.addFieldToDocument(doc, TrackIndexField.RELEASE_ID, "1d9e8ed6-3893-4d3b-aa7d-6cd79609e386");
        Index.addFieldToDocument(doc, TrackIndexField.RELEASE, "Our Glorious 5 Year Plan");
        Index.addFieldToDocument(doc, TrackIndexField.ARTIST_ID, "4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
        Index.addFieldToDocument(doc, TrackIndexField.ARTIST, "Farming Incident");
        Index.addFieldToDocument(doc, TrackIndexField.ARTIST_NAME, "Farming Incident");
        Index.addFieldToDocument(doc, TrackIndexField.ARTIST_NAMECREDIT, "Farming Incident");
        Index.addFieldToDocument(doc, TrackIndexField.ARTIST_SORTNAME, "Incident, Farming");
        Index.addFieldToDocument(doc, TrackIndexField.ARTIST_JOINPHRASE, "-");

        Index.addNumericFieldToDocument(doc, TrackIndexField.DURATION, 234000);
        Index.addNumericFieldToDocument(doc, TrackIndexField.QUANTIZED_DURATION, (234000 / 2000));
        Index.addNumericFieldToDocument(doc, TrackIndexField.NUM_TRACKS,10);
        Index.addNumericFieldToDocument(doc, TrackIndexField.TRACKNUM, 5);
        Index.addFieldToDocument(doc, TrackIndexField.RELEASE_TYPE, ReleaseGroupType.ALBUM.getName());
        writer.addDocument(doc);
        writer.close();
        ss = new TrackSearch(new IndexSearcher(ramDir,true));
    }

    public void testFindTrack() throws Exception {
        Results res = ss.searchLucene("track:\"Gravitational Lenz\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(TrackIndexField.TRACK_ID));
        assertEquals("Gravitational Lenz", doc.get(TrackIndexField.TRACK));
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(TrackIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(TrackIndexField.ARTIST));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(TrackIndexField.RELEASE_ID));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexField.RELEASE));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(TrackIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(TrackIndexField.DURATION)));
    }

    public void testFindTrackById() throws Exception {
        Results res = ss.searchLucene("trid:\"7ca7782b-a602-448b-b108-bb881a7be2d6\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(TrackIndexField.TRACK_ID));
        assertEquals("Gravitational Lenz", doc.get(TrackIndexField.TRACK));
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(TrackIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(TrackIndexField.ARTIST));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(TrackIndexField.RELEASE_ID));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexField.RELEASE));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(TrackIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(TrackIndexField.DURATION)));
    }

    public void testFindTrackByReleaseId() throws Exception {
        Results res = ss.searchLucene("reid:\"1d9e8ed6-3893-4d3b-aa7d-6cd79609e386\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(TrackIndexField.TRACK_ID));
        assertEquals("Gravitational Lenz", doc.get(TrackIndexField.TRACK));
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(TrackIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(TrackIndexField.ARTIST));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(TrackIndexField.RELEASE_ID));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexField.RELEASE));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(TrackIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(TrackIndexField.DURATION)));
    }

    public void testFindTrackByArtistId() throws Exception {
        Results res = ss.searchLucene("arid:\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(TrackIndexField.TRACK_ID));
        assertEquals("Gravitational Lenz", doc.get(TrackIndexField.TRACK));
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(TrackIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(TrackIndexField.ARTIST));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(TrackIndexField.RELEASE_ID));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexField.RELEASE));
        assertEquals(5,NumericUtils.prefixCodedToInt(doc.get(TrackIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(TrackIndexField.DURATION)));
    }

     public void testFindTrackByArtistName() throws Exception {
        Results res = ss.searchLucene("artist:\"Farming Incident\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(TrackIndexField.TRACK_ID));
        assertEquals("Gravitational Lenz", doc.get(TrackIndexField.TRACK));
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(TrackIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(TrackIndexField.ARTIST));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(TrackIndexField.RELEASE_ID));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexField.RELEASE));
        assertEquals(5,NumericUtils.prefixCodedToInt(doc.get(TrackIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(TrackIndexField.DURATION)));
    }

     public void testFindTrackByArtistSortname() throws Exception {
        Results res = ss.searchLucene("sortname:\"Incident, Farming\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(TrackIndexField.TRACK_ID));
        assertEquals("Gravitational Lenz", doc.get(TrackIndexField.TRACK));
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(TrackIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(TrackIndexField.ARTIST));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(TrackIndexField.RELEASE_ID));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexField.RELEASE));
        assertEquals(5,NumericUtils.prefixCodedToInt(doc.get(TrackIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(TrackIndexField.DURATION)));
    }

    public void testFindTrackByReleaseType() throws Exception {
        Results res = ss.searchLucene("type:\"album\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(TrackIndexField.TRACK_ID));
        assertEquals("Gravitational Lenz", doc.get(TrackIndexField.TRACK));
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(TrackIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(TrackIndexField.ARTIST));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(TrackIndexField.RELEASE_ID));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexField.RELEASE));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(TrackIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(TrackIndexField.DURATION)));
    }

    public void testFindTrackByReleaseTypeNumeric() throws Exception {
        Results res = ss.searchLucene("type:\"1\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(TrackIndexField.TRACK_ID));
        assertEquals("Gravitational Lenz", doc.get(TrackIndexField.TRACK));
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(TrackIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(TrackIndexField.ARTIST));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(TrackIndexField.RELEASE_ID));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexField.RELEASE));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(TrackIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(TrackIndexField.DURATION)));
    }
    public void testFindTrackByNumberOfTracksOnRelease() throws Exception {
        Results res = ss.searchLucene("tracks:10", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(TrackIndexField.TRACK_ID));
        assertEquals("Gravitational Lenz", doc.get(TrackIndexField.TRACK));
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(TrackIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(TrackIndexField.ARTIST));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(TrackIndexField.RELEASE_ID));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexField.RELEASE));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(TrackIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(TrackIndexField.DURATION)));
    }

    public void testFindTrackByDuration() throws Exception {
        Results res = ss.searchLucene("dur:234000", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(TrackIndexField.TRACK_ID));
        assertEquals("Gravitational Lenz", doc.get(TrackIndexField.TRACK));
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(TrackIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(TrackIndexField.ARTIST));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(TrackIndexField.RELEASE_ID));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(TrackIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(TrackIndexField.DURATION)));
    }


    public void testFindTrackByNonNumericDuration() throws Exception {
        Results res = ss.searchLucene("dur:fred", 0, 10);
        assertEquals(0, res.totalHits);
    }

    public void testFindTrackByDurationRange() throws Exception {
        Results res = ss.searchLucene("dur:[87 TO 240000]", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(TrackIndexField.TRACK_ID));
        assertEquals("Gravitational Lenz", doc.get(TrackIndexField.TRACK));
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(TrackIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(TrackIndexField.ARTIST));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(TrackIndexField.RELEASE_ID));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(TrackIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(TrackIndexField.DURATION)));
    }

    public void testFindTrackByQdur() throws Exception {
        Results res = ss.searchLucene("qdur:117", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(TrackIndexField.TRACK_ID));
        assertEquals("Gravitational Lenz", doc.get(TrackIndexField.TRACK));
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(TrackIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(TrackIndexField.ARTIST));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(TrackIndexField.RELEASE_ID));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexField.RELEASE));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(TrackIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(TrackIndexField.DURATION)));
    }

    public void testFindTrackByTrackNumber() throws Exception {
        Results res = ss.searchLucene("tnum:5", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(TrackIndexField.TRACK_ID));
        assertEquals("Gravitational Lenz", doc.get(TrackIndexField.TRACK));
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(TrackIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(TrackIndexField.ARTIST));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(TrackIndexField.RELEASE_ID));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(TrackIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(TrackIndexField.DURATION)));
    }


    public void testFindTrackByDefault() throws Exception {
        Results res = ss.searchLucene("\"Gravitational Lenz\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(TrackIndexField.TRACK_ID));
        assertEquals("Gravitational Lenz", doc.get(TrackIndexField.TRACK));
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(TrackIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(TrackIndexField.ARTIST));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(TrackIndexField.RELEASE_ID));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexField.RELEASE));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(TrackIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(TrackIndexField.DURATION)));
    }


    public void testOutputAsMmd1Xml() throws Exception {

        Results res = ss.searchLucene("track:\"Gravitational Lenz\"", 0, 10);
        ResultsWriter writer = new TrackMmd1XmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();
        String output = sw.toString();
        System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("<track id=\"7ca7782b-a602-448b-b108-bb881a7be2d6\""));
        assertTrue(output.contains("<title>Gravitational Lenz</title>"));
        assertTrue(output.contains("<duration>234000</duration>"));
        assertTrue(output.contains("<artist id=\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\""));
        assertTrue(output.contains("<name>Farming Incident</name>"));
        assertTrue(output.contains("<sort-name>Incident, Farming</sort-name>"));
        assertTrue(output.contains("release type=\"Album\" id=\"1d9e8ed6-3893-4d3b-aa7d-6cd79609e386\""));
        assertTrue(output.contains("<title>Our Glorious 5 Year Plan</title>"));
        assertTrue(output.contains("offset=\"4\""));
        assertTrue(output.contains("count=\"10\""));
    }

    public void testOutputAsXml() throws Exception {

        Results res = ss.searchLucene("track:\"Gravitational Lenz\"", 0, 10);
        ResultsWriter writer = new TrackXmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();
        String output = sw.toString();
        System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("<recording id=\"7ca7782b-a602-448b-b108-bb881a7be2d6\""));
        assertTrue(output.contains("<title>Gravitational Lenz</title>"));
        assertTrue(output.contains("<length>234000</length>"));
        assertTrue(output.contains("<artist id=\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\""));
        assertTrue(output.contains("<name>Farming Incident</name>"));
        assertTrue(output.contains("<sort-name>Incident, Farming</sort-name>"));
        //assertTrue(output.contains("release type=\"Album\" id=\"1d9e8ed6-3893-4d3b-aa7d-6cd79609e386\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("count=\"1\""));
    }

      public void testOutputAsHtml() throws Exception {

        Results res = ss.searchLucene("track:\"Gravitational Lenz\"", 0, 1);
        ResultsWriter writer = new TrackHtmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();

        String output = sw.toString();
        //System.out.println("Html is" + output);
        assertTrue(output.contains("hits=1"));
        assertTrue(output.contains("offset=0"));
        assertTrue(output.contains("redirect=7ca7782b-a602-448b-b108-bb881a7be2d6"));
        assertTrue(output.contains("searchresultseven"));
        assertTrue(output.contains("Gravitational Lenz"));
        assertTrue(output.contains("Farming Incident"));
        assertTrue(output.contains("Our Glorious 5 Year Plan"));
        assertTrue(output.contains("3:54"));
        assertFalse(output.contains("http://127.0.0.1:8000/openalbum?id=1d9e8ed6-3893-4d3b-aa7d-6cd79609e386"));
        assertFalse(output.contains("tlen good"));
    }


      public void testOutputAndTportAsHtml() throws Exception {

        EnumMap<RequestParameter, String> extraInfoMap =  new EnumMap<RequestParameter, String>(RequestParameter.class);
        extraInfoMap.put(RequestParameter.TAGGER_PORT, "8000");

        Results res = ss.searchLucene("track:\"Gravitational Lenz\"", 0, 1);
        ResultsWriter writer = new TrackHtmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res,extraInfoMap);
        pr.close();

        String output = sw.toString();
        //System.out.println("Html is" + output);
        assertTrue(output.contains("hits=1"));
        assertTrue(output.contains("offset=0"));
        assertTrue(output.contains("redirect=7ca7782b-a602-448b-b108-bb881a7be2d6"));
        assertTrue(output.contains("searchresultseven"));
        assertTrue(output.contains("Gravitational Lenz"));
        assertTrue(output.contains("Farming Incident"));
        assertTrue(output.contains("Our Glorious 5 Year Plan"));
        assertTrue(output.contains("3:54"));
        assertTrue(output.contains("http://127.0.0.1:8000/openalbum?id=1d9e8ed6-3893-4d3b-aa7d-6cd79609e386"));
        assertFalse(output.contains("tlen good"));
    }

    public void testOutputAndTportDurGoodMatchAsHtml() throws Exception {

        EnumMap<RequestParameter, String> extraInfoMap =  new EnumMap<RequestParameter, String>(RequestParameter.class);
        extraInfoMap.put(RequestParameter.TAGGER_PORT, "8000");
        extraInfoMap.put(RequestParameter.DURATION, "234000");



        Results res = ss.searchLucene("track:\"Gravitational Lenz\"", 0, 1);
        ResultsWriter writer = new TrackHtmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res,extraInfoMap);
        pr.close();

        String output = sw.toString();
        //System.out.println("Html is" + output);
        assertTrue(output.contains("hits=1"));
        assertTrue(output.contains("offset=0"));
        assertTrue(output.contains("redirect=7ca7782b-a602-448b-b108-bb881a7be2d6"));
        assertTrue(output.contains("searchresultseven"));
        assertTrue(output.contains("Gravitational Lenz"));
        assertTrue(output.contains("Farming Incident"));
        assertTrue(output.contains("Our Glorious 5 Year Plan"));
        assertTrue(output.contains("3:54"));
        assertTrue(output.contains("http://127.0.0.1:8000/openalbum?id=1d9e8ed6-3893-4d3b-aa7d-6cd79609e386"));
        assertTrue(output.contains("tlen good"));
    }

    public void testOutputAndTportDurOkMatchAsHtml() throws Exception {

        EnumMap<RequestParameter, String> extraInfoMap =  new EnumMap<RequestParameter, String>(RequestParameter.class);
        extraInfoMap.put(RequestParameter.TAGGER_PORT, "8000");
        extraInfoMap.put(RequestParameter.DURATION, "240000");



        Results res = ss.searchLucene("track:\"Gravitational Lenz\"", 0, 1);
        ResultsWriter writer = new TrackHtmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res,extraInfoMap);
        pr.close();

        String output = sw.toString();
        //System.out.println("Html is" + output);
        assertTrue(output.contains("hits=1"));
        assertTrue(output.contains("offset=0"));
        assertTrue(output.contains("redirect=7ca7782b-a602-448b-b108-bb881a7be2d6"));
        assertTrue(output.contains("searchresultseven"));
        assertTrue(output.contains("Gravitational Lenz"));
        assertTrue(output.contains("Farming Incident"));
        assertTrue(output.contains("Our Glorious 5 Year Plan"));
        assertTrue(output.contains("3:54"));
        assertTrue(output.contains("http://127.0.0.1:8000/openalbum?id=1d9e8ed6-3893-4d3b-aa7d-6cd79609e386"));
        assertTrue(output.contains("tlen ok"));
    }

    public void testOutputAndTportDurPoorMatchAsHtml() throws Exception {

        EnumMap<RequestParameter, String> extraInfoMap =  new EnumMap<RequestParameter, String>(RequestParameter.class);
        extraInfoMap.put(RequestParameter.TAGGER_PORT, "8000");
        extraInfoMap.put(RequestParameter.DURATION, "240");



        Results res = ss.searchLucene("track:\"Gravitational Lenz\"", 0, 1);
        ResultsWriter writer = new TrackHtmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res,extraInfoMap);
        pr.close();

        String output = sw.toString();
        //System.out.println("Html is" + output);
        assertTrue(output.contains("hits=1"));
        assertTrue(output.contains("offset=0"));
        assertTrue(output.contains("redirect=7ca7782b-a602-448b-b108-bb881a7be2d6"));
        assertTrue(output.contains("searchresultseven"));
        assertTrue(output.contains("Gravitational Lenz"));
        assertTrue(output.contains("Farming Incident"));
        assertTrue(output.contains("Our Glorious 5 Year Plan"));
        assertTrue(output.contains("3:54"));
        assertTrue(output.contains("http://127.0.0.1:8000/openalbum?id=1d9e8ed6-3893-4d3b-aa7d-6cd79609e386"));
        assertTrue(output.contains("tlen bad"));
    }

    public void testOutputOldStyleLinkAsHtml() throws Exception {

        EnumMap<RequestParameter, String> extraInfoMap =  new EnumMap<RequestParameter, String>(RequestParameter.class);
        extraInfoMap.put(RequestParameter.OLD_STYLE_LINK, "1");



        Results res = ss.searchLucene("track:\"Gravitational Lenz\"", 0, 1);
        ResultsWriter writer = new TrackHtmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res,extraInfoMap);
        pr.close();

        String output = sw.toString();
        //System.out.println("Html is" + output);
        assertTrue(output.contains("hits=1"));
        assertTrue(output.contains("offset=0"));
        assertTrue(output.contains("redirect=7ca7782b-a602-448b-b108-bb881a7be2d6"));
        assertTrue(output.contains("searchresultseven"));
        assertTrue(output.contains("Gravitational Lenz"));
        assertTrue(output.contains("Farming Incident"));
        assertTrue(output.contains("Our Glorious 5 Year Plan"));
        assertTrue(output.contains("3:54"));
        assertTrue(output.contains("<a href=\"tag:7ca7782b-a602-448b-b108-bb881a7be2d6:1d9e8ed6-3893-4d3b-aa7d-6cd79609e386\">"));
    }
}
