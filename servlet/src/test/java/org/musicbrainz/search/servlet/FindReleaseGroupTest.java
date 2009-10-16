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
        PerFieldAnalyzerWrapper analyzer = new ReleaseGroupAnalyzer();
        IndexWriter writer = new IndexWriter(ramDir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);

        Document doc = new Document();
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.RELEASEGROUP_ID, "2c7d81da-8fc3-3157-99c1-e9195ac92c45");
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.RELEASEGROUP, "Nobody's Twisting Your Arm");
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.RELEASES, "secret");

        Index.addFieldToDocument(doc, ReleaseGroupIndexField.TYPE, ReleaseGroupType.SINGLE.getName());
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST_ID, "707622da-475f-48e1-905d-248718df6521");
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST, "The Wedding Present");
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST_V1, "The Wedding Present");
        Index.addFieldToDocument(doc, ReleaseIndexField.ARTIST_SORTNAME, "Wedding Present, The");
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
        assertEquals("707622da-475f-48e1-905d-248718df6521", doc.get(ReleaseGroupIndexField.ARTIST_ID));
        assertEquals("The Wedding Present", doc.get(ReleaseGroupIndexField.ARTIST));
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
        assertEquals("The Wedding Present", doc.get(ReleaseGroupIndexField.ARTIST));
        assertEquals("single", doc.get(ReleaseGroupIndexField.TYPE));
    }

    public void testFindReleaseGroupByReleases() throws Exception {
        Results res = ss.searchLucene("releasegroup:\"secret\"", 0, 10);
        assertEquals(0, res.totalHits);
        res = ss.searchLucene("releases:\"secret\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
        assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
        assertEquals("707622da-475f-48e1-905d-248718df6521", doc.get(ReleaseGroupIndexField.ARTIST_ID));
        assertEquals("The Wedding Present", doc.get(ReleaseGroupIndexField.ARTIST));
        assertEquals("single", doc.get(ReleaseGroupIndexField.TYPE));
    }

    public void testFindReleaseGroupByArtist() throws Exception {
        Results res = ss.searchLucene("artist:\"The Wedding Present\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
        assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
        assertEquals("707622da-475f-48e1-905d-248718df6521", doc.get(ReleaseGroupIndexField.ARTIST_ID));
        assertEquals("The Wedding Present", doc.get(ReleaseGroupIndexField.ARTIST));
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
        assertEquals("The Wedding Present", doc.get(ReleaseGroupIndexField.ARTIST));
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
        assertEquals("The Wedding Present", doc.get(ReleaseGroupIndexField.ARTIST));
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
           assertEquals("The Wedding Present", doc.get(ReleaseGroupIndexField.ARTIST));
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
        assertEquals("The Wedding Present", doc.get(ReleaseGroupIndexField.ARTIST));
        assertEquals("single", doc.get(ReleaseGroupIndexField.TYPE));
    }


    /**
     * Tests get same results as
     * http://musicbrainz.org/ws/1/release-group/?type=xml&query=%22Nobody%27s%20Twisting%20Your%20Arm%22
     *
     * @throws Exception
     */
    public void testOutputAsXml() throws Exception {

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
}