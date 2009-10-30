package org.musicbrainz.search.servlet;

import junit.framework.TestCase;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.velocity.app.Velocity;
import org.musicbrainz.search.index.*;
import org.musicbrainz.search.servlet.LabelSearch;
import org.musicbrainz.search.servlet.mmd1.LabelMmd1XmlWriter;
import org.musicbrainz.search.servlet.MbDocument;
import org.musicbrainz.search.servlet.Result;
import org.musicbrainz.search.servlet.Results;
import org.musicbrainz.search.servlet.ResultsWriter;
import org.musicbrainz.search.servlet.SearchServer;
import org.musicbrainz.search.servlet.SearchServerServlet;
import org.musicbrainz.search.servlet.mmd2.LabelXmlWriter;
import org.musicbrainz.search.servlet.mmd2.WorkXmlWriter;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Assumes an index has been built stored and in the data folder, I've picked a fairly obscure bside so hopefully
 * will not get added to another release
 */
public class FindWorkTest extends TestCase {

    private SearchServer ss;


    public FindWorkTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        SearchServerServlet.setUpVelocity();
        Velocity.init();
        RAMDirectory ramDir = new RAMDirectory();
        PerFieldAnalyzerWrapper analyzer = new PerFieldEntityAnalyzer(WorkIndexField.class);
        IndexWriter writer = new IndexWriter(ramDir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        {
            Document doc = new Document();
            Index.addFieldToDocument(doc, WorkIndexField.WORK_ID, "4ff89cf0-86af-11de-90ed-001fc6f176ff");
            Index.addFieldToDocument(doc, WorkIndexField.WORK, "Symphony No. 5");
            Index.addFieldToDocument(doc, WorkIndexField.ISWC,"T-101779304-1");
            Index.addFieldToDocument(doc, WorkIndexField.ARTIST_ID, "1f9df192-a621-4f54-8850-2c5373b7eac9");
            Index.addFieldToDocument(doc, WorkIndexField.ARTIST, "Ludwig van Beethoven");
            Index.addFieldToDocument(doc, WorkIndexField.ARTIST_NAME, "Ludwig van Beethoven");
            Index.addFieldToDocument(doc, WorkIndexField.ARTIST_NAMECREDIT, "Ludwig van Beethoven");
            Index.addFieldToDocument(doc, WorkIndexField.ARTIST_SORTNAME, "Beethoven, Ludwig van");
            Index.addFieldToDocument(doc, WorkIndexField.ARTIST_JOINPHRASE, "-");

            writer.addDocument(doc);
        }
        writer.close();
        ss = new LabelSearch(new IndexSearcher(ramDir, true));
    }

    public void testFindWorkById() throws Exception {
        Results res = ss.searchLucene("wid:\"4ff89cf0-86af-11de-90ed-001fc6f176ff\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4ff89cf0-86af-11de-90ed-001fc6f176ff", doc.get(WorkIndexField.WORK_ID));
        assertEquals("Symphony No. 5", doc.get(WorkIndexField.WORK));
    }

    public void testFindWorkByName() throws Exception {
        Results res = ss.searchLucene("work:\"Symphony No. 5\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4ff89cf0-86af-11de-90ed-001fc6f176ff", doc.get(WorkIndexField.WORK_ID));
        assertEquals("Symphony No. 5", doc.get(WorkIndexField.WORK));
    }

    /**
     * Tests get same results as
     * http://musicbrainz.org/ws/1/label/?type=xml&query=%22Jockey%20Slut%22
     *
     * @throws Exception
     */
    public void testOutputAsXml() throws Exception {

        Results res = ss.searchLucene("work:\"Symphony No. 5\"", 0, 1);
        ResultsWriter writer = new WorkXmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();
        String output = sw.toString();
        System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("id=\"4ff89cf0-86af-11de-90ed-001fc6f176ff\""));
        assertTrue(output.contains("<title>Symphony No. 5</title>"));
        assertTrue(output.contains("<name>Ludwig van Beethoven</name>"));
        assertTrue(output.contains("<sort-name>Beethoven, Ludwig van</sort-name>"));
        assertTrue(output.contains("<iswc>T-101779304-1</iswc>"));


    }
}