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
public class FindLabelTest extends TestCase {

    private SearchServer ss;


    public FindLabelTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        RAMDirectory ramDir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(ramDir, new StandardUnaccentAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);

        LabelIndex li = new LabelIndex();
        Document doc = new Document();
        li.addLabelGidToDocument(doc, "ff571ff4-04cb-4b9c-8a1c-354c330f863c");
        li.addLabelToDocument(doc, "Jockey Slut");
        li.addSortNameToDocument(doc, "Jockey Slut");
        li.addBeginDateToDocument(doc, "1993");
        li.addEndDateToDocument(doc, "2004");
        li.addTypeToDocument(doc, LabelType.PRODUCTION);
        li.addCountryToDocument(doc, "GB");
        writer.addDocument(doc);
        writer.close();
        Map<String, IndexSearcher> searchers = new HashMap<String, IndexSearcher>();
        searchers.put("label", new IndexSearcher(ramDir));
        ss = new SearchServer(searchers);
    }

    public void testFindLabelByName() throws Exception {
        Results res = ss.search("label", "label:\"Jockey Slut\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        Document doc = result.doc;
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(LabelIndexFieldName.LABEL_ID.getFieldname()));
        assertEquals("Jockey Slut", doc.get(LabelIndexFieldName.LABEL.getFieldname()));
        assertEquals("1993", doc.get(LabelIndexFieldName.BEGIN.getFieldname()));
        assertEquals("2004", doc.get(LabelIndexFieldName.END.getFieldname()));
        assertNull(doc.get(LabelIndexFieldName.ALIAS.getFieldname()));
        assertNull(doc.get(LabelIndexFieldName.COMMENT.getFieldname()));
        assertEquals("Jockey Slut", doc.get(LabelIndexFieldName.SORTNAME.getFieldname()));
        assertEquals("production", doc.get(LabelIndexFieldName.TYPE.getFieldname()));
    }


    public void testFindLabelByType() throws Exception {
        Results res = ss.search("label", "type:\"production\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        Document doc = result.doc;
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(LabelIndexFieldName.LABEL_ID.getFieldname()));
        assertEquals("Jockey Slut", doc.get(LabelIndexFieldName.LABEL.getFieldname()));
        assertEquals("1993", doc.get(LabelIndexFieldName.BEGIN.getFieldname()));
        assertEquals("2004", doc.get(LabelIndexFieldName.END.getFieldname()));
        assertNull(doc.get(LabelIndexFieldName.ALIAS.getFieldname()));
        assertNull(doc.get(LabelIndexFieldName.COMMENT.getFieldname()));
        assertEquals("Jockey Slut", doc.get(LabelIndexFieldName.SORTNAME.getFieldname()));
        assertEquals("production", doc.get(LabelIndexFieldName.TYPE.getFieldname()));
    }

    public void testFindLabelBySortname() throws Exception {
        Results res = ss.search("label", "sortname:\"Jockey Slut\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        Document doc = result.doc;
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(LabelIndexFieldName.LABEL_ID.getFieldname()));
        assertEquals("Jockey Slut", doc.get(LabelIndexFieldName.LABEL.getFieldname()));
        assertEquals("1993", doc.get(LabelIndexFieldName.BEGIN.getFieldname()));
        assertEquals("2004", doc.get(LabelIndexFieldName.END.getFieldname()));
        assertNull(doc.get(LabelIndexFieldName.ALIAS.getFieldname()));
        assertNull(doc.get(LabelIndexFieldName.COMMENT.getFieldname()));
        assertEquals("Jockey Slut", doc.get(LabelIndexFieldName.SORTNAME.getFieldname()));
        assertEquals("production", doc.get(LabelIndexFieldName.TYPE.getFieldname()));
    }

    public void testFindLabelByCountry() throws Exception {
        Results res = ss.search("label", "country:\"gb\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        Document doc = result.doc;
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(LabelIndexFieldName.LABEL_ID.getFieldname()));
        assertEquals("Jockey Slut", doc.get(LabelIndexFieldName.LABEL.getFieldname()));
        assertEquals("1993", doc.get(LabelIndexFieldName.BEGIN.getFieldname()));
        assertEquals("2004", doc.get(LabelIndexFieldName.END.getFieldname()));
        assertNull(doc.get(LabelIndexFieldName.ALIAS.getFieldname()));
        assertNull(doc.get(LabelIndexFieldName.COMMENT.getFieldname()));
        assertEquals("Jockey Slut", doc.get(LabelIndexFieldName.SORTNAME.getFieldname()));
        assertEquals("production", doc.get(LabelIndexFieldName.TYPE.getFieldname()));
    }

    /**
     * Tests get same results as
     * http://musicbrainz.org/ws/1/label/?type=xml&query=%22Jockey%20Slut%22
     *
     * @throws Exception
     */
    public void testOutputAsXml() throws Exception {

        Results res = ss.search("label", "label:\"Jockey Slut\"", 0, 1);
        ResultsWriter writer = new LabelXmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();
        String output = sw.toString();
        //System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("id=\"ff571ff4-04cb-4b9c-8a1c-354c330f863c\""));
        assertTrue(output.contains("type=\"Production\""));
        assertTrue(output.contains("<name>Jockey Slut</name>"));
        assertTrue(output.contains("<sort-name>Jockey Slut</sort-name>"));
        assertTrue(output.contains("begin=\"1993\""));
        assertTrue(output.contains("end=\"2004\""));
    }
}