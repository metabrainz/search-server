package org.musicbrainz.search.servlet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Test;
import org.musicbrainz.mmd2.Alias;
import org.musicbrainz.mmd2.AliasList;
import org.musicbrainz.mmd2.ObjectFactory;
import org.musicbrainz.mmd2.Editor;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.MusicbrainzSimilarity;
import org.musicbrainz.search.index.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Assumes an index has been built stored and in the data folder, I've picked a fairly obscure bside so hopefully
 * will not get added to another release
 */
public class FindEditorTest {

    private AbstractSearchServer ss;
    private AbstractDismaxSearchServer sd;


    @Before
    public void setUp() throws Exception {
        ObjectFactory of = new ObjectFactory();
        RAMDirectory ramDir = new RAMDirectory();
        Analyzer analyzer = DatabaseIndex.getAnalyzer(EditorIndexField.class);
        IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
        writerConfig.setSimilarity(new MusicbrainzSimilarity());
        IndexWriter writer = new IndexWriter(ramDir, writerConfig);

        {
            MbDocument doc = new MbDocument();

            Editor editor = of.createEditor();

            doc.addField(EditorIndexField.EDITOR, "fred");
            editor.setName("fred");

            doc.addField(EditorIndexField.BIO, "A man called fred");
            editor.setBio("A man called fred");

            doc.addField(EditorIndexField.EDITOR_STORE, MMDSerializer.serialize(editor));
            writer.addDocument(doc.getLuceneDocument());
        }


        {
            MbDocument doc = new MbDocument();
            Editor editor = of.createEditor();
            doc.addField(MetaIndexField.META, MetaIndexField.META_VALUE);
            doc.addNumericField(MetaIndexField.LAST_UPDATED, new Date().getTime());
            doc.addField(EditorIndexField.EDITOR_STORE, MMDSerializer.serialize(editor));
            writer.addDocument(doc.getLuceneDocument());
        }

        writer.close();
        SearcherManager searcherManager = new SearcherManager(ramDir, new MusicBrainzSearcherFactory(ResourceType.EDITOR));
        ss = new EditorSearch(searcherManager);
        sd = new EditorDismaxSearch(ss);
    }



    @Test
    public void testFindEditorByName() throws Exception {
        Results res = ss.search("editor:\"fred\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("fred", doc.get(EditorIndexField.EDITOR));
    }

    @Test
    public void testFindEditorByBio() throws Exception {
        Results res = ss.search("bio:\"man\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("fred", doc.get(EditorIndexField.EDITOR));
    }

    @Test
    public void testFindEditorByDismax1() throws Exception {
        Results res = sd.search("fred", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("fred", doc.get(EditorIndexField.EDITOR));
    }

    @Test
    public void testFindEditorByDismax2() throws Exception {
        Results res = sd.search("man", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("fred", doc.get(EditorIndexField.EDITOR));
    }

    @Test
    public void testFindEditorByDefault() throws Exception {

        {
            Results res = ss.search("\"fred\"", 0, 10);
            assertEquals(1, res.getTotalHits());
            Result result = res.results.get(0);
            MbDocument doc = result.getDoc();
            assertEquals("fred", doc.get(EditorIndexField.EDITOR));
        }

    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testOutputJson() throws Exception {

        Results res = ss.search("editor:\"fred\"", 0, 10);
        org.musicbrainz.search.servlet.mmd2.ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON);
        pr.close();

        String output = sw.toString();
        System.out.println("Json is" + output);

        assertTrue(output.contains("\"editor-list\""));
        assertTrue(output.contains("\"count\":1"));
        assertTrue(output.contains("\"offset\":0,"));
        assertTrue(output.contains("name\":\"fred\""));
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testOutputJsonNew() throws Exception {

        Results res = ss.search("editor:\"fred\"", 0, 10);
        org.musicbrainz.search.servlet.mmd2.ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON_NEW);
        pr.close();

        String output = sw.toString();
        System.out.println("Json New is" + output);

        assertTrue(output.contains("\"editors\""));
        assertTrue(output.contains("name\":\"fred\""));
        assertTrue(output.contains("\"count\":1"));
        assertTrue(output.contains("\"offset\":0,"));

    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testOutputJsonNewIdent() throws Exception {

        Results res = ss.search("editor:\"fred\"", 0, 10);
        org.musicbrainz.search.servlet.mmd2.ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON_NEW, true);
        pr.close();

        String output = sw.toString();
        System.out.println("Json New Ident is" + output);
        assertTrue(output.contains("\"offset\" : 0"));

    }
}
