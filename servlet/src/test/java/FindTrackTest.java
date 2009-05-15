import junit.framework.TestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Hits;
import org.apache.lucene.queryParser.QueryParser;
import org.musicbrainz.search.analysis.StandardUnaccentAnalyzer;
import org.musicbrainz.search.*;

import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.io.StringWriter;
import java.io.PrintWriter;

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
        RAMDirectory ramDir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(ramDir, new StandardUnaccentAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);

        TrackIndex li = new TrackIndex();
        Document doc = new Document();
        li.addTrackGidToDocument(doc, "7ca7782b-a602-448b-b108-bb881a7be2d6");
        li.addTrackToDocument(doc, "Gravitational Lenz");
        li.addReleaseGidToDocument(doc, "1d9e8ed6-3893-4d3b-aa7d-6cd79609e386");
        li.addReleaseToDocument(doc, "Our Glorious 5 Year Plan");
        li.addArtistGidToDocument(doc, "4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
        li.addArtistToDocument(doc, "Farming Incident");
        li.addDurationToDocument(doc, "00000000002340");
        li.addNumTracksToDocument(doc, "10");
        li.addTrackNoToDocument(doc, "00000000000005");
        writer.addDocument(doc);
        writer.close();
        Map<String, IndexSearcher> searchers = new HashMap<String, IndexSearcher>();
        searchers.put("track", new IndexSearcher(ramDir));
        ss = new SearchServer(searchers);
    }

    public void testFindTrack() throws Exception {
        Results res = ss.search("track", "track:\"Gravitational Lenz\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        Document doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(TrackIndexFieldName.TRACK_ID.getFieldname()));
        assertEquals("Gravitational Lenz", doc.get(TrackIndexFieldName.TRACK.getFieldname()));
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexFieldName.ARTIST_ID.getFieldname()));
        assertEquals("Farming Incident", doc.get(TrackIndexFieldName.ARTIST.getFieldname()));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(TrackIndexFieldName.RELEASE_ID.getFieldname()));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexFieldName.RELEASE.getFieldname()));
        assertEquals("00000000000005", doc.get(TrackIndexFieldName.TRACKNUM.getFieldname()));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexFieldName.RELEASE.getFieldname()));
        assertEquals("00000000002340", doc.get(TrackIndexFieldName.DURATION.getFieldname()));
    }

    /**
     * Results should match http://musicbrainz.org/ws/1/track/?type=xml&query=%22Gravitational%20Lenz%22
     *
     * @throws Exception
     */
    public void testOutputAsXml() throws Exception {

        Results res = ss.search("track", "track:\"Gravitational Lenz\"", 0, 10);
        ResultsWriter writer = new TrackXmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();
        String output = sw.toString();
        //System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("<track id=\"7ca7782b-a602-448b-b108-bb881a7be2d6\""));
        assertTrue(output.contains("<title>Gravitational Lenz</title>"));
        //assertTrue(output.contains("duration=\"2430\""))
        assertTrue(output.contains("<artist id=\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\""));
        assertTrue(output.contains("<name>Farming Incident</name>"));
        assertTrue(output.contains("release id=\"1d9e8ed6-3893-4d3b-aa7d-6cd79609e386\""));
        assertTrue(output.contains("<title>Our Glorious 5 Year Plan</title>"));
        //assertTrue(output.contains("offset=\"5\""));
        //assertTrue(output.contains("count=\"10\""));

    }
}
