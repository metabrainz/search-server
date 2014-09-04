package org.musicbrainz.search.servlet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Test;
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.index.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Assumes an index has been built stored and in the data folder, I've picked a fairly obscure bside so hopefully
 * will not get added to another release
 */
public class FindUrlTest {

    private AbstractSearchServer ss;
    private AbstractDismaxSearchServer sd;

    @Before
    public void setUp() throws Exception {
        RAMDirectory ramDir = new RAMDirectory();
        ObjectFactory of = new ObjectFactory();

        Analyzer analyzer = DatabaseIndex.getAnalyzer(UrlIndexField.class);
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION, analyzer);
        IndexWriter writer = new IndexWriter(ramDir, writerConfig);
        {
            MbDocument doc = new MbDocument();

            Url url = of.createUrl();
            doc.addField(UrlIndexField.URL_ID, "4ff89cf0-86af-11de-90ed-001fc6f176ff");
            url.setId("4ff89cf0-86af-11de-90ed-001fc6f176ff");
            doc.addField(UrlIndexField.URL, "http://en.wikipedia.org/wiki/Nine_Inch_Nails");
            url.setResource("http://en.wikipedia.org/wiki/Nine_Inch_Nails");


            RelationList rl = of.createRelationList();
            rl.setTargetType("artist");
            doc.addField(UrlIndexField.TARGET_TYPE, "artist");
            doc.addField(UrlIndexField.RELATION_TYPE, "Wikipedia");
            {
                Relation relation = of.createRelation();
                Target   target   = of.createTarget();
                target.setValue("4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
                relation.setTarget(target);
                Artist artist1 = of.createArtist();
                artist1.setId("4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
                artist1.setName("Nine Inch Nails");
                artist1.setSortName("Nine Inch Nails");
                relation.setArtist(artist1);
                relation.setType("Wikipedia");
                rl.getRelation().add(relation);
            }
            url.getRelationList().add(rl);
            String store = MMDSerializer.serialize(url);
            doc.addField(UrlIndexField.URL_STORE, store);
            writer.addDocument(doc.getLuceneDocument());
        }

        {
            MbDocument doc = new MbDocument();
            doc.addField(MetaIndexField.META, MetaIndexField.META_VALUE);
            doc.addNumericField(MetaIndexField.LAST_UPDATED, new Date().getTime());
            writer.addDocument(doc.getLuceneDocument());
        }

        writer.close();
        SearcherManager searcherManager = new SearcherManager(ramDir, new MusicBrainzSearcherFactory(ResourceType.URL));
        ss = new

                UrlSearch(searcherManager);

        sd = new

                UrlDismaxSearch(ss);

    }

    @Test
    public void testFindUrlById() throws Exception {
        Results res = ss.search("uid:\"4ff89cf0-86af-11de-90ed-001fc6f176ff\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("4ff89cf0-86af-11de-90ed-001fc6f176ff", doc.get(UrlIndexField.URL_ID));
        assertEquals("http://en.wikipedia.org/wiki/Nine_Inch_Nails", doc.get(UrlIndexField.URL));
    }

    @Test
    public void testFindUrlByName() throws Exception {
        Results res = ss.search("url:\"http://en.wikipedia.org/wiki/Nine_Inch_Nails\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("4ff89cf0-86af-11de-90ed-001fc6f176ff", doc.get(UrlIndexField.URL_ID));
        assertEquals("http://en.wikipedia.org/wiki/Nine_Inch_Nails", doc.get(UrlIndexField.URL));
    }

    @Test
    public void testFindUrlByTargetType() throws Exception {
        Results res = ss.search("targettype:artist", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("4ff89cf0-86af-11de-90ed-001fc6f176ff", doc.get(UrlIndexField.URL_ID));
        assertEquals("http://en.wikipedia.org/wiki/Nine_Inch_Nails", doc.get(UrlIndexField.URL));
    }

    @Test
    public void testFindUrlByTargetTypeUppercase() throws Exception {
        Results res = ss.search("targettype:ARTIST", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("4ff89cf0-86af-11de-90ed-001fc6f176ff", doc.get(UrlIndexField.URL_ID));
        assertEquals("http://en.wikipedia.org/wiki/Nine_Inch_Nails", doc.get(UrlIndexField.URL));
    }
    @Test
    public void testFindUrlByRelationType() throws Exception {
        Results res = ss.search("relationtype:wikipedia", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("4ff89cf0-86af-11de-90ed-001fc6f176ff", doc.get(UrlIndexField.URL_ID));
        assertEquals("http://en.wikipedia.org/wiki/Nine_Inch_Nails", doc.get(UrlIndexField.URL));
    }

    /**
     * Tests
     *
     * @throws Exception
     */
    @Test
    public void testOutputAsXml() throws Exception {

        Results res = ss.search("uid:\"4ff89cf0-86af-11de-90ed-001fc6f176ff\"", 0, 1);
        ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_XML, true);
        pr.close();
        String output = sw.toString();
        System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("id=\"4ff89cf0-86af-11de-90ed-001fc6f176ff\""));
        assertTrue(output.contains("<resource>http://en.wikipedia.org/wiki/Nine_Inch_Nails</resource>"));
        assertTrue(output.contains("<relation-list target-type=\"artist\">"));
        assertTrue(output.contains("<relation type=\"Wikipedia\">"));
        assertTrue(output.contains("<target>4302e264-1cf0-4d1f-aca7-2a6f89e34b36</target>"));
    }

    /**
     * Tests
     *
     * @throws Exception
     */
    @Test
    public void testOutputAsJsonNewPretty() throws Exception {

        Results res = ss.search("uid:\"4ff89cf0-86af-11de-90ed-001fc6f176ff\"", 0, 1);
        ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON_NEW, true);
        pr.close();

        String output = sw.toString();
        System.out.println("Json New Pretty is" + output);
        assertTrue(output.contains("urls"));
        assertTrue(output.contains("\"count\" : 1"));
        assertTrue(output.contains(""));
    }

}