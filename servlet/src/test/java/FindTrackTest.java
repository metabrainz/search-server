import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.search.Index;
import org.musicbrainz.search.Result;
import org.musicbrainz.search.Results;
import org.musicbrainz.search.ResultsWriter;
import org.musicbrainz.search.SearchServer;
import org.musicbrainz.search.TrackIndexField;
import org.musicbrainz.search.TrackXmlWriter;
import org.musicbrainz.search.analysis.StandardUnaccentAnalyzer;

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

        Document doc = new Document();
        Index.addFieldToDocument(doc, TrackIndexField.TRACK_ID, "7ca7782b-a602-448b-b108-bb881a7be2d6");
        Index.addFieldToDocument(doc, TrackIndexField.TRACK, "Gravitational Lenz");
        Index.addFieldToDocument(doc, TrackIndexField.RELEASE_ID, "1d9e8ed6-3893-4d3b-aa7d-6cd79609e386");
        Index.addFieldToDocument(doc, TrackIndexField.RELEASE, "Our Glorious 5 Year Plan");
        Index.addFieldToDocument(doc, TrackIndexField.ARTIST_ID, "4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
        Index.addFieldToDocument(doc, TrackIndexField.ARTIST, "Farming Incident");
        Index.addFieldToDocument(doc, TrackIndexField.DURATION, "00000000002340");
        Index.addFieldToDocument(doc, TrackIndexField.NUM_TRACKS, "10");
        Index.addFieldToDocument(doc, TrackIndexField.TRACKNUM, "00000000000005");
        
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
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(TrackIndexField.TRACK_ID.getName()));
        assertEquals("Gravitational Lenz", doc.get(TrackIndexField.TRACK.getName()));
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(TrackIndexField.ARTIST_ID.getName()));
        assertEquals("Farming Incident", doc.get(TrackIndexField.ARTIST.getName()));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(TrackIndexField.RELEASE_ID.getName()));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexField.RELEASE.getName()));
        assertEquals("00000000000005", doc.get(TrackIndexField.TRACKNUM.getName()));
        assertEquals("Our Glorious 5 Year Plan", doc.get(TrackIndexField.RELEASE.getName()));
        assertEquals("00000000002340", doc.get(TrackIndexField.DURATION.getName()));
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
        assertTrue(output.contains("<duration>2340</duration>"));
        assertTrue(output.contains("<artist id=\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\""));
        assertTrue(output.contains("<name>Farming Incident</name>"));
        assertTrue(output.contains("release id=\"1d9e8ed6-3893-4d3b-aa7d-6cd79609e386\""));
        assertTrue(output.contains("<title>Our Glorious 5 Year Plan</title>"));
        assertTrue(output.contains("offset=\"5\""));
        assertTrue(output.contains("count=\"10\""));

    }
}
