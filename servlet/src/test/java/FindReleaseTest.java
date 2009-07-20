import junit.framework.TestCase;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.search.Index;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.ReleaseIndexField;
import org.musicbrainz.search.ReleaseXmlWriter;
import org.musicbrainz.search.ResourceType;
import org.musicbrainz.search.Result;
import org.musicbrainz.search.Results;
import org.musicbrainz.search.ResultsWriter;
import org.musicbrainz.search.SearchServer;
import org.musicbrainz.search.analysis.StandardUnaccentAnalyzer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Assumes an index has been built stored and in the data folder, I've picked a fairly obscure bside so hopefully
 * will not get added to another release
 */
public class FindReleaseTest extends TestCase {

    private SearchServer ss;


    public FindReleaseTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        RAMDirectory ramDir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(ramDir, new StandardUnaccentAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);

        Document doc = new Document();
        Index.addFieldToDocument(doc, ReleaseIndexField.RELEASE_ID, "1d9e8ed6-3893-4d3b-aa7d-6cd79609e386");
        Index.addFieldToDocument(doc, ReleaseIndexField.RELEASE, "Our Glorious 5 Year Plan");
        Index.addFieldToDocument(doc, ReleaseIndexField.SCRIPT, "Latn");
        Index.addFieldToDocument(doc, ReleaseIndexField.LANGUAGE, "eng");
        Index.addFieldToDocument(doc, ReleaseIndexField.ARTIST_ID, "4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
        Index.addFieldToDocument(doc, ReleaseIndexField.ARTIST, "Farming Incident");
        Index.addFieldToDocument(doc, ReleaseIndexField.NUM_TRACKS, "10");
        Index.addFieldToDocument(doc, ReleaseIndexField.NUM_DISC_IDS, "1");
        Index.addFieldToDocument(doc, ReleaseIndexField.STATUS, "official");
        Index.addFieldToDocument(doc, ReleaseIndexField.TYPE, "album");


        //Per Event
        Index.addFieldToDocument(doc, ReleaseIndexField.COUNTRY, "gb");
        Index.addFieldToDocument(doc, ReleaseIndexField.LABEL, "Wrath Records");
        Index.addFieldToDocument(doc, ReleaseIndexField.CATALOG_NO, "WRATHCD25");
        Index.addFieldToDocument(doc, ReleaseIndexField.DATE, "2005");
        Index.addFieldToDocument(doc, ReleaseIndexField.BARCODE, "-");

        writer.addDocument(doc);
        writer.close();
        Map<ResourceType, IndexSearcher> searchers = new HashMap<ResourceType, IndexSearcher>();
        searchers.put(ResourceType.RELEASE, new IndexSearcher(ramDir));
        ss = new SearchServer(searchers);
    }

    public void testFindReleaseByName() throws Exception {
        Results res = ss.search(ResourceType.RELEASE, "release:\"Our Glorious 5 Year Plan\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        assertEquals("10", doc.get(ReleaseIndexField.NUM_TRACKS));
        assertEquals(1, doc.getFields(ReleaseIndexField.CATALOG_NO).length);
        assertEquals("WRATHCD25", doc.get(ReleaseIndexField.CATALOG_NO));
        assertEquals(1, doc.getFields(ReleaseIndexField.BARCODE).length);
        assertEquals("-", doc.get(ReleaseIndexField.BARCODE));
        assertEquals(1, doc.getFields(ReleaseIndexField.COUNTRY).length);
        assertEquals("gb", doc.get(ReleaseIndexField.COUNTRY));
        assertEquals(1, doc.getFields(ReleaseIndexField.DATE).length);
        assertEquals("2005", doc.get(ReleaseIndexField.DATE));
        assertEquals(1, doc.getFields(ReleaseIndexField.NUM_DISC_IDS).length);
        assertEquals("1", doc.get(ReleaseIndexField.NUM_DISC_IDS));
        assertEquals("eng", doc.get(ReleaseIndexField.LANGUAGE));
        assertEquals("Latn", doc.get(ReleaseIndexField.SCRIPT));
        assertEquals("official", doc.get(ReleaseIndexField.STATUS));
        assertEquals("album", doc.get(ReleaseIndexField.TYPE));

    }

    public void testFindReleaseByDefault() throws Exception {
        Results res = ss.search(ResourceType.RELEASE, "\"Our Glorious 5 Year Plan\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        assertEquals("10", doc.get(ReleaseIndexField.NUM_TRACKS));
        assertEquals(1, doc.getFields(ReleaseIndexField.CATALOG_NO).length);
        assertEquals("WRATHCD25", doc.get(ReleaseIndexField.CATALOG_NO));
        assertEquals(1, doc.getFields(ReleaseIndexField.BARCODE).length);
        assertEquals("-", doc.get(ReleaseIndexField.BARCODE));
        assertEquals(1, doc.getFields(ReleaseIndexField.COUNTRY).length);
        assertEquals("gb", doc.get(ReleaseIndexField.COUNTRY));
        assertEquals(1, doc.getFields(ReleaseIndexField.DATE).length);
        assertEquals("2005", doc.get(ReleaseIndexField.DATE));
        assertEquals(1, doc.getFields(ReleaseIndexField.NUM_DISC_IDS).length);
        assertEquals("1", doc.get(ReleaseIndexField.NUM_DISC_IDS));
        assertEquals("eng", doc.get(ReleaseIndexField.LANGUAGE));
        assertEquals("Latn", doc.get(ReleaseIndexField.SCRIPT));
        assertEquals("official", doc.get(ReleaseIndexField.STATUS));
        assertEquals("album", doc.get(ReleaseIndexField.TYPE));

    }

    public void testFindReleaseByArtistName() throws Exception {
        Results res = ss.search(ResourceType.RELEASE, "artist:\"Farming Incident\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        assertEquals("10", doc.get(ReleaseIndexField.NUM_TRACKS));
        assertEquals(1, doc.getFields(ReleaseIndexField.CATALOG_NO).length);
        assertEquals("WRATHCD25", doc.get(ReleaseIndexField.CATALOG_NO));
        assertEquals(1, doc.getFields(ReleaseIndexField.BARCODE).length);
        assertEquals("-", doc.get(ReleaseIndexField.BARCODE));
        assertEquals(1, doc.getFields(ReleaseIndexField.COUNTRY).length);
        assertEquals("gb", doc.get(ReleaseIndexField.COUNTRY));
        assertEquals(1, doc.getFields(ReleaseIndexField.DATE).length);
        assertEquals("2005", doc.get(ReleaseIndexField.DATE));
        assertEquals(1, doc.getFields(ReleaseIndexField.NUM_DISC_IDS).length);
        assertEquals("1", doc.get(ReleaseIndexField.NUM_DISC_IDS));
        assertEquals("eng", doc.get(ReleaseIndexField.LANGUAGE));
        assertEquals("Latn", doc.get(ReleaseIndexField.SCRIPT));
        assertEquals("official", doc.get(ReleaseIndexField.STATUS));
        assertEquals("album", doc.get(ReleaseIndexField.TYPE));
    }

    /**
     * Tests get same results as
     * http://musicbrainz.org/ws/1/release/?type=xml&query=%22Our%20Glorious%205%20Year%20Plan%22
     *
     * @throws Exception
     */
    public void testOutputAsXml() throws Exception {

        Results res = ss.search(ResourceType.RELEASE, "release:\"Our Glorious 5 Year Plan\"", 0, 1);
        ResultsWriter writer = new ReleaseXmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();
        String output = sw.toString();
        //System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("id=\"1d9e8ed6-3893-4d3b-aa7d-6cd79609e386\""));
        assertTrue(output.contains("language=\"ENG\""));
        assertTrue(output.contains("script=\"Latn\""));
        assertTrue(output.contains("type=\"Album Official\""));
        assertTrue(output.contains("<title>Our Glorious 5 Year Plan</title>"));
        assertTrue(output.contains("<name>Farming Incident</name>"));
        assertTrue(output.contains("artist id=\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\""));
        assertTrue(output.contains("<disc-list count=\"1\""));
        assertTrue(output.contains("<track-list count=\"10\""));
        assertTrue(output.contains("date=\"2005\""));
        assertTrue(output.contains("country=\"GB\""));

//      assertTrue(output.contains("label=\"Wrath Records\"")); #5225 this is what current service returns but invalid for MMD
        assertTrue(output.contains("<label><name>Wrath Records</name></label>"));
//      assertTrue(output.contains("barcode=\"-\""));       Code doesnt show if '-' but looks like should looking at main server
        assertTrue(output.contains("catalog-number=\"WRATHCD25\""));   // #5225 but current service breaks MMD and returns as catno

    }
}