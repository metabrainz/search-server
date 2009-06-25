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
public class FindReleaseGroupTest extends TestCase {

    private SearchServer ss;


    public FindReleaseGroupTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        RAMDirectory ramDir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(ramDir, new StandardUnaccentAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);

        Document doc = new Document();
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.RELEASEGROUP_ID, "2c7d81da-8fc3-3157-99c1-e9195ac92c45");
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.RELEASEGROUP, "Nobody's Twisting Your Arm");
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.TYPE, ReleaseGroupType.SINGLE.getName());
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST_ID, "707622da-475f-48e1-905d-248718df6521");
        Index.addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST, "The Wedding Present");
        writer.addDocument(doc);
        writer.close();
        Map<ResourceType, IndexSearcher> searchers = new HashMap<ResourceType, IndexSearcher>();
        searchers.put(ResourceType.RELEASE_GROUP, new IndexSearcher(ramDir));
        ss = new SearchServer(searchers);
    }

    public void testFindReleaseGroupByName() throws Exception {
        Results res = ss.search(ResourceType.RELEASE_GROUP, "releasegroup:\"Nobody's Twisting Your Arm\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        Document doc = result.doc;
        assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID.getName()));
        assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP.getName()));
        assertEquals("707622da-475f-48e1-905d-248718df6521",doc.get(ReleaseGroupIndexField.ARTIST_ID.getName()));
        assertEquals("The Wedding Present", doc.get(ReleaseGroupIndexField.ARTIST.getName()));
        assertEquals("single", doc.get(ReleaseGroupIndexField.TYPE.getName()));
    }

    public void testFindReleaseGroupByArtist() throws Exception {
        Results res = ss.search(ResourceType.RELEASE_GROUP, "artist:\"The Wedding Present\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        Document doc = result.doc;
        assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID.getName()));
        assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP.getName()));
        assertEquals("707622da-475f-48e1-905d-248718df6521",doc.get(ReleaseGroupIndexField.ARTIST_ID.getName()));
        assertEquals("The Wedding Present", doc.get(ReleaseGroupIndexField.ARTIST.getName()));
        assertEquals("single", doc.get(ReleaseGroupIndexField.TYPE.getName()));
    }


    public void testFindReleaseGroupByType() throws Exception {
        Results res = ss.search(ResourceType.RELEASE_GROUP, "type:\"single\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        Document doc = result.doc;
        assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID.getName()));
        assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP.getName()));
        assertEquals("707622da-475f-48e1-905d-248718df6521",doc.get(ReleaseGroupIndexField.ARTIST_ID.getName()));
        assertEquals("The Wedding Present", doc.get(ReleaseGroupIndexField.ARTIST.getName()));
        assertEquals("single", doc.get(ReleaseGroupIndexField.TYPE.getName()));
    }

    public void testFindReleaseGroupByDefault() throws Exception {
        Results res = ss.search(ResourceType.RELEASE_GROUP, "\"Nobody's Twisting Your Arm\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        Document doc = result.doc;
        assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID.getName()));
        assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP.getName()));
        assertEquals("707622da-475f-48e1-905d-248718df6521",doc.get(ReleaseGroupIndexField.ARTIST_ID.getName()));
        assertEquals("The Wedding Present", doc.get(ReleaseGroupIndexField.ARTIST.getName()));
        assertEquals("single", doc.get(ReleaseGroupIndexField.TYPE.getName()));
    }



    /**
     * Tests get same results as
     * http://musicbrainz.org/ws/1/release-group/?type=xml&query=%22Nobody%27s%20Twisting%20Your%20Arm%22
     *
     * @throws Exception
     */
    public void testOutputAsXml() throws Exception {

        Results res = ss.search(ResourceType.RELEASE_GROUP, "releasegroup:\"Nobody's Twisting Your Arm\"", 0, 1);
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
        assertTrue(output.contains("artist id=\"707622da-475f-48e1-905d-248718df6521\""));
        assertTrue(output.contains("type=\"Single\""));


    }
}