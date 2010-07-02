package org.musicbrainz.search.servlet;

import junit.framework.TestCase;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.NumericUtils;
import org.musicbrainz.mmd2.Artist;
import org.musicbrainz.mmd2.ArtistCredit;
import org.musicbrainz.mmd2.NameCredit;
import org.musicbrainz.mmd2.ObjectFactory;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;
import org.musicbrainz.search.index.MMDSerializer;
import org.musicbrainz.search.index.RecordingIndexField;
import org.musicbrainz.search.index.ReleaseGroupType;
import org.musicbrainz.search.servlet.mmd1.TrackMmd1XmlWriter;
import org.musicbrainz.search.servlet.mmd2.RecordingWriter;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Assumes an index has been built stored and in the data folder, I've picked a fairly obscure bside so hopefully
 * will not get added to another release
 */
public class FindRecordingTest extends TestCase {


    private SearchServer ss;


    public FindRecordingTest(String testName) {
        super(testName);
    }


    @Override
    protected void setUp() throws Exception {
        ObjectFactory of = new ObjectFactory();

        RAMDirectory ramDir = new RAMDirectory();
        PerFieldAnalyzerWrapper analyzer = new PerFieldEntityAnalyzer(RecordingIndexField.class);
        IndexWriter writer = new IndexWriter(ramDir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);

        MbDocument doc = new MbDocument();
        doc.addField(RecordingIndexField.RECORDING_ID, "7ca7782b-a602-448b-b108-bb881a7be2d6");
        doc.addField(RecordingIndexField.RECORDING, "Gravitational Lenz");
        doc.addField(RecordingIndexField.RECORDING_OUTPUT, "Gravitational Lenz");
        doc.addField(RecordingIndexField.RELEASE_ID, "1d9e8ed6-3893-4d3b-aa7d-6cd79609e386");
        doc.addField(RecordingIndexField.RELEASE, "Our Glorious 5 Year Plan");
        doc.addField(RecordingIndexField.ARTIST_ID, "4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
        doc.addField(RecordingIndexField.ARTIST, "Farming Incident");
        doc.addField(RecordingIndexField.ARTIST_NAME, "Farming Incident");
        doc.addField(RecordingIndexField.PUID, "1d9e8ed6-3893-4d3b-aa7d-72e79609e386");
        ArtistCredit ac = of.createArtistCredit();
        NameCredit nc = of.createNameCredit();
        Artist artist = of.createArtist();
        artist.setId("4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
        artist.setName("Farming Incident");
        artist.setSortName("Incident, Farming");
        nc.setArtist(artist);
        ac.getNameCredit().add(nc);
        doc.addField(RecordingIndexField.ARTIST_CREDIT, MMDSerializer.serialize(ac));

        doc.addNumericField(RecordingIndexField.DURATION, 234000);
        doc.addNumericField(RecordingIndexField.QUANTIZED_DURATION, (234000 / 2000));
        doc.addNumericField(RecordingIndexField.NUM_TRACKS,10);
        doc.addNumericField(RecordingIndexField.NUM_TRACKS_RELEASE,10);
        doc.addNumericField(RecordingIndexField.TRACKNUM, 5);
        doc.addField(RecordingIndexField.TRACK_OUTPUT, "Gravitational Lens");
        doc.addField(RecordingIndexField.RECORDING, "Gravitational Lens");
        doc.addField(RecordingIndexField.POSITION, "1");
        doc.addField(RecordingIndexField.RELEASE_TYPE, ReleaseGroupType.ALBUM.getName());
        doc.addField(RecordingIndexField.RELEASE_STATUS, "Official");
        doc.addField(RecordingIndexField.RELEASE_DATE, "1970-01-01");
        doc.addField(RecordingIndexField.ISRC, "123456789");
        doc.addField(RecordingIndexField.ISRC, "abcdefghi");

        doc.addField(RecordingIndexField.TAG, "indie");
        doc.addField(RecordingIndexField.TAGCOUNT, "101");

        writer.addDocument(doc.getLuceneDocument());
        writer.close();
        ss = new RecordingSearch(new IndexSearcher(ramDir,true));
    }

    public void testFindRecordingByV1TrackField() throws Exception {
        Results res = ss.searchLucene("track:\"Gravitational Lenz\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(RecordingIndexField.RELEASE_ID));
        assertEquals("Our Glorious 5 Year Plan", doc.get(RecordingIndexField.RELEASE));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(RecordingIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.DURATION)));
    }


    public void testFindRecording() throws Exception {
        Results res = ss.searchLucene("recording:\"Gravitational Lenz\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(RecordingIndexField.RELEASE_ID));
        assertEquals("Our Glorious 5 Year Plan", doc.get(RecordingIndexField.RELEASE));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(RecordingIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.DURATION)));
        assertEquals("Gravitational Lens", doc.get(RecordingIndexField.TRACK_OUTPUT));
        assertEquals("123456789", doc.get(RecordingIndexField.ISRC));
    }

     public void testFindRecordingByV1TrackId() throws Exception {
        Results res = ss.searchLucene("trid:\"7ca7782b-a602-448b-b108-bb881a7be2d6\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    public void testFindRecordingById() throws Exception {
        Results res = ss.searchLucene("rid:\"7ca7782b-a602-448b-b108-bb881a7be2d6\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    public void testFindRecordingByReleaseId() throws Exception {
        Results res = ss.searchLucene("reid:\"1d9e8ed6-3893-4d3b-aa7d-6cd79609e386\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    public void testFindRecordingByArtistId() throws Exception {
        Results res = ss.searchLucene("arid:\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

     public void testFindRecordingByArtistName() throws Exception {
        Results res = ss.searchLucene("artist:\"Farming Incident\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    /** Searches recording field, which should include names of associated tracks) */
    public void testFindRecordingByTrackName() throws Exception {
        Results res = ss.searchLucene("recording:\"Gravitational Lens\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }



    public void testFindRecordingByReleaseType() throws Exception {
        Results res = ss.searchLucene("type:\"album\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    public void testFindRecordingByReleaseTypeNumeric() throws Exception {
        Results res = ss.searchLucene("type:\"1\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }
    public void testFindRecordingByNumberOfTracksOnMediumOnRelease() throws Exception {
        Results res = ss.searchLucene("tracks:10", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

     public void testFindRecordingByNumberOfTracksOnRelease() throws Exception {
        Results res = ss.searchLucene("tracksrelease:10", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    public void testFindRecordingByDuration() throws Exception {
        Results res = ss.searchLucene("dur:234000", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    public void testFindRecordingByISRC() throws Exception {
        Results res = ss.searchLucene("isrc:123456789", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    public void testFindRecordingByNonNumericDuration() throws Exception {
        Results res = ss.searchLucene("dur:fred", 0, 10);
        assertEquals(0, res.totalHits);
    }


    public void testFindRecordingByTag() throws Exception {
        Results res = ss.searchLucene("tag:indie", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    public void testFindRecordingByDurationRange() throws Exception {
        Results res = ss.searchLucene("dur:[87 TO 240000]", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(RecordingIndexField.RELEASE_ID));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(RecordingIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.DURATION)));
    }

    public void testFindRecordingByQdur() throws Exception {
        Results res = ss.searchLucene("qdur:117", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(RecordingIndexField.RELEASE_ID));
        assertEquals("Our Glorious 5 Year Plan", doc.get(RecordingIndexField.RELEASE));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(RecordingIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.DURATION)));
    }

    public void testFindRecordingByTrackNumber() throws Exception {
        Results res = ss.searchLucene("tnum:5", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(RecordingIndexField.RELEASE_ID));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(RecordingIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.DURATION)));
    }

    public void testFindRecordingByPosition() throws Exception {
        Results res = ss.searchLucene("position:1", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(RecordingIndexField.RELEASE_ID));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(RecordingIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.DURATION)));
    }

    public void testFindRecordingByReleaseStatus() throws Exception {
        Results res = ss.searchLucene("status:Official", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(RecordingIndexField.RELEASE_ID));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(RecordingIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.DURATION)));
    }

    public void testFindRecordingByReleaseDate() throws Exception {
        Results res = ss.searchLucene("date:1970-01-01", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(RecordingIndexField.RELEASE_ID));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(RecordingIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.DURATION)));
    }


    public void testFindRecordingByDefault() throws Exception {
        Results res = ss.searchLucene("\"Gravitational Lenz\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(RecordingIndexField.RELEASE_ID));
        assertEquals("Our Glorious 5 Year Plan", doc.get(RecordingIndexField.RELEASE));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(RecordingIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.DURATION)));
    }

    public void testNumericRangeQuery() throws Exception {
           Results res = ss.searchLucene("tracks:[1 TO 10]", 0, 10);
           assertEquals(1, res.totalHits);
    }

    public void testFindRecordingByPuidIsDisallowed() throws Exception {
        Results res = ss.searchLucene("puid:1d9e8ed6-3893-4d3b-aa7d-72e79609e386", 0, 10);
        assertEquals(0, res.totalHits);
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

        Results res = ss.searchLucene("recording:\"Gravitational Lenz\"", 0, 10);
        ResultsWriter writer = new RecordingWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();
        String output = sw.toString();
        System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("xmlns:ext=\"http://musicbrainz.org/ns/ext#-2.0\""));
        assertTrue(output.contains("id=\"7ca7782b-a602-448b-b108-bb881a7be2d6\""));
        assertTrue(output.contains("<title>Gravitational Lenz</title>"));
        assertTrue(output.contains("<length>234000</length>"));
        assertTrue(output.contains("<artist id=\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\""));
        assertTrue(output.contains("<name>Farming Incident</name>"));
        assertTrue(output.contains("<sort-name>Incident, Farming</sort-name>"));
        assertTrue(output.contains("release id=\"1d9e8ed6-3893-4d3b-aa7d-6cd79609e386\""));
        assertTrue(output.contains("release-group type=\"album\""));
        assertTrue(output.contains("track-list offset=\"4\""));
        assertTrue(output.contains("count=\"10\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("<isrc id=\"123456789\"/>"));
        assertTrue(output.contains("<isrc id=\"abcdefghi\"/>"));
        assertTrue(output.contains("<title>Gravitational Lens</title>"));
        assertTrue(output.contains("<status>official</status>"));
        assertTrue(output.contains("<date>1970-01-01</date>"));
        assertTrue(output.contains("<track-count>10</track-count>"));
        assertTrue(output.contains("indie</name>"));
        assertTrue(output.contains("<puid-list><puid id=\"1d9e8ed6-3893-4d3b-aa7d-72e79609e386\"/></puid-list>"));
        

    }

    public void testOutputJson() throws Exception {

        Results res = ss.searchLucene("recording:\"Gravitational Lenz\"", 0, 10);
        ResultsWriter writer = new RecordingWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON);
        pr.close();

        String output = sw.toString();
        System.out.println("Json is" + output);

        assertTrue(output.contains("id\":\"7ca7782b-a602-448b-b108-bb881a7be2d6\""));
        assertTrue(output.contains("\"count\":1"));
        assertTrue(output.contains("\"offset\":0,"));
        assertTrue(output.contains("\"score\":\"100\""));
        assertTrue(output.contains("\"type\":\"album\""));
        assertTrue(output.contains("title\":\"Gravitational Lenz\""));
        assertTrue(output.contains("\"length\":234000"));
        assertTrue(output.contains("\"isrc\":[{\"id\":\"123456789"));
        assertTrue(output.contains("\"position\":1"));
        assertTrue(output.contains("\"status\":\"official\""));
        assertTrue(output.contains("\"track-count\":10"));
        assertTrue(output.contains("\"tag\":[{\"count\":101,\"name\":\"indie\"}"));
        assertTrue(output.contains("\"puid-list\":{\"puid\":[{\"id\":\"1d9e8ed6-3893-4d3b-aa7d-72e79609e386\"}]}"));

    }

}
