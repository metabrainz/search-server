import junit.framework.TestCase;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.search.ArtistIndexField;
import org.musicbrainz.search.ArtistType;
import org.musicbrainz.search.ArtistXmlWriter;
import org.musicbrainz.search.Index;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.ResourceType;
import org.musicbrainz.search.Result;
import org.musicbrainz.search.Results;
import org.musicbrainz.search.ResultsWriter;
import org.musicbrainz.search.SearchServer;
import org.musicbrainz.search.analysis.StandardUnaccentAnalyzer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Test retrieving artist from index and Outputting as Xml
 */
public class FindArtistTest extends TestCase {

    private SearchServer ss;

    public FindArtistTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {

        RAMDirectory ramDir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(ramDir, new StandardUnaccentAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);

        //General Purpose Artist
        {
            Document doc = new Document();
            Index.addFieldToDocument(doc, ArtistIndexField.ARTIST_ID, "4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
            Index.addFieldToDocument(doc, ArtistIndexField.ARTIST, "Farming Incident");
            Index.addFieldToDocument(doc, ArtistIndexField.SORTNAME, "Farming Incident");
            Index.addFieldToDocument(doc, ArtistIndexField.BEGIN, "1999-04");
            Index.addFieldToDocument(doc, ArtistIndexField.TYPE, ArtistType.GROUP.getName());
            writer.addDocument(doc);
        }

        //Artist with & on name and aliases
        {
            Document doc = new Document();
            Index.addFieldToDocument(doc, ArtistIndexField.ARTIST_ID, "ccd4879c-5e88-4385-b131-bf65296bf245");
            Index.addFieldToDocument(doc, ArtistIndexField.ARTIST, "Echo & The Bunnymen");
            Index.addFieldToDocument(doc, ArtistIndexField.SORTNAME, "Echo & The Bunnymen");
            Index.addFieldToDocument(doc, ArtistIndexField.BEGIN, "1978");
            Index.addFieldToDocument(doc, ArtistIndexField.TYPE, ArtistType.GROUP.getName());
            Index.addFieldToDocument(doc, ArtistIndexField.ALIAS, "Echo And The Bunnymen");
            Index.addFieldToDocument(doc, ArtistIndexField.ALIAS, "Echo & The Bunnyman");
            Index.addFieldToDocument(doc, ArtistIndexField.ALIAS, "Echo and The Bunymen");
            Index.addFieldToDocument(doc, ArtistIndexField.ALIAS, "Echo & The Bunymen");
            writer.addDocument(doc);
        }

        writer.close();
        Map<ResourceType, IndexSearcher> searchers = new HashMap<ResourceType, IndexSearcher>();
        searchers.put(ResourceType.ARTIST, new IndexSearcher(ramDir));
        ss = new SearchServer(searchers);
    }

    public void testFindArtistByName() throws Exception {
        Results res = ss.search(ResourceType.ARTIST, "artist:\"Farming Incident\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.ARTIST));
        assertEquals("1999-04", doc.get(ArtistIndexField.BEGIN));
        assertNull(doc.get(ArtistIndexField.END));
        assertNull(doc.get(ArtistIndexField.ALIAS));
        assertNull(doc.get(ArtistIndexField.COMMENT));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.SORTNAME));
        assertEquals("group", doc.get(ArtistIndexField.TYPE));
    }


    public void testFindArtistBySortName() throws Exception {
        Results res = ss.search(ResourceType.ARTIST, "sortname:\"Farming Incident\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.ARTIST));
        assertEquals("1999-04", doc.get(ArtistIndexField.BEGIN));
        assertNull(doc.get(ArtistIndexField.END));
        assertNull(doc.get(ArtistIndexField.ALIAS));
        assertNull(doc.get(ArtistIndexField.COMMENT));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.SORTNAME));
        assertEquals("group", doc.get(ArtistIndexField.TYPE));
    }


    public void testFindArtistByType() throws Exception {
        Results res = ss.search(ResourceType.ARTIST, "type:\"group\"", 0, 10);
        assertEquals(2, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.ARTIST));
        assertEquals("1999-04", doc.get(ArtistIndexField.BEGIN));
        assertNull(doc.get(ArtistIndexField.END));
        assertNull(doc.get(ArtistIndexField.ALIAS));
        assertNull(doc.get(ArtistIndexField.COMMENT));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.SORTNAME));
        assertEquals("group", doc.get(ArtistIndexField.TYPE));
    }

    public void testFindArtistByBeginDate() throws Exception {
        Results res = ss.search(ResourceType.ARTIST, "begin:\"1999-04\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.ARTIST));
        assertEquals("1999-04", doc.get(ArtistIndexField.BEGIN));
        assertNull(doc.get(ArtistIndexField.END));
        assertNull(doc.get(ArtistIndexField.ALIAS));
        assertNull(doc.get(ArtistIndexField.COMMENT));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.SORTNAME));
        assertEquals("group", doc.get(ArtistIndexField.TYPE));
    }

    public void testFindArtistByEndDate() throws Exception {
        Results res = ss.search(ResourceType.ARTIST, "end:\"1999-04\"", 0, 10);
        assertEquals(0, res.totalHits);
    }

    public void testFindArtistByTypeNoMatch() throws Exception {
        Results res = ss.search(ResourceType.ARTIST, "type:\"person\"", 0, 10);
        assertEquals(0, res.totalHits);
    }

    public void testFindArtistByAlias() throws Exception {
        Results res = ss.search(ResourceType.ARTIST, "alias:\"Echo And The Bunnymen\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", doc.get(ArtistIndexField.ARTIST_ID));
        assertEquals("Echo & The Bunnymen", doc.get(ArtistIndexField.ARTIST));
        assertEquals("1978", doc.get(ArtistIndexField.BEGIN));
        assertEquals("Echo & The Bunnymen", doc.get(ArtistIndexField.SORTNAME));
        assertEquals("group", doc.get(ArtistIndexField.TYPE));
    }

    public void testFindArtistByDefaultField() throws Exception {
        Results res = ss.search(ResourceType.ARTIST, "\"Echo & The Bunnymen\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", doc.get(ArtistIndexField.ARTIST_ID));
        assertEquals("Echo & The Bunnymen", doc.get(ArtistIndexField.ARTIST));
        assertEquals("1978", doc.get(ArtistIndexField.BEGIN));
        assertEquals("Echo & The Bunnymen", doc.get(ArtistIndexField.SORTNAME));
        assertEquals("group", doc.get(ArtistIndexField.TYPE));
    }

    /**
     * Tests get same results as
     * http://musicbrainz.org/ws/1/artist/?type=xml&query=%22Farming%20Incident%22
     *
     * @throws Exception
     */
    public void testOutputAsXml() throws Exception {

        Results res = ss.search(ResourceType.ARTIST, "artist:\"Farming Incident\"", 0, 1);
        ResultsWriter writer = new ArtistXmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();

        String output = sw.toString();
//      System.out.println("Xml is" + output);
//        assertTrue(output.contains("<artist id=\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\""));  group comes before id in output
//        assertTrue(output.contains("<artist-list count=\"1\" offset=\"0\">"));               offset comes before count in output
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("type=\"Group\""));
        assertTrue(output.contains("<name>Farming Incident</name>"));
        assertTrue(output.contains("<sort-name>Farming Incident</sort-name>"));
        assertTrue(output.contains("<life-span begin=\"1999-04\""));
        assertFalse(output.contains("end"));
        assertFalse(output.contains("alias"));
        assertFalse(output.contains("disambugation"));

    }


    /**
     * Tests that & is converted to valid xml
     *
     * @throws Exception
     */
    public void testOutputAsXmlSpecialCharacters() throws Exception {

        Results res = ss.search(ResourceType.ARTIST, "alias:\"Echo And The Bunnymen\"", 0, 1);
        ResultsWriter writer = new ArtistXmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();

        String output = sw.toString();
        System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("type=\"Group\""));
        assertTrue(output.contains("<name>Echo &amp; The Bunnymen</name>"));
    }


    public void testWritingPerformance() throws Exception {
        Results res = ss.search(ResourceType.ARTIST, "artist:\"Farming Incident\"", 0, 10);
        assertEquals(1, res.totalHits);

        Date start = new Date();
        ResultsWriter writer = new ArtistXmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        for (int i = 0; i < 1000; i++) {
            writer.write(pr, res);
        }
        pr.close();
        Date end = new Date();
        System.out.println("Time Taken:" + (end.getTime() - start.getTime()) + "ms");

    }

}