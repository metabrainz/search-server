package org.musicbrainz.search.servlet;

import junit.framework.TestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.TagIndexField;
import org.musicbrainz.search.servlet.mmd2.TagWriter;

import java.io.PrintWriter;
import java.io.StringWriter;


public class FindTagTest extends TestCase {

    private SearchServer ss;


    public FindTagTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        RAMDirectory ramDir = new RAMDirectory();

        Analyzer analyzer = DatabaseIndex.getAnalyzer(TagIndexField.class);
        IndexWriterConfig  writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(ramDir, writerConfig);
        {
            MbDocument doc = new MbDocument();
            doc.addField(TagIndexField.TAG, "rock");
            writer.addDocument(doc.getLuceneDocument());

            doc = new MbDocument();
            doc.addField(TagIndexField.TAG, "classic rock");
            writer.addDocument(doc.getLuceneDocument());
        }
        writer.close();
        ss = new TagSearch(new IndexSearcher(IndexReader.open(ramDir)));
    }



    public void testFindTagByName() throws Exception {
        Results res = ss.searchLucene("tag:rock", 0, 10);
        assertEquals(2, res.totalHits);
        Result result = res.results.get(0);

        MbDocument doc = result.doc;
        assertEquals("rock", doc.get(TagIndexField.TAG));
        
        result = res.results.get(1);
        doc = result.doc;
        assertEquals("classic rock", doc.get(TagIndexField.TAG));
    }

    public void testOutputAsXml() throws Exception {

        Results res = ss.searchLucene("tag:rock", 0, 10);
        ResultsWriter writer = new TagWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();
        String output = sw.toString();
        System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"2\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("<name>rock</name>"));
        assertTrue(output.contains("<name>classic rock</name>"));


    }
}