import junit.framework.TestCase;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.search.FreeDBHtmlWriter;
import org.musicbrainz.search.FreeDBIndexField;
import org.musicbrainz.search.Index;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.ResourceType;
import org.musicbrainz.search.Result;
import org.musicbrainz.search.Results;
import org.musicbrainz.search.ResultsWriter;
import org.musicbrainz.search.SearchServer;
import org.musicbrainz.search.SearchServerServlet;
import org.musicbrainz.search.analysis.StandardUnaccentAnalyzer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Test retrieving FreeDB entries from index and Outputting as Html
 */
public class FindFreeDBTest extends TestCase {

    private SearchServer ss;

    public FindFreeDBTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {

        RAMDirectory ramDir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(ramDir, new StandardUnaccentAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);

        //A complete FreeDB entry
        {
            Document doc = new Document();
            Index.addFieldToDocument(doc, FreeDBIndexField.ARTIST, "Ska-P");
            Index.addFieldToDocument(doc, FreeDBIndexField.TITLE, "Lágrimas & Gozos");
            Index.addFieldToDocument(doc, FreeDBIndexField.CATEGORY, "folk");
            Index.addFieldToDocument(doc, FreeDBIndexField.DISCID, "c20c4b0d");
            Index.addFieldToDocument(doc, FreeDBIndexField.TRACKS, "13");
            Index.addFieldToDocument(doc, FreeDBIndexField.YEAR, "2008");
            writer.addDocument(doc);
        }

        writer.close();
        Map<ResourceType, IndexSearcher> searchers = new HashMap<ResourceType, IndexSearcher>();
        searchers.put(ResourceType.FREEDB, new IndexSearcher(ramDir));
        ss = new SearchServer(searchers);
        
        // Velocity setup
        SearchServerServlet.setUpVelocity();
    }

    public void testSearchFreeDBByArtist() throws Exception {
        Results res = ss.search(ResourceType.FREEDB, "artist:\"Ska-P\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Ska-P", doc.get(FreeDBIndexField.ARTIST));
        assertEquals("Lágrimas & Gozos", doc.get(FreeDBIndexField.TITLE));
        assertEquals("folk", doc.get(FreeDBIndexField.CATEGORY));
        assertEquals("c20c4b0d", doc.get(FreeDBIndexField.DISCID));
        assertEquals("13", doc.get(FreeDBIndexField.TRACKS));
        assertEquals("2008", doc.get(FreeDBIndexField.YEAR));
    }

    public void testSearchFreeDBByTitle() throws Exception {
        Results res = ss.search(ResourceType.FREEDB, "title:\"Lágrimas & Gozos\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Ska-P", doc.get(FreeDBIndexField.ARTIST));
        assertEquals("Lágrimas & Gozos", doc.get(FreeDBIndexField.TITLE));
        assertEquals("folk", doc.get(FreeDBIndexField.CATEGORY));
        assertEquals("c20c4b0d", doc.get(FreeDBIndexField.DISCID));
        assertEquals("13", doc.get(FreeDBIndexField.TRACKS));
        assertEquals("2008", doc.get(FreeDBIndexField.YEAR));
    }

    public void testSearchFreeDBByDiscId() throws Exception {
        Results res = ss.search(ResourceType.FREEDB, "discid:\"c20c4b0d\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Ska-P", doc.get(FreeDBIndexField.ARTIST));
        assertEquals("Lágrimas & Gozos", doc.get(FreeDBIndexField.TITLE));
        assertEquals("folk", doc.get(FreeDBIndexField.CATEGORY));
        assertEquals("c20c4b0d", doc.get(FreeDBIndexField.DISCID));
        assertEquals("13", doc.get(FreeDBIndexField.TRACKS));
        assertEquals("2008", doc.get(FreeDBIndexField.YEAR));
    }

    public void testSearchFreeDBByYear() throws Exception {
        Results res = ss.search(ResourceType.FREEDB, "year:\"2008\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Ska-P", doc.get(FreeDBIndexField.ARTIST));
        assertEquals("Lágrimas & Gozos", doc.get(FreeDBIndexField.TITLE));
        assertEquals("folk", doc.get(FreeDBIndexField.CATEGORY));
        assertEquals("c20c4b0d", doc.get(FreeDBIndexField.DISCID));
        assertEquals("13", doc.get(FreeDBIndexField.TRACKS));
        assertEquals("2008", doc.get(FreeDBIndexField.YEAR));
    }

    public void testFindArtistByDefaultField() throws Exception {
        Results res = ss.search(ResourceType.FREEDB, "\"Ska-P\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Ska-P", doc.get(FreeDBIndexField.ARTIST));
        assertEquals("Lágrimas & Gozos", doc.get(FreeDBIndexField.TITLE));
        assertEquals("folk", doc.get(FreeDBIndexField.CATEGORY));
        assertEquals("c20c4b0d", doc.get(FreeDBIndexField.DISCID));
        assertEquals("13", doc.get(FreeDBIndexField.TRACKS));
        assertEquals("2008", doc.get(FreeDBIndexField.YEAR));
    }

    public void testOutputAsHtml() throws Exception {

        Results res = ss.search(ResourceType.FREEDB, "artist:\"Ska-P\"", 0, 1);
        ResultsWriter writer = new FreeDBHtmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();

        String output = sw.toString();
        assertTrue(output.contains("L&aacute;grimas &amp; Gozos"));
        assertTrue(output.contains("Ska-P"));
        assertTrue(output.contains("c20c4b0d"));

    }

    /**
     * Tests that & is converted to valid html
     *
     * @throws Exception
     */
    public void testOutputAsHtmlSpecialCharacters() throws Exception {

        Results res = ss.search(ResourceType.FREEDB, "artist:\"Ska-P\"", 0, 1);
        ResultsWriter writer = new FreeDBHtmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();

        String output = sw.toString();
        assertTrue(output.contains("L&aacute;grimas &amp; Gozos"));
    }
    
    public void testHtmlWritingPerformance() throws Exception {
        Results res = ss.search(ResourceType.FREEDB, "artist:\"Ska-P\"", 0, 10);
        assertEquals(1, res.totalHits);

        Date start = new Date();
        ResultsWriter writer = new FreeDBHtmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        for (int i = 0; i < 1000; i++) {
            writer.write(pr, res);
        }
        pr.close();
        Date end = new Date();
        System.out.println("HTML - Time Taken: " + (end.getTime() - start.getTime()) + "ms");
    }

}