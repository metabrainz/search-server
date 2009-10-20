package org.musicbrainz.search.servlet;
import junit.framework.TestCase;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.velocity.app.Velocity;
import org.musicbrainz.search.index.*;
import org.musicbrainz.search.servlet.MbDocument;
import org.musicbrainz.search.servlet.ReleaseGroupSearch;
import org.musicbrainz.search.servlet.mmd1.ReleaseGroupMmd1XmlWriter;
import org.musicbrainz.search.servlet.Result;
import org.musicbrainz.search.servlet.Results;
import org.musicbrainz.search.servlet.ResultsWriter;
import org.musicbrainz.search.servlet.SearchServer;
import org.musicbrainz.search.servlet.SearchServerServlet;
import org.musicbrainz.search.servlet.mmd2.ReleaseGroupXmlWriter;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Assumes an index has been built stored and in the data folder, I've picked a fairly obscure bside so hopefully
 * will not get added to another release
 */
public class FindReleaseGroupTest extends TestCase {

    private SearchServer ss;


    public FindReleaseGroupTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
         SearchServerServlet.setUpVelocity();
        Velocity.init();
        RAMDirectory ramDir = new RAMDirectory();
        PerFieldAnalyzerWrapper analyzer = new PerFieldEntityAnalyzer(ReleaseGroupIndexField.class);
        IndexWriter writer = new IndexWriter(ramDir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);

        //Release Group with single artist
        Document doc = new Document();
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.RELEASEGROUP_ID, "2c7d81da-8fc3-3157-99c1-e9195ac92c45");
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.RELEASEGROUP, "Nobody's Twisting Your Arm");
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.RELEASE, "secret");

        Index.addFieldToDocument(doc, ReleaseGroupIndexField.TYPE, ReleaseGroupType.SINGLE.getName());
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST_ID, "707622da-475f-48e1-905d-248718df6521");
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST_NAME, "The Wedding Present");
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST, "The Wedding Present");
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST_SORTNAME, "Wedding Present, The");
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST_JOINPHRASE, "-");
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST_NAMECREDIT, "The Wedding Present");
        writer.addDocument(doc);

        //Release Group with multiple Artist and different name credit and no releases
        doc = new Document();
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.RELEASEGROUP_ID, "0011c128-b1f2-300e-88cc-c33c30dce704");
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.RELEASEGROUP, "Epics");


        Index.addFieldToDocument(doc, ReleaseGroupIndexField.TYPE, ReleaseGroupType.ALBUM.getName());
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST_ID, "99845d0c-f239-4051-a6b1-4b5e9f7ede0b");
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST_NAME, "Erich Kunzel");
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST_SORTNAME, "Kunzel, Eric");
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST_JOINPHRASE, "and");
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST_NAMECREDIT, "Erich Kunzel");

        Index.addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST_ID, "d8fbd94c-cd06-4e8b-a559-761ad969d07e");
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST_NAME, "The Cincinnati Pops Orchestra");
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST_SORTNAME, "Cincinnati Pops Orchestra, The");
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST_JOINPHRASE, "-");
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST_NAMECREDIT, "Cincinnati Pops");

        Index.addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST, "Erich Kunzel and Cincinnati Pops");

        writer.addDocument(doc);
        writer.close();
        ss = new ReleaseGroupSearch(new IndexSearcher(ramDir,true));
    }

    public void testFindReleaseGroupById() throws Exception {
        Results res = ss.searchLucene("rgid:\"2c7d81da-8fc3-3157-99c1-e9195ac92c45\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
        assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
        assertEquals("secret", doc.get(ReleaseGroupIndexField.RELEASE));
        assertEquals("707622da-475f-48e1-905d-248718df6521", doc.get(ReleaseGroupIndexField.ARTIST_ID));
        assertEquals("The Wedding Present", doc.get(ReleaseGroupIndexField.ARTIST_NAME));
        assertEquals("single", doc.get(ReleaseGroupIndexField.TYPE));
    }

    public void testFindReleaseGroupByName() throws Exception {
        Results res = ss.searchLucene("releasegroup:\"Nobody's Twisting Your Arm\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
        assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
        assertEquals("707622da-475f-48e1-905d-248718df6521", doc.get(ReleaseGroupIndexField.ARTIST_ID));
        assertEquals("The Wedding Present", doc.get(ReleaseGroupIndexField.ARTIST_NAME));
        assertEquals("single", doc.get(ReleaseGroupIndexField.TYPE));
    }

    public void testFindReleaseGroupByRelease() throws Exception {
        Results res = ss.searchLucene("releasegroup:\"secret\"", 0, 10);
        assertEquals(0, res.totalHits);
        res = ss.searchLucene("release:secret", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
        assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
        assertEquals("707622da-475f-48e1-905d-248718df6521", doc.get(ReleaseGroupIndexField.ARTIST_ID));
        assertEquals("The Wedding Present", doc.get(ReleaseGroupIndexField.ARTIST_NAME));
        assertEquals("single", doc.get(ReleaseGroupIndexField.TYPE));
    }


    //release
    public void testFindReleaseGroupByArtist() throws Exception {
        Results res = ss.searchLucene("artist:\"The Wedding Present\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
        assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
        assertEquals("707622da-475f-48e1-905d-248718df6521", doc.get(ReleaseGroupIndexField.ARTIST_ID));
        assertEquals("The Wedding Present", doc.get(ReleaseGroupIndexField.ARTIST_NAME));
        assertEquals("single", doc.get(ReleaseGroupIndexField.TYPE));
    }


    public void testFindReleaseGroupBySortArtist() throws Exception {
        Results res = ss.searchLucene("sortname:\"Wedding Present, The\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
        assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
        assertEquals("707622da-475f-48e1-905d-248718df6521", doc.get(ReleaseGroupIndexField.ARTIST_ID));
        assertEquals("The Wedding Present", doc.get(ReleaseGroupIndexField.ARTIST_NAME));
        assertEquals("single", doc.get(ReleaseGroupIndexField.TYPE));
    }

    public void testFindReleaseGroupByType() throws Exception {
        Results res = ss.searchLucene("type:\"single\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
        assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
        assertEquals("707622da-475f-48e1-905d-248718df6521", doc.get(ReleaseGroupIndexField.ARTIST_ID));
        assertEquals("The Wedding Present", doc.get(ReleaseGroupIndexField.ARTIST_NAME));
        assertEquals("single", doc.get(ReleaseGroupIndexField.TYPE));
    }

    public void testFindReleaseGroupByNumericType() throws Exception {
           Results res = ss.searchLucene("type:2", 0, 10);
           assertEquals(1, res.totalHits);
           Result result = res.results.get(0);
           MbDocument doc = result.doc;
           assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
           assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
           assertEquals("707622da-475f-48e1-905d-248718df6521", doc.get(ReleaseGroupIndexField.ARTIST_ID));
           assertEquals("The Wedding Present", doc.get(ReleaseGroupIndexField.ARTIST_NAME));
           assertEquals("single", doc.get(ReleaseGroupIndexField.TYPE));
       }

    public void testFindReleaseGroupByDefault() throws Exception {
        Results res = ss.searchLucene("\"secret\"", 0, 10);
        assertEquals(0, res.totalHits);
        res = ss.searchLucene("\"Nobody's Twisting Your Arm\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
        assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
        assertEquals("707622da-475f-48e1-905d-248718df6521", doc.get(ReleaseGroupIndexField.ARTIST_ID));
        assertEquals("The Wedding Present", doc.get(ReleaseGroupIndexField.ARTIST_NAME));
        assertEquals("single", doc.get(ReleaseGroupIndexField.TYPE));
        assertEquals("secret", doc.get(ReleaseGroupIndexField.RELEASE));
                
    }


    public void testFindReleaseGroupByArtist2() throws Exception {
        Results res = ss.searchLucene("artist:\"Erich Kunzel\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("0011c128-b1f2-300e-88cc-c33c30dce704", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
        assertEquals("Epics", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
        assertEquals("Erich Kunzel and Cincinnati Pops",doc.get(ReleaseGroupIndexField.ARTIST));

    }

    public void testFindReleaseGroupByAllArtist2() throws Exception {
            Results res = ss.searchLucene("artist:\"Erich Kunzel and Cincinnati Pops\"", 0, 10);
            assertEquals(1, res.totalHits);
            Result result = res.results.get(0);
            MbDocument doc = result.doc;
            assertEquals("0011c128-b1f2-300e-88cc-c33c30dce704", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
            assertEquals("Epics", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
            assertEquals("Erich Kunzel and Cincinnati Pops",doc.get(ReleaseGroupIndexField.ARTIST));
    }


    public void testFindReleaseGroupByArtistName() throws Exception {
        Results res = ss.searchLucene("artistname:\"Erich Kunzel\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("0011c128-b1f2-300e-88cc-c33c30dce704", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
        assertEquals("Epics", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
        assertEquals("Erich Kunzel and Cincinnati Pops",doc.get(ReleaseGroupIndexField.ARTIST));

    }

    public void testFindReleaseGroupByAllArtistName() throws Exception {
        Results res = ss.searchLucene("artistname:\"Erich Kunzel\" AND artistname:\"Cincinnati Pops\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("0011c128-b1f2-300e-88cc-c33c30dce704", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
        assertEquals("Epics", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
        assertEquals("Erich Kunzel and Cincinnati Pops",doc.get(ReleaseGroupIndexField.ARTIST));

    }
    public void testFindReleaseGroupBySortArtist2() throws Exception {
        Results res = ss.searchLucene("sortname:\"Kunzel, Eric\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("0011c128-b1f2-300e-88cc-c33c30dce704", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
        assertEquals("Epics", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
        assertEquals("Erich Kunzel and Cincinnati Pops",doc.get(ReleaseGroupIndexField.ARTIST));

    }


    /**
     * Tests get same results as
     * http://musicbrainz.org/ws/1/release-group/?type=xml&query=%22Nobody%27s%20Twisting%20Your%20Arm%22
     *
     * @throws Exception
     */
    public void testOutputAsAsMmd1Xml() throws Exception {

        Results res = ss.searchLucene("releasegroup:\"Nobody's Twisting Your Arm\"", 0, 1);
        ResultsWriter writer = new ReleaseGroupMmd1XmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();
        String output = sw.toString();
        //System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("id=\"2c7d81da-8fc3-3157-99c1-e9195ac92c45\""));
        assertTrue(output.contains("<title>Nobody's Twisting Your Arm</title>"));
        assertTrue(output.contains("<name>The Wedding Present</name>"));
        assertTrue(output.contains("<sort-name>Wedding Present, The</sort-name>"));
        assertTrue(output.contains("artist id=\"707622da-475f-48e1-905d-248718df6521\""));
        assertTrue(output.contains("type=\"Single\""));


    }

     /**
     * Tests get same results as
     * http://musicbrainz.org/ws/1/release-group/?type=xml&query=%22Nobody%27s%20Twisting%20Your%20Arm%22
     *
     * @throws Exception
     */
    public void testOutputAsAsXml() throws Exception {

        Results res = ss.searchLucene("releasegroup:\"Nobody's Twisting Your Arm\"", 0, 1);
        ResultsWriter writer = new ReleaseGroupXmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();
        String output = sw.toString();
        System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("id=\"2c7d81da-8fc3-3157-99c1-e9195ac92c45\""));
        assertTrue(output.contains("<title>Nobody's Twisting Your Arm</title>"));
        assertTrue(output.contains("<name>The Wedding Present</name>"));
        assertTrue(output.contains("<sort-name>Wedding Present, The</sort-name>"));
        assertTrue(output.contains("<name-credit>"));
        assertTrue(output.contains("<artist-credit>"));
        assertTrue(output.contains("artist id=\"707622da-475f-48e1-905d-248718df6521\""));
        assertTrue(output.contains("type=\"Single\""));
        assertTrue(output.contains("release-list count=\"1\""));


    }


    /**
     * Tests get same results as
     * http://musicbrainz.org/ws/1/release-group/?type=xml&query=%22Nobody%27s%20Twisting%20Your%20Arm%22
     *
     * @throws Exception
     */
    public void testOutputAsAsMmd1Xml2() throws Exception {

        Results res = ss.searchLucene("releasegroup:Epics", 0, 1);
        ResultsWriter writer = new ReleaseGroupMmd1XmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();
        String output = sw.toString();
        //System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("id=\"0011c128-b1f2-300e-88cc-c33c30dce704\""));
        assertTrue(output.contains("<title>Epics</title>"));
        assertTrue(output.contains("<name>Erich Kunzel</name>"));
        assertTrue(output.contains("<sort-name>Kunzel, Eric</sort-name>"));
        assertTrue(output.contains("artist id=\"99845d0c-f239-4051-a6b1-4b5e9f7ede0b\""));
        assertTrue(output.contains("type=\"Album\""));


    }

     /**
     *
     * @throws Exception
     */
    public void testOutputAsAsXml2() throws Exception {

        Results res = ss.searchLucene("releasegroup:Epics", 0, 1);
        ResultsWriter writer = new ReleaseGroupXmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();
        String output = sw.toString();
        System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("id=\"0011c128-b1f2-300e-88cc-c33c30dce704\""));
        assertTrue(output.contains("type=\"Album\""));
        assertTrue(output.contains("<title>Epics</title>"));
        assertTrue(output.contains("<name-credit joinphrase=\"and\">"));
        assertTrue(output.contains("<name>Erich Kunzel</name>"));
        assertTrue(output.contains("<sort-name>Kunzel, Eric</sort-name>"));
        assertTrue(output.contains("<name-credit>"));
        assertTrue(output.contains("<artist-credit>"));
        assertTrue(output.contains("artist id=\"99845d0c-f239-4051-a6b1-4b5e9f7ede0b\""));
        assertTrue(output.contains("<name-credit><name>Cincinnati Pops</name>"));
        assertTrue(output.contains("<name>The Cincinnati Pops Orchestra</name>"));
        assertTrue(output.contains("<sort-name>Cincinnati Pops Orchestra, The</sort-name>"));
        assertTrue(output.contains("artist id=\"d8fbd94c-cd06-4e8b-a559-761ad969d07e\""));
        assertTrue(output.contains("release-list count=\"0\""));


    }
}