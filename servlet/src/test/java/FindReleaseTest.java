import junit.framework.TestCase;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.musicbrainz.search.analysis.StandardUnaccentAnalyzer;
import org.musicbrainz.search.*;

import java.util.Map;
import java.util.HashMap;
import java.io.StringWriter;
import java.io.PrintWriter;

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

        ReleaseIndex li = new ReleaseIndex();
        Document doc = new Document();
        li.addReleaseGidToDocument(doc, "1d9e8ed6-3893-4d3b-aa7d-6cd79609e386");
        li.addReleaseToDocument(doc, "Our Glorious 5 Year Plan");
        li.addScriptToDocument(doc, "latn");
        li.addLanguageToDocument(doc, "eng");
        li.addArtistGidToDocument(doc, "4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
        li.addArtistToDocument(doc, "Farming Incident");
        li.addNumTracksToDocument(doc, "10");
        li.addDiscIdsToDocument(doc, "1");

        //Per Event
        li.addCountryToDocument(doc, "gb");
        li.addLabelToDocument(doc, "Wrath Records");
        li.addCatalogNoToDocument(doc, "WRATHCD25");
        li.addDateToDocument(doc, "2005");
        li.addBarcodeToDocument(doc, "-");

        writer.addDocument(doc);
        writer.close();
        Map<String, IndexSearcher> searchers = new HashMap<String, IndexSearcher>();
        searchers.put("release", new IndexSearcher(ramDir));
        ss = new SearchServer(searchers);
    }

    public void testFindReleaseByName() throws Exception {
        Results res = ss.search("release", "release:\"Our Glorious 5 Year Plan\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        Document doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexFieldName.RELEASE.getFieldname()));
        assertEquals("Farming Incident", doc.get(ReleaseIndexFieldName.ARTIST.getFieldname()));
        assertEquals("Wrath Records", doc.get(ReleaseIndexFieldName.LABEL.getFieldname()));
        assertEquals("10", doc.get(ReleaseIndexFieldName.NUM_TRACKS.getFieldname()));
        assertEquals(1, doc.getFields(ReleaseIndexFieldName.CATALOG_NO.getFieldname()).length);
        assertEquals("WRATHCD25", doc.get(ReleaseIndexFieldName.CATALOG_NO.getFieldname()));
        assertEquals(1, doc.getFields(ReleaseIndexFieldName.BARCODE.getFieldname()).length);
        assertEquals("-", doc.get(ReleaseIndexFieldName.BARCODE.getFieldname()));
        assertEquals(1, doc.getFields(ReleaseIndexFieldName.COUNTRY.getFieldname()).length);
        assertEquals("gb", doc.get(ReleaseIndexFieldName.COUNTRY.getFieldname()));
        assertEquals(1, doc.getFields(ReleaseIndexFieldName.DATE.getFieldname()).length);
        assertEquals("2005", doc.get(ReleaseIndexFieldName.DATE.getFieldname()));
        assertEquals(1, doc.getFields(ReleaseIndexFieldName.NUM_DISC_IDS.getFieldname()).length);
        assertEquals("1", doc.get(ReleaseIndexFieldName.NUM_DISC_IDS.getFieldname()));
    }

    public void testFindReleaseByArtistName() throws Exception {
        Results res = ss.search("release", "artist:\"Farming Incident\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        Document doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexFieldName.RELEASE.getFieldname()));
        assertEquals("Farming Incident", doc.get(ReleaseIndexFieldName.ARTIST.getFieldname()));
        assertEquals("Wrath Records", doc.get(ReleaseIndexFieldName.LABEL.getFieldname()));
        assertEquals("10", doc.get(ReleaseIndexFieldName.NUM_TRACKS.getFieldname()));
        assertEquals(1, doc.getFields(ReleaseIndexFieldName.CATALOG_NO.getFieldname()).length);
        assertEquals("WRATHCD25", doc.get(ReleaseIndexFieldName.CATALOG_NO.getFieldname()));
        assertEquals(1, doc.getFields(ReleaseIndexFieldName.BARCODE.getFieldname()).length);
        assertEquals("-", doc.get(ReleaseIndexFieldName.BARCODE.getFieldname()));
        assertEquals(1, doc.getFields(ReleaseIndexFieldName.COUNTRY.getFieldname()).length);
        assertEquals("gb", doc.get(ReleaseIndexFieldName.COUNTRY.getFieldname()));
        assertEquals(1, doc.getFields(ReleaseIndexFieldName.DATE.getFieldname()).length);
        assertEquals("2005", doc.get(ReleaseIndexFieldName.DATE.getFieldname()));
        assertEquals(1, doc.getFields(ReleaseIndexFieldName.NUM_DISC_IDS.getFieldname()).length);
        assertEquals("1", doc.get(ReleaseIndexFieldName.NUM_DISC_IDS.getFieldname()));
    }

    /**
     * Tests get same results as
     * http://musicbrainz.org/ws/1/release/?type=xml&query=%22Our%20Glorious%205%20Year%20Plan%22
     *
     * @throws Exception
     */
    public void testOutputAsXml() throws Exception {

        Results res = ss.search("release", "release:\"Our Glorious 5 Year Plan\"", 0, 1);
        ResultsWriter writer = new ReleaseXmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();
        String output = sw.toString();
        //System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("release id=\"1d9e8ed6-3893-4d3b-aa7d-6cd79609e386\""));
//        assertTrue(output.contains("language=\"ENG\""));
//        assertTrue(output.contains("script=\"Latn\""));
//        assertTrue(output.contains("type=\"Album Official\""));
        assertTrue(output.contains("<title>Our Glorious 5 Year Plan</title>"));
        assertTrue(output.contains("<name>Farming Incident</name>"));
        assertTrue(output.contains("artist id=\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\""));
        assertTrue(output.contains("<disc-list count=\"1\""));
        assertTrue(output.contains("<track-list count=\"10\""));
        assertTrue(output.contains("date=\"2005\""));
        assertTrue(output.contains("country=\"GB\""));
        assertTrue(output.contains("label=\"Wrath Records\""));
//        assertTrue(output.contains("barcode=\"-\""));       Code doesnt show if '-' but loks like should looking at main server
        assertTrue(output.contains("catalog-number=\"WRATHCD25\""));   //Why does formast define as this, but main server returns cat-no


    }
}