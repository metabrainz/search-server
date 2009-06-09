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

        Document doc = new Document();
        Index.addFieldToDocument(doc, ReleaseIndexField.RELEASE_ID, "1d9e8ed6-3893-4d3b-aa7d-6cd79609e386");
        Index.addFieldToDocument(doc, ReleaseIndexField.RELEASE, "Our Glorious 5 Year Plan");
        Index.addFieldToDocument(doc, ReleaseIndexField.SCRIPT, "Latn");
        Index.addFieldToDocument(doc, ReleaseIndexField.LANGUAGE, "eng");
        Index.addFieldToDocument(doc, ReleaseIndexField.ARTIST_ID, "4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
        Index.addFieldToDocument(doc, ReleaseIndexField.ARTIST, "Farming Incident");
        Index.addFieldToDocument(doc, ReleaseIndexField.NUM_TRACKS, "10");
        Index.addFieldToDocument(doc, ReleaseIndexField.NUM_DISC_IDS, "1");

        //Per Event
        Index.addFieldToDocument(doc, ReleaseIndexField.COUNTRY, "gb");
        Index.addFieldToDocument(doc, ReleaseIndexField.LABEL, "Wrath Records");
        Index.addFieldToDocument(doc, ReleaseIndexField.CATALOG_NO, "WRATHCD25");
        Index.addFieldToDocument(doc, ReleaseIndexField.DATE, "2005");
        Index.addFieldToDocument(doc, ReleaseIndexField.BARCODE, "-");

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
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE.getName()));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST.getName()));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL.getName()));
        assertEquals("10", doc.get(ReleaseIndexField.NUM_TRACKS.getName()));
        assertEquals(1, doc.getFields(ReleaseIndexField.CATALOG_NO.getName()).length);
        assertEquals("WRATHCD25", doc.get(ReleaseIndexField.CATALOG_NO.getName()));
        assertEquals(1, doc.getFields(ReleaseIndexField.BARCODE.getName()).length);
        assertEquals("-", doc.get(ReleaseIndexField.BARCODE.getName()));
        assertEquals(1, doc.getFields(ReleaseIndexField.COUNTRY.getName()).length);
        assertEquals("gb", doc.get(ReleaseIndexField.COUNTRY.getName()));
        assertEquals(1, doc.getFields(ReleaseIndexField.DATE.getName()).length);
        assertEquals("2005", doc.get(ReleaseIndexField.DATE.getName()));
        assertEquals(1, doc.getFields(ReleaseIndexField.NUM_DISC_IDS.getName()).length);
        assertEquals("1", doc.get(ReleaseIndexField.NUM_DISC_IDS.getName()));
        assertEquals("eng", doc.get(ReleaseIndexField.LANGUAGE.getName()));
        assertEquals("Latn", doc.get(ReleaseIndexField.SCRIPT.getName()));
        
    }

    public void testFindReleaseByArtistName() throws Exception {
        Results res = ss.search("release", "artist:\"Farming Incident\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        Document doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE.getName()));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST.getName()));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL.getName()));
        assertEquals("10", doc.get(ReleaseIndexField.NUM_TRACKS.getName()));
        assertEquals(1, doc.getFields(ReleaseIndexField.CATALOG_NO.getName()).length);
        assertEquals("WRATHCD25", doc.get(ReleaseIndexField.CATALOG_NO.getName()));
        assertEquals(1, doc.getFields(ReleaseIndexField.BARCODE.getName()).length);
        assertEquals("-", doc.get(ReleaseIndexField.BARCODE.getName()));
        assertEquals(1, doc.getFields(ReleaseIndexField.COUNTRY.getName()).length);
        assertEquals("gb", doc.get(ReleaseIndexField.COUNTRY.getName()));
        assertEquals(1, doc.getFields(ReleaseIndexField.DATE.getName()).length);
        assertEquals("2005", doc.get(ReleaseIndexField.DATE.getName()));
        assertEquals(1, doc.getFields(ReleaseIndexField.NUM_DISC_IDS.getName()).length);
        assertEquals("1", doc.get(ReleaseIndexField.NUM_DISC_IDS.getName()));
        assertEquals("eng", doc.get(ReleaseIndexField.LANGUAGE.getName()));
        assertEquals("Latn", doc.get(ReleaseIndexField.SCRIPT.getName()));
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
        System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("id=\"1d9e8ed6-3893-4d3b-aa7d-6cd79609e386\""));
        assertTrue(output.contains("language=\"ENG\""));
        assertTrue(output.contains("script=\"Latn\""));
//        assertTrue(output.contains("type=\"Album Official\""));
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