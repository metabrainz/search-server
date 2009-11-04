package org.musicbrainz.search.servlet;

import junit.framework.TestCase;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;
import org.musicbrainz.search.index.AnnotationIndexField;
import org.musicbrainz.search.index.AnnotationType;

/**
 * Test retrieving Annotations entries from index and Outputting as Html
 */
public class FindAnnotationTest extends TestCase {

    private SearchServer ss;

    public FindAnnotationTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        RAMDirectory ramDir = new RAMDirectory();
        PerFieldAnalyzerWrapper analyzer = new PerFieldEntityAnalyzer(AnnotationIndexField.class);
        IndexWriter writer = new IndexWriter(ramDir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);

        //A complete Annotation entry
        {
            MbDocument doc = new MbDocument();
            doc.addField(AnnotationIndexField.TYPE, AnnotationType.RELEASE.getName());
            doc.addField(AnnotationIndexField.NAME, "Pieds nus sur la braise");
            doc.addField(AnnotationIndexField.MBID, "bdb24cb5-404b-4f60-bba4-7b730325ae47");
            doc.addField(AnnotationIndexField.TEXT, "EAN: 0828768226629 - DiscID: TWj6cLku360MfFYAq_MEaT_stgc-");
            writer.addDocument(doc.getLuceneDocument());
        }

        writer.close();
        ss = new AnnotationSearch(new IndexSearcher(ramDir, true));
    }

    public void testSearchByTypeNoMatch() throws Exception {
        Results res = ss.searchLucene("type:artist", 0, 10);
        assertEquals(0, res.totalHits);
    }

    public void testSearchByType() throws Exception {
        Results res = ss.searchLucene("type:release", 0, 10);
        assertEquals(1, res.totalHits);
    }

    public void testSearchByName() throws Exception {
        Results res = ss.searchLucene("name:Pieds nus sur la braise", 0, 10);
        assertEquals(1, res.totalHits);
    }

    public void testSearchByNameNoMatch() throws Exception {
        Results res = ss.searchLucene("name:fred", 0, 10);
        assertEquals(0, res.totalHits);
    }

    public void testSearchByMbid() throws Exception {
        Results res = ss.searchLucene("mbid:bdb24cb5-404b-4f60-bba4-7b730325ae47", 0, 10);
        assertEquals(1, res.totalHits);
    }

    public void testSearchByMbidNoMatch() throws Exception {
        Results res = ss.searchLucene("mbid:bdb24cb5-404b-4f60-bba4-000000000000", 0, 10);
        assertEquals(0, res.totalHits);
    }

    public void testSearchByText() throws Exception {
        Results res = ss.searchLucene("text:DiscID", 0, 10);
        assertEquals(1, res.totalHits);
    }

    public void testSearchByTextNoMatch() throws Exception {
        Results res = ss.searchLucene("text:fred", 0, 10);
        assertEquals(0, res.totalHits);
    }

    public void testSearchByDefaultField() throws Exception {

        Results res = ss.searchLucene("DiscID", 0, 10);
        assertEquals(1, res.totalHits);
    }
}