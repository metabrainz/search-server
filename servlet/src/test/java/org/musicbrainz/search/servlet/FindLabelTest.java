package org.musicbrainz.search.servlet;

import junit.framework.TestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.MusicbrainzSimilarity;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.LabelIndexField;
import org.musicbrainz.search.servlet.mmd1.LabelMmd1XmlWriter;
import org.musicbrainz.search.servlet.mmd1.LabelType;
import org.musicbrainz.search.servlet.mmd2.LabelWriter;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Assumes an index has been built stored and in the data folder, I've picked a fairly obscure bside so hopefully
 * will not get added to another release
 */
public class FindLabelTest extends TestCase {

    private SearchServer ss;
    private SearchServer sd;


    public FindLabelTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        RAMDirectory ramDir = new RAMDirectory();
        Analyzer analyzer = DatabaseIndex.getAnalyzer(LabelIndexField.class);
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        writerConfig.setSimilarity(new MusicbrainzSimilarity());
        IndexWriter writer = new IndexWriter(ramDir, writerConfig);
                
        {
            MbDocument doc = new MbDocument();
            doc.addField(LabelIndexField.LABEL_ID, "ff571ff4-04cb-4b9c-8a1c-354c330f863c");
            doc.addField(LabelIndexField.LABEL, "Jockey Slut");
            doc.addField(LabelIndexField.SORTNAME, "Slut, Jockey");
            doc.addField(LabelIndexField.ALIAS, "Jockeys");
            doc.addNumericField(LabelIndexField.CODE, 1234);
            doc.addField(LabelIndexField.BEGIN, "1993");
            doc.addField(LabelIndexField.END, "2004");
            doc.addField(LabelIndexField.TYPE, "Production");
            doc.addField(LabelIndexField.COUNTRY, "GB");
            doc.addField(LabelIndexField.TAG, "dance");
            doc.addField(LabelIndexField.TAGCOUNT, "22");
            doc.addField(LabelIndexField.IPI,"1001");

            writer.addDocument(doc.getLuceneDocument());
        }

        {
            MbDocument doc = new MbDocument();
            doc.addField(LabelIndexField.LABEL_ID, "a539bb1e-f2e1-4b45-9db8-8053841e7503");
            doc.addField(LabelIndexField.LABEL, "4AD");
            doc.addField(LabelIndexField.SORTNAME, "4AD");
            doc.addField(LabelIndexField.BEGIN, "1979");
            doc.addNumericField(LabelIndexField.CODE, 5807);
            doc.addField(LabelIndexField.TYPE, LabelType.PRODUCTION.getName());
            doc.addField(LabelIndexField.COUNTRY, "unknown");

            writer.addDocument(doc.getLuceneDocument());
        }

        {
            MbDocument doc = new MbDocument();
            doc.addField(LabelIndexField.LABEL_ID, "a539bb1e-f2e1-4b45-9db8-8053841e7504");
            doc.addField(LabelIndexField.LABEL, "Dark Prism");
            doc.addField(LabelIndexField.SORTNAME, "Dark Prism");
            doc.addField(LabelIndexField.CODE, "");
            doc.addField(LabelIndexField.TYPE, LabelType.HOLDING.getName());
            writer.addDocument(doc.getLuceneDocument());
        }

        {
            MbDocument doc = new MbDocument();
            doc.addField(LabelIndexField.LABEL_ID, "b539bb1e-f2e1-4b45-9db8-8053841e7504");
            doc.addField(LabelIndexField.LABEL, "blob");
            doc.addField(LabelIndexField.SORTNAME, "blob");
            doc.addField(LabelIndexField.TYPE, "unknown");
            writer.addDocument(doc.getLuceneDocument());
        }


        writer.close();
        ss = new LabelSearch(new IndexSearcher(ramDir, true));
        sd = new LabelDismaxSearch(new IndexSearcher(ramDir, true));
    }

    public void testFindLabelById() throws Exception {
        Results res = ss.searchLucene("laid:\"ff571ff4-04cb-4b9c-8a1c-354c330f863c\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(LabelIndexField.LABEL_ID));
        assertEquals("Jockey Slut", doc.get(LabelIndexField.LABEL));
        assertEquals("1993", doc.get(LabelIndexField.BEGIN));
        assertEquals("2004", doc.get(LabelIndexField.END));
        assertEquals("Jockeys",doc.get(LabelIndexField.ALIAS));
        assertNull(doc.get(LabelIndexField.COMMENT));
        assertEquals("Slut, Jockey", doc.get(LabelIndexField.SORTNAME));
        assertEquals("Production", doc.get(LabelIndexField.TYPE));
    }

    public void testFindLabelByName() throws Exception {
        Results res = ss.searchLucene("label:\"Jockey Slut\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(LabelIndexField.LABEL_ID));
        assertEquals("Jockey Slut", doc.get(LabelIndexField.LABEL));
    }

    public void testFindLabelByDismax1() throws Exception {
        Results res = sd.searchLucene("Jockey Slut", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(LabelIndexField.LABEL_ID));
        assertEquals("Jockey Slut", doc.get(LabelIndexField.LABEL));
    }

    public void testFindLabelByDismax2() throws Exception {
        Results res = sd.searchLucene("Jockey", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(LabelIndexField.LABEL_ID));
        assertEquals("Jockey Slut", doc.get(LabelIndexField.LABEL));
    }

    public void testFindLabelByDefault() throws Exception {

        {
            Results res = ss.searchLucene("\"Jockey Slut\"", 0, 10);
            assertEquals(1, res.totalHits);
            Result result = res.results.get(0);
            MbDocument doc = result.doc;
            assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(LabelIndexField.LABEL_ID));
            assertEquals("Jockey Slut", doc.get(LabelIndexField.LABEL));
        }

        {
            Results res = ss.searchLucene("\"Slut Jockey\"", 0, 10);
            assertEquals(1, res.totalHits);
            Result result = res.results.get(0);
            MbDocument doc = result.doc;
            assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(LabelIndexField.LABEL_ID));
            assertEquals("Jockey Slut", doc.get(LabelIndexField.LABEL));
        }

        {
            Results res = ss.searchLucene("\"Jockeys\"", 0, 10);
            assertEquals(1, res.totalHits);
            Result result = res.results.get(0);
            MbDocument doc = result.doc;
            assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(LabelIndexField.LABEL_ID));
            assertEquals("Jockey Slut", doc.get(LabelIndexField.LABEL));
        }
    }

    public void testFindLabelByType() throws Exception {
        Results res = ss.searchLucene("type:\"production\"", 0, 10);
        assertEquals(2, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;

        //(This will always come first because searcher sots by score and then docno, and this doc added first)
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(LabelIndexField.LABEL_ID));
        assertEquals("Jockey Slut", doc.get(LabelIndexField.LABEL));
    }

     public void testFindLabelByIpi() throws Exception {
        Results res = ss.searchLucene("ipi:1001", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;

        //(This will always come first because searcher sots by score and then docno, and this doc added first)
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(LabelIndexField.LABEL_ID));
        assertEquals("Jockey Slut", doc.get(LabelIndexField.LABEL));
    }

    public void testFindLabelByNumericType() throws Exception {
            Results res = ss.searchLucene("type:3", 0, 10);
            assertEquals(2, res.totalHits);
            Result result = res.results.get(0);
            MbDocument doc = result.doc;

            //(This will always come first because searcher sots by score and then docno, and this doc added first)
            assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(LabelIndexField.LABEL_ID));
            assertEquals("Jockey Slut", doc.get(LabelIndexField.LABEL));
        }


    public void testFindLabelBySortname() throws Exception {
        Results res = ss.searchLucene("sortname:\"Slut, Jockey\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(LabelIndexField.LABEL_ID));
        assertEquals("Jockey Slut", doc.get(LabelIndexField.LABEL));
    }

    public void testFindLabelByCountry() throws Exception {
        Results res = ss.searchLucene("country:\"gb\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(LabelIndexField.LABEL_ID));
        assertEquals("Jockey Slut", doc.get(LabelIndexField.LABEL));
    }

    public void testFindLabelByUnknownCountry() throws Exception {
            Results res = ss.searchLucene("country:\"unknown\"", 0, 10);
            assertEquals(1, res.totalHits);
            Result result = res.results.get(0);
            MbDocument doc = result.doc;
            assertEquals("a539bb1e-f2e1-4b45-9db8-8053841e7503", doc.get(LabelIndexField.LABEL_ID));
        }

    public void testFindLabelByCountryUpercase() throws Exception {
        Results res = ss.searchLucene("country:\"GB\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(LabelIndexField.LABEL_ID));
        assertEquals("Jockey Slut", doc.get(LabelIndexField.LABEL));
    }


    public void testFindLabelByCode() throws Exception {
        Results res = ss.searchLucene("code:5807", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("a539bb1e-f2e1-4b45-9db8-8053841e7503", doc.get(LabelIndexField.LABEL_ID));
        assertEquals("4AD", doc.get(LabelIndexField.LABEL));
    }

    public void testFindLabelByCode2() throws Exception {
        Results res = ss.searchLucene("code:005807", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("a539bb1e-f2e1-4b45-9db8-8053841e7503", doc.get(LabelIndexField.LABEL_ID));
        assertEquals("4AD", doc.get(LabelIndexField.LABEL));
    }

     public void testFindLabelByCodeRange() throws Exception {
        Results res = ss.searchLucene("code:[5806 TO 5807]", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("a539bb1e-f2e1-4b45-9db8-8053841e7503", doc.get(LabelIndexField.LABEL_ID));
        assertEquals("4AD", doc.get(LabelIndexField.LABEL));
    }

    public void testFindLabelByZeroedCode() throws Exception {
        Results res = ss.searchLucene("code:\"05807\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("a539bb1e-f2e1-4b45-9db8-8053841e7503", doc.get(LabelIndexField.LABEL_ID));
        assertEquals("4AD", doc.get(LabelIndexField.LABEL));
    }

     public void testFindArtistByTag() throws Exception {
        Results res = ss.searchLucene("tag:dance", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(LabelIndexField.LABEL_ID));
        assertEquals("Jockey Slut", doc.get(LabelIndexField.LABEL));
    }

    public void testIssue66() throws Exception {
        Results res = ss.searchLucene("dark", 0, 10);
        assertEquals(1, res.totalHits);
        ResultsWriter writer = new LabelMmd1XmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();
        String output =sw.toString();
        assertTrue(output.contains("<name>Dark Prism</name>"));

        writer = new LabelWriter();
        sw = new StringWriter();
        pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();
        assertTrue(output.contains("<name>Dark Prism</name>"));

    }


    /**
     * Tests get same results as
     * http://musicbrainz.org/ws/1/label/?type=xml&query=%22Jockey%20Slut%22
     *
     * @throws Exception exeption
     */
    public void testOutputAsMmd1Xml() throws Exception {

        Results res = ss.searchLucene("label:\"Jockey Slut\"", 0, 1);
        ResultsWriter writer = new LabelMmd1XmlWriter();
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
        assertTrue(output.contains("<sort-name>Slut, Jockey</sort-name>"));
        assertTrue(output.contains("begin=\"1993\""));
        assertTrue(output.contains("end=\"2004\""));
    }

     /**
     * Tests get same results as
     * http://musicbrainz.org/ws/1/label/?type=xml&query=%22Jockey%20Slut%22
     *
     * @throws Exception exception
     */
    public void testOutputAsXml() throws Exception {

        Results res = ss.searchLucene("label:\"Jockey Slut\"", 0, 1);
        ResultsWriter writer = new LabelWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();
        String output = sw.toString();
        System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("xmlns:ext=\"http://musicbrainz.org/ns/ext#-2.0\""));
        assertTrue(output.contains("id=\"ff571ff4-04cb-4b9c-8a1c-354c330f863c\""));
        assertTrue(output.contains("type=\"Production\""));
        assertTrue(output.contains("<name>Jockey Slut</name>"));
        assertTrue(output.contains("<sort-name>Slut, Jockey</sort-name>"));
        assertTrue(output.contains("<alias>Jockeys</alias>"));
        assertTrue(output.contains("<begin>1993</begin"));
        assertTrue(output.contains("<end>2004</end>"));
        assertTrue(output.contains("<label-code>1234</label-code>"));
        assertTrue(output.contains("<country>GB</country>"));
        assertTrue(output.contains("dance</name>"));
    }

    public void testOutputAsXmlWithUnknownCountry() throws Exception {

            Results res = ss.searchLucene("laid:a539bb1e-f2e1-4b45-9db8-8053841e7503", 0, 1);
            ResultsWriter writer = new LabelWriter();
            StringWriter sw = new StringWriter();
            PrintWriter pr = new PrintWriter(sw);
            writer.write(pr, res);
            pr.close();
            String output = sw.toString();
            System.out.println("Xml is" + output);
            assertTrue(output.contains("count=\"1\""));
            assertTrue(output.contains("offset=\"0\""));
            assertTrue(output.contains("xmlns:ext=\"http://musicbrainz.org/ns/ext#-2.0\""));
            assertTrue(output.contains("id=\"a539bb1e-f2e1-4b45-9db8-8053841e7503\""));
            assertFalse(output.contains("<country>"));
        }

    public void testOutputAsMMd1XmlWithUnknownType() throws Exception {

        Results res = ss.searchLucene("blob", 0, 1);
        ResultsWriter writer = new LabelMmd1XmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();
        String output = sw.toString();
        System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertFalse(output.contains("label type"));
    }

    /**
     * @throws Exception exception
     */
    public void testOutputJson() throws Exception {

        Results res = ss.searchLucene("label:\"Jockey Slut\"", 0, 10);
        org.musicbrainz.search.servlet.mmd2.ResultsWriter writer = new LabelWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON);
        pr.close();

        String output = sw.toString();
        System.out.println("Json is" + output);

        assertTrue(output.contains("id\":\"ff571ff4-04cb-4b9c-8a1c-354c330f863c\""));
        assertTrue(output.contains("\"count\":1"));
        assertTrue(output.contains("\"offset\":0,"));
        assertTrue(output.contains("\"type\":\"Production\""));
        assertTrue(output.contains("name\":\"Jockey Slut\""));
        assertTrue(output.contains("\"sort-name\":\"Slut, Jockey\""));
        assertTrue(output.contains("life-span\":{\"begin\":\"1993\""));
        assertTrue(output.contains("\"label-code\":1234"));
        assertTrue(output.contains("\"country\":\"GB\""));
        assertTrue(output.contains("tag-list\":{\"tag\":[{\"count\":22,\"name\":\"dance\"}"));
    }
}