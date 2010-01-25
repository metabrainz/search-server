package org.musicbrainz.search.servlet;

import junit.framework.TestCase;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;
import org.musicbrainz.search.index.FreeDBIndexField;
import org.musicbrainz.search.servlet.mmd2.*;
import org.musicbrainz.search.servlet.mmd2.ResultsWriter;

import java.io.PrintWriter;
import java.io.StringWriter;

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
        PerFieldAnalyzerWrapper analyzer = new PerFieldEntityAnalyzer(FreeDBIndexField.class);
        IndexWriter writer = new IndexWriter(ramDir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);

        //A complete FreeDB entry
        {
            MbDocument doc = new MbDocument();
            doc.addField(FreeDBIndexField.ARTIST, "Ska-P");
            doc.addField(FreeDBIndexField.TITLE, "L\u00e1grimas & Gozos");
            doc.addField(FreeDBIndexField.CATEGORY, "folk");
            doc.addField(FreeDBIndexField.DISCID, "c20c4b0d");
            doc.addField(FreeDBIndexField.TRACKS, "13");
            doc.addField(FreeDBIndexField.YEAR, "2008");
            writer.addDocument(doc.getLuceneDocument());
        }

        writer.close();
        ss = new FreeDBSearch(new IndexSearcher(ramDir, true));
    }

    public void testSearchFreeDBByArtist() throws Exception {
        Results res = ss.searchLucene("artist:\"Ska-P\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Ska-P", doc.get(FreeDBIndexField.ARTIST));
        assertEquals("L\u00e1grimas & Gozos", doc.get(FreeDBIndexField.TITLE));
        assertEquals("folk", doc.get(FreeDBIndexField.CATEGORY));
        assertEquals("c20c4b0d", doc.get(FreeDBIndexField.DISCID));
        assertEquals("13", doc.get(FreeDBIndexField.TRACKS));
        assertEquals("2008", doc.get(FreeDBIndexField.YEAR));
    }

    public void testSearchFreeDBByTitle() throws Exception {
        Results res = ss.searchLucene("title:\"L\u00e1grimas & Gozos\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Ska-P", doc.get(FreeDBIndexField.ARTIST));
        assertEquals("L\u00e1grimas & Gozos", doc.get(FreeDBIndexField.TITLE));
        assertEquals("folk", doc.get(FreeDBIndexField.CATEGORY));
        assertEquals("c20c4b0d", doc.get(FreeDBIndexField.DISCID));
        assertEquals("13", doc.get(FreeDBIndexField.TRACKS));
        assertEquals("2008", doc.get(FreeDBIndexField.YEAR));
    }

    public void testSearchFreeDBByDiscId() throws Exception {
        Results res = ss.searchLucene("discid:\"c20c4b0d\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Ska-P", doc.get(FreeDBIndexField.ARTIST));
        assertEquals("L\u00e1grimas & Gozos", doc.get(FreeDBIndexField.TITLE));
        assertEquals("folk", doc.get(FreeDBIndexField.CATEGORY));
        assertEquals("c20c4b0d", doc.get(FreeDBIndexField.DISCID));
        assertEquals("13", doc.get(FreeDBIndexField.TRACKS));
        assertEquals("2008", doc.get(FreeDBIndexField.YEAR));
    }

    public void testSearchFreeDBByYear() throws Exception {
        Results res = ss.searchLucene("year:\"2008\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Ska-P", doc.get(FreeDBIndexField.ARTIST));
        assertEquals("L\u00e1grimas & Gozos", doc.get(FreeDBIndexField.TITLE));
        assertEquals("folk", doc.get(FreeDBIndexField.CATEGORY));
        assertEquals("c20c4b0d", doc.get(FreeDBIndexField.DISCID));
        assertEquals("13", doc.get(FreeDBIndexField.TRACKS));
        assertEquals("2008", doc.get(FreeDBIndexField.YEAR));
    }

    public void testSearchFreeDBByDefaultField() throws Exception {

        //by artist
        {
            Results res = ss.searchLucene("\"Ska-P\"", 0, 10);
            assertEquals(1, res.totalHits);
            Result result = res.results.get(0);
            MbDocument doc = result.doc;
            assertEquals("Ska-P", doc.get(FreeDBIndexField.ARTIST));
            assertEquals("L\u00e1grimas & Gozos", doc.get(FreeDBIndexField.TITLE));
            assertEquals("folk", doc.get(FreeDBIndexField.CATEGORY));
            assertEquals("c20c4b0d", doc.get(FreeDBIndexField.DISCID));
            assertEquals("13", doc.get(FreeDBIndexField.TRACKS));
            assertEquals("2008", doc.get(FreeDBIndexField.YEAR));
        }

        //by title
        {
            Results res = ss.searchLucene("\"L\u00e1grimas & Gozos\"", 0, 10);
            assertEquals(1, res.totalHits);
            Result result = res.results.get(0);
            MbDocument doc = result.doc;
            assertEquals("Ska-P", doc.get(FreeDBIndexField.ARTIST));
            assertEquals("L\u00e1grimas & Gozos", doc.get(FreeDBIndexField.TITLE));
            assertEquals("folk", doc.get(FreeDBIndexField.CATEGORY));
            assertEquals("c20c4b0d", doc.get(FreeDBIndexField.DISCID));
            assertEquals("13", doc.get(FreeDBIndexField.TRACKS));
            assertEquals("2008", doc.get(FreeDBIndexField.YEAR));
        }

    }

    /**
     * @throws Exception
     */
    public void testOutputXml() throws Exception {

        Results res = ss.searchLucene("discid:\"c20c4b0d\"", 0, 10);
        org.musicbrainz.search.servlet.mmd2.ResultsWriter writer = new FreeDBWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();

        String output = sw.toString();
        System.out.println("Xml is" + output);
        assertTrue(output.contains("xmlns:ext=\"http://musicbrainz.org/ns/ext#-2.0\""));
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("score=\"100\""));
        assertTrue(output.contains("id=\"c20c4b0d\""));
        assertTrue(output.contains("<title>L\u00e1grimas &amp; Gozos</title>"));
        assertTrue(output.contains("<artist>Ska-P</artist>"));
        assertTrue(output.contains("<year>2008</year>"));
        assertTrue(output.contains("<category>folk</category>"));
        assertTrue(output.contains("<track-list count=\"13\"/>"));

    }

    public void testOutputJson() throws Exception {

        Results res = ss.searchLucene("discid:\"c20c4b0d\"", 0, 10);
        ResultsWriter writer = new FreeDBWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON);
        pr.close();

        String output = sw.toString();
        System.out.println("Json is" + output);

        assertTrue(output.contains("\"count\":1"));
        assertTrue(output.contains("\"offset\":0,"));
        assertTrue(output.contains("\"score\":\"100\","));
        assertTrue(output.contains("\"id\":\"c20c4b0d\""));
        assertTrue(output.contains("\"title\":\"L\u00e1grimas & Gozos\""));
        assertTrue(output.contains("\"artist\":\"Ska-P\""));
        assertTrue(output.contains("\"year\":\"2008\""));
        assertTrue(output.contains("\"category\":\"folk\""));
        assertTrue(output.contains("\"track-list\":{\"count\":13}"));

    }


}