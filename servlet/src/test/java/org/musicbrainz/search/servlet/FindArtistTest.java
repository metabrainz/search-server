package org.musicbrainz.search.servlet;
import junit.framework.TestCase;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.velocity.app.Velocity;
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.index.ArtistType;
import org.musicbrainz.search.servlet.ArtistHtmlWriter;
import org.musicbrainz.search.servlet.ArtistSearch;
import org.musicbrainz.search.servlet.mmd1.ArtistMmd1XmlWriter;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.servlet.Result;
import org.musicbrainz.search.servlet.Results;
import org.musicbrainz.search.servlet.ResultsWriter;
import org.musicbrainz.search.servlet.SearchServer;
import org.musicbrainz.search.servlet.SearchServerServlet;
import org.musicbrainz.search.servlet.mmd1.Mmd1XmlWriter;
import org.musicbrainz.search.servlet.mmd2.ArtistXmlWriter;
import org.musicbrainz.search.servlet.mmd2.XmlWriter;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

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
        SearchServerServlet.setUpVelocity();
        Velocity.init();
        RAMDirectory ramDir = new RAMDirectory();
        PerFieldAnalyzerWrapper analyzer = new PerFieldEntityAnalyzer(ArtistIndexField.class);
        IndexWriter writer = new IndexWriter(ramDir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);

        //General Purpose Artist
        {
            MbDocument doc = new MbDocument();
            doc.addField(ArtistIndexField.ARTIST_ID, "4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
            doc.addField(ArtistIndexField.ARTIST, "Farming Incident");
            doc.addField(ArtistIndexField.SORTNAME, "Farming Incident");
            doc.addField(ArtistIndexField.BEGIN, "1999-04");
            doc.addField(ArtistIndexField.TYPE, ArtistType.GROUP.getName());
            doc.addField(ArtistIndexField.COMMENT, "the real one");
            doc.addField(ArtistIndexField.COUNTRY, "af");
            doc.addField(ArtistIndexField.GENDER, "male");
            writer.addDocument(doc.getLuceneDocument());
        }

        //Artist with & on name and aliases
        {
            MbDocument doc = new MbDocument();
            doc.addField(ArtistIndexField.ARTIST_ID, "ccd4879c-5e88-4385-b131-bf65296bf245");
            doc.addField(ArtistIndexField.ARTIST, "Echo & The Bunnymen");
            doc.addField(ArtistIndexField.SORTNAME, "Echo & The Bunnymen");
            doc.addField(ArtistIndexField.BEGIN, "1978");
            doc.addField(ArtistIndexField.TYPE, ArtistType.GROUP.getName());
            doc.addField(ArtistIndexField.ALIAS, "Echo And The Bunnymen");
            doc.addField(ArtistIndexField.ALIAS, "Echo & The Bunnyman");
            doc.addField(ArtistIndexField.ALIAS, "Echo and The Bunymen");
            doc.addField(ArtistIndexField.ALIAS, "Echo & The Bunymen");
            writer.addDocument(doc.getLuceneDocument());
        }
        writer.close();
        ss = new ArtistSearch(new IndexSearcher(ramDir,true));
    }

    public void testFindArtistById() throws Exception {
        Results res = ss.searchLucene("arid:\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.ARTIST));
        assertEquals("1999-04", doc.get(ArtistIndexField.BEGIN));
        assertEquals("the real one", doc.get(ArtistIndexField.COMMENT));
        assertNull(doc.get(ArtistIndexField.END));
        assertNull(doc.get(ArtistIndexField.ALIAS));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.SORTNAME));
        assertEquals("group", doc.get(ArtistIndexField.TYPE));
    }

    public void testFindArtistByName() throws Exception {
        Results res = ss.searchLucene("artist:\"Farming Incident\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.ARTIST));
        assertEquals("1999-04", doc.get(ArtistIndexField.BEGIN));
        assertEquals("the real one", doc.get(ArtistIndexField.COMMENT));
        assertNull(doc.get(ArtistIndexField.END));
        assertNull(doc.get(ArtistIndexField.ALIAS));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.SORTNAME));
        assertEquals("group", doc.get(ArtistIndexField.TYPE));
    }


    public void testFindArtistBySortName() throws Exception {
        Results res = ss.searchLucene("sortname:\"Farming Incident\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.ARTIST));
        assertEquals("1999-04", doc.get(ArtistIndexField.BEGIN));
        assertEquals("the real one", doc.get(ArtistIndexField.COMMENT));
        assertNull(doc.get(ArtistIndexField.END));
        assertNull(doc.get(ArtistIndexField.ALIAS));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.SORTNAME));
        assertEquals("group", doc.get(ArtistIndexField.TYPE));
    }


    public void testFindArtistByType() throws Exception {
        Results res = ss.searchLucene("type:\"group\"", 0, 10);
        assertEquals(2, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.ARTIST));
        assertEquals("1999-04", doc.get(ArtistIndexField.BEGIN));
        assertEquals("the real one", doc.get(ArtistIndexField.COMMENT));
        assertNull(doc.get(ArtistIndexField.END));
        assertNull(doc.get(ArtistIndexField.ALIAS));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.SORTNAME));
        assertEquals("group", doc.get(ArtistIndexField.TYPE));
    }

    public void testFindArtistByNumericType() throws Exception {
        Results res = ss.searchLucene("type:2", 0, 10);
        assertEquals(2, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.ARTIST));
        assertEquals("1999-04", doc.get(ArtistIndexField.BEGIN));
        assertEquals("the real one", doc.get(ArtistIndexField.COMMENT));
        assertNull(doc.get(ArtistIndexField.END));
        assertNull(doc.get(ArtistIndexField.ALIAS));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.SORTNAME));
        assertEquals("group", doc.get(ArtistIndexField.TYPE));
    }


    public void testFindArtistByBeginDate() throws Exception {
        Results res = ss.searchLucene("begin:\"1999-04\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.ARTIST));
        assertEquals("1999-04", doc.get(ArtistIndexField.BEGIN));
        assertEquals("the real one", doc.get(ArtistIndexField.COMMENT));
        assertNull(doc.get(ArtistIndexField.END));
        assertNull(doc.get(ArtistIndexField.ALIAS));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.SORTNAME));
        assertEquals("group", doc.get(ArtistIndexField.TYPE));
    }

    public void testFindArtistByEndDate() throws Exception {
        Results res = ss.searchLucene("end:\"1999-04\"", 0, 10);
        assertEquals(0, res.totalHits);
    }

    public void testFindArtistByTypeNoMatch() throws Exception {
        Results res = ss.searchLucene("type:\"person\"", 0, 10);
        assertEquals(0, res.totalHits);
    }

    public void testFindArtistByAlias() throws Exception {
        Results res = ss.searchLucene("alias:\"Echo And The Bunnymen\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", doc.get(ArtistIndexField.ARTIST_ID));
        assertEquals("Echo & The Bunnymen", doc.get(ArtistIndexField.ARTIST));
        assertEquals("1978", doc.get(ArtistIndexField.BEGIN));
        assertEquals("Echo & The Bunnymen", doc.get(ArtistIndexField.SORTNAME));
        assertEquals("group", doc.get(ArtistIndexField.TYPE));
    }

    public void testFindArtistByCountry() throws Exception {
        Results res = ss.searchLucene("country:\"AF\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.ARTIST));
    }

    public void testFindArtistByGenderLowercase() throws Exception {
        Results res = ss.searchLucene("gender:\"male\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.ARTIST));
    }

    public void testFindArtistByGenderTitlecase() throws Exception {
        Results res = ss.searchLucene("gender:\"Male\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.ARTIST));
    }

    public void testFindArtistByDefaultField() throws Exception {

        //Matches on name field without it being specified
        {
            Results res = ss.searchLucene("\"Echo & The Bunnymen\"", 0, 10);
            assertEquals(1, res.totalHits);
            Result result = res.results.get(0);
            MbDocument doc = result.doc;
            assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", doc.get(ArtistIndexField.ARTIST_ID));
            assertEquals("Echo & The Bunnymen", doc.get(ArtistIndexField.ARTIST));
            assertEquals("1978", doc.get(ArtistIndexField.BEGIN));
            assertEquals("Echo & The Bunnymen", doc.get(ArtistIndexField.SORTNAME));
            assertEquals("group", doc.get(ArtistIndexField.TYPE));
        }

        //and alias field  field without it being specified
        {
            Results res = ss.searchLucene("\"Echo & The Bunnyman\"", 0, 10);
            assertEquals(1, res.totalHits);
            Result result = res.results.get(0);
            MbDocument doc = result.doc;
            assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", doc.get(ArtistIndexField.ARTIST_ID));
            assertEquals("Echo & The Bunnymen", doc.get(ArtistIndexField.ARTIST));
            assertEquals("1978", doc.get(ArtistIndexField.BEGIN));
            assertEquals("Echo & The Bunnymen", doc.get(ArtistIndexField.SORTNAME));
            assertEquals("group", doc.get(ArtistIndexField.TYPE));
        }

        //but doesn't search default fields if a field is specified 
        {
            Results res = ss.searchLucene("type:\"Echo & The Bunnyman\"", 0, 10);
            assertEquals(0, res.totalHits);

        }
    }

    /**
     * Tests get same results as
     * http://musicbrainz.org/ws/1/artist/?type=xml&query=%22Farming%20Incident%22
     *
     * @throws Exception
     */
    public void testOutputAsMmd1Xml() throws Exception {

        Results res = ss.searchLucene("artist:\"Farming Incident\"", 0, 1);
        Mmd1XmlWriter v1Writer = new ArtistMmd1XmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        v1Writer.write(pr, res);
        pr.close();

        String output = sw.toString();
        System.out.println("Xml is" + output);
        //assertTrue(output.contains("<artist id=\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\""));  group comes before id in output
        //assertTrue(output.contains("<artist-list count=\"1\" offset=\"0\">"));               offset comes before count in output
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
    
     *
     * @throws Exception
     */
    public void testOutputXml() throws Exception {

        Results res = ss.searchLucene("artist:\"Farming Incident\"", 0, 1);
        XmlWriter v1Writer = new ArtistXmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        v1Writer.write(pr, res);
        pr.close();

        String output = sw.toString();
        System.out.println("Xml is" + output);
        //assertTrue(output.contains("<artist id=\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\""));  group comes before id in output
        //assertTrue(output.contains("<artist-list count=\"1\" offset=\"0\">"));               offset comes before count in output
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("type=\"group\""));
        assertTrue(output.contains("<name>Farming Incident</name>"));
        assertTrue(output.contains("<sort-name>Farming Incident</sort-name>"));
        assertTrue(output.contains("<life-span><begin>1999-04</begin></life-span>"));
        assertTrue(output.contains("<country>af</country>"));
        assertTrue(output.contains("<gender>male</gender>"));
        assertFalse(output.contains("alias"));
        assertFalse(output.contains("disambugation"));
    }

    /**

     *
     * @throws Exception
     */
    public void testOutputXml2() throws Exception {

        Results res = ss.searchLucene("artist:\"Echo & the Bunnymen\"", 0, 1);
        XmlWriter v1Writer = new ArtistXmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        v1Writer.write(pr, res);
        pr.close();
        String output = sw.toString();
        System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("type=\"group\""));
        assertTrue(output.contains("<name>Echo &amp; The Bunnymen</name>"));
        assertTrue(output.contains("<sort-name>Echo &amp; The Bunnymen</sort-name>"));
        assertTrue(output.contains("<life-span><begin>1978</begin></life-span>"));
        assertTrue(output.contains("<alias>Echo And The Bunnymen</alias>"));
        assertTrue(output.contains("<alias>Echo &amp; The Bunnyman</alias>"));
        assertTrue(output.contains("<alias>Echo and The Bunymen</alias>"));
        assertTrue(output.contains("<alias>Echo &amp; The Bunymen</alias>"));
    }


    public void testOutputAsHtml() throws Exception {

        Results res = ss.searchLucene("artist:\"Farming Incident\"", 0, 1);
        ResultsWriter writer = new ArtistHtmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();

        String output = sw.toString();
//      System.out.println("Xml is" + output);
//        assertTrue(output.contains("<artist id=\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\""));  group comes before id in output
//        assertTrue(output.contains("<artist-list count=\"1\" offset=\"0\">"));               offset comes before count in output
        assertTrue(output.contains("group"));
        assertTrue(output.contains("Farming Incident"));
        assertTrue(output.contains("1999-04"));
        assertTrue(output.contains("the real one"));
        assertFalse(output.contains("end"));
        assertFalse(output.contains("alias"));
        assertFalse(output.contains("disambugation"));

    }

    /**
     * Tests that & is converted to valid xml
     *
     * @throws Exception
     */
    public void testOutputAsMmd1XmlSpecialCharacters() throws Exception {

        Results res = ss.searchLucene("alias:\"Echo And The Bunnymen\"", 0, 1);
        Mmd1XmlWriter v1Writer = new ArtistMmd1XmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        v1Writer.write(pr, res);
        pr.close();

        String output = sw.toString();
        //System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("type=\"Group\""));
        assertTrue(output.contains("<name>Echo &amp; The Bunnymen</name>"));
    }

    /**
     * Tests that & is converted to valid html
     *
     * @throws Exception
     */
    public void testOutputAsHtmlSpecialCharacters() throws Exception {

        Results res = ss.searchLucene("alias:\"Echo And The Bunnymen\"", 0, 1);
        ResultsWriter writer = new ArtistHtmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();

        String output = sw.toString();
        assertTrue(output.contains("group"));
        assertTrue(output.contains("Echo &amp; The Bunnymen"));
        //System.out.println(output);
    }

    public void testXmlWritingPerformance() throws Exception {
        Results res = ss.searchLucene("artist:\"Farming Incident\"", 0, 10);
        assertEquals(1, res.totalHits);

        Date start = new Date();
        ResultsWriter writer = new ArtistMmd1XmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        for (int i = 0; i < 1000; i++) {
            writer.write(pr, res);
        }
        pr.close();
        Date end = new Date();
        System.out.println("XML - Time Taken: " + (end.getTime() - start.getTime()) + "ms");
    }

    public void testHtmlWritingPerformance() throws Exception {
        Results res = ss.searchLucene("artist:\"Farming Incident\"", 0, 10);
        assertEquals(1, res.totalHits);

        Date start = new Date();
        ResultsWriter writer = new ArtistHtmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        for (int i = 0; i < 1000; i++) {
            writer.write(pr, res);
        }
        pr.close();
        Date end = new Date();
        System.out.println("HTML - Time Taken: " + (end.getTime() - start.getTime()) + "ms");
    }


    /**
     * Tests strips off header and footer
     * http://musicbrainz.org/ws/1/artist/?type=xml&query=%22Farming%20Incident%22
     *
     * @throws Exception
     */
    /*
    public void testOutputAsXmlFragment() throws Exception {

        Results res = ss.searchLucene("artist:\"Farming Incident\"", 0, 1);
        Mmd1XmlWriter writer = new ArtistMmd1XmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();

        String output = sw.toString();
        assertFalse(output.contains("metadata"));
        assertFalse(output.contains("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"));
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("type=\"Group\""));
        assertTrue(output.contains("<name>Farming Incident</name>"));
        assertTrue(output.contains("<sort-name>Farming Incident</sort-name>"));
        assertTrue(output.contains("<life-span begin=\"1999-04\""));
        assertFalse(output.contains("end"));
        assertFalse(output.contains("alias"));
        assertFalse(output.contains("disambugation"));
        System.out.println("Xml Fragment is" + output);
    }
    */
}