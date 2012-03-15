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
import org.musicbrainz.search.index.AnnotationIndexField;
import org.musicbrainz.search.index.AnnotationType;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.servlet.mmd2.AnnotationWriter;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Test retrieving Annotations entries from index and Outputting as Html
 */
public class FindAnnotationTest extends TestCase {

    private SearchServer ss;
    private SearchServer sd;

    public FindAnnotationTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        RAMDirectory ramDir = new RAMDirectory();
        Analyzer analyzer = DatabaseIndex.getAnalyzer(AnnotationIndexField.class);
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(ramDir, writerConfig);

        //A complete Release Annotation entry
        {
            MbDocument doc = new MbDocument();
            doc.addField(AnnotationIndexField.TYPE, AnnotationType.RELEASE.getName());
            doc.addField(AnnotationIndexField.NAME, "Pieds nus sur la braise");
            doc.addField(AnnotationIndexField.ENTITY, "bdb24cb5-404b-4f60-bba4-7b730325ae47");
            doc.addField(AnnotationIndexField.TEXT, "EAN: 0828768226629 - DiscID: TWj6cLku360MfFYAq_MEaT_stgc-");
            writer.addDocument(doc.getLuceneDocument());
        }

        //A complete Artist Annotation entry
        {
            MbDocument doc = new MbDocument();
            doc.addField(AnnotationIndexField.TYPE, AnnotationType.ARTIST.getName());
            doc.addField(AnnotationIndexField.NAME, "Bjork");
            doc.addField(AnnotationIndexField.ENTITY, "123456qa-404b-4f60-bba4-7b730325ae47");
            doc.addField(AnnotationIndexField.TEXT, "Icelandic");
            writer.addDocument(doc.getLuceneDocument());
        }

        //A complete Recording Annotation entry
        {
            MbDocument doc = new MbDocument();
            doc.addField(AnnotationIndexField.TYPE, AnnotationType.RECORDING.getName());
            doc.addField(AnnotationIndexField.NAME, "Give my love to Kevin");
            doc.addField(AnnotationIndexField.ENTITY, "bdb24cdd-404b-4f60-bba4-7b730325ae47");
            doc.addField(AnnotationIndexField.TEXT, "Single");
            writer.addDocument(doc.getLuceneDocument());
        }

        //A complete Label Annotation entry
        {
            MbDocument doc = new MbDocument();
            doc.addField(AnnotationIndexField.TYPE, AnnotationType.LABEL.getName());
            doc.addField(AnnotationIndexField.NAME, "Reception Records");
            doc.addField(AnnotationIndexField.ENTITY, "bdb32cb5-404b-4f60-bba4-7b730325ae47");
            doc.addField(AnnotationIndexField.TEXT, "Setup by Wedding Present, released a few records by others");
            writer.addDocument(doc.getLuceneDocument());
        }

        //A complete Release Group Annotation entry
        {
            MbDocument doc = new MbDocument();
            doc.addField(AnnotationIndexField.TYPE, AnnotationType.RELEASE_GROUP.getName());
            doc.addField(AnnotationIndexField.NAME, "3 Songs");
            doc.addField(AnnotationIndexField.ENTITY, "aaa24cb5-404b-4f60-bba4-7b730325ae47");
            doc.addField(AnnotationIndexField.TEXT, "Also known as Corduroy");
            writer.addDocument(doc.getLuceneDocument());
        }

        //A complete Work Annotation entry
        {
            MbDocument doc = new MbDocument();
            doc.addField(AnnotationIndexField.TYPE, AnnotationType.WORK.getName());
            doc.addField(AnnotationIndexField.NAME, "Song 2");
            doc.addField(AnnotationIndexField.ENTITY, "DDDD24cb5-404b-4f60-bba4-7b730325ae47");
            doc.addField(AnnotationIndexField.TEXT, "There was no Song 1");
            writer.addDocument(doc.getLuceneDocument());
        }

        writer.close();
        ss = new AnnotationSearch(new IndexSearcher(IndexReader.open(ramDir)));
        sd = new AnnotationDismaxSearch(new IndexSearcher(IndexReader.open(ramDir)));

    }

    public void testSearchByTypeRelease() throws Exception {
        Results res = ss.searchLucene("type:release", 0, 10);
        assertEquals(1, res.totalHits);
    }

    public void testSearchByTypeArtist() throws Exception {
        Results res = ss.searchLucene("type:artist", 0, 10);
        assertEquals(1, res.totalHits);
    }

    public void testSearchByDismax1() throws Exception {
        Results res = sd.searchLucene("Pieds nus", 0, 10);
        assertEquals(1, res.totalHits);
    }

    public void testSearchByDismax2() throws Exception {
        Results res = sd.searchLucene("0828768226629", 0, 10);
        assertEquals(1, res.totalHits);
    }

    public void testSearchByTypeReleaseGroup() throws Exception {
        Results res = ss.searchLucene("type:release-group", 0, 10);
        assertEquals("3 Songs", res.results.get(0).getDoc().get(AnnotationIndexField.NAME));
        assertEquals(1, res.totalHits);
    }

    public void testSearchByTypeRecording() throws Exception {
        Results res = ss.searchLucene("type:recording", 0, 10);
        assertEquals(1, res.totalHits);
    }

    public void testSearchByTypeWork() throws Exception {
        Results res = ss.searchLucene("type:work", 0, 10);
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

    public void testSearchByEntity() throws Exception {
        Results res = ss.searchLucene("entity:bdb24cb5-404b-4f60-bba4-7b730325ae47", 0, 10);
        assertEquals(1, res.totalHits);
    }

    public void testSearchByEntitydNoMatch() throws Exception {
        Results res = ss.searchLucene("entity:bdb24cb5-404b-4f60-bba4-000000000000", 0, 10);
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

/**
     * @throws Exception
     */
    public void testOutputXml() throws Exception {

        Results res = ss.searchLucene("entity:bdb24cb5-404b-4f60-bba4-7b730325ae47", 0, 1);
        org.musicbrainz.search.servlet.mmd2.ResultsWriter writer = new AnnotationWriter();
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
        assertTrue(output.contains("<name>Pieds nus sur la braise</name>"));
        assertTrue(output.contains("type=\"release\""));
        assertTrue(output.contains("<entity>bdb24cb5-404b-4f60-bba4-7b730325ae47</entity>"));
        assertTrue(output.contains("<text>EAN: 0828768226629 - DiscID: TWj6cLku360MfFYAq_MEaT_stgc-</text>"));

    }

    public void testOutputJson() throws Exception {

        Results res = ss.searchLucene("entity:bdb24cb5-404b-4f60-bba4-7b730325ae47", 0, 1);
        org.musicbrainz.search.servlet.mmd2.ResultsWriter writer = new AnnotationWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON);
        pr.close();

        String output = sw.toString();
        System.out.println("Json is" + output);

        assertTrue(output.contains("\"count\":1"));
        assertTrue(output.contains("\"offset\":0,"));
        assertTrue(output.contains("\"score\":\"100\","));
        assertTrue(output.contains("\"name\":\"Pieds nus sur la braise\""));
        assertTrue(output.contains("\"type\":\"release\""));
        assertTrue(output.contains("\"entity\":\"bdb24cb5-404b-4f60-bba4-7b730325ae47\""));
        assertTrue(output.contains("\"text\":\"EAN: 0828768226629 - DiscID: TWj6cLku360MfFYAq_MEaT_stgc-\""));

    }

}