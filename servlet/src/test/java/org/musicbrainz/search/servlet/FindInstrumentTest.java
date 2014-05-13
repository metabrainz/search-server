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
import org.musicbrainz.search.analysis.MusicbrainzSimilarity;
import org.musicbrainz.search.index.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Assumes an index has been built stored and in the data folder, I've picked a fairly obscure bside so hopefully
 * will not get added to another release
 */
public class FindInstrumentTest {

    private AbstractSearchServer ss;
    private AbstractDismaxSearchServer sd;


    @Before
    public void setUp() throws Exception {
        ObjectFactory of = new ObjectFactory();
        RAMDirectory ramDir = new RAMDirectory();
        Analyzer analyzer = DatabaseIndex.getAnalyzer(InstrumentIndexField.class);
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION, analyzer);
        writerConfig.setSimilarity(new MusicbrainzSimilarity());
        IndexWriter writer = new IndexWriter(ramDir, writerConfig);

        {
            MbDocument doc = new MbDocument();

            Instrument instrument = of.createInstrument();
            doc.addField(InstrumentIndexField.INSTRUMENT_ID, "ff571ff4-04cb-4b9c-8a1c-354c330f863c");
            instrument.setId("ff571ff4-04cb-4b9c-8a1c-354c330f863c");

            doc.addField(InstrumentIndexField.INSTRUMENT, "Trombone");
            instrument.setName("Trombone");

            doc.addField(InstrumentIndexField.DESCRIPTION, "Brassy");
            instrument.setDescription("Brassy");

            doc.addField(InstrumentIndexField.COMMENT, "series 2 was best");
            instrument.setDisambiguation("series 2 was best");

            doc.addField(InstrumentIndexField.ALIAS, "Tromba");
            AliasList aliasList = of.createAliasList();
            Alias alias = of.createAlias();
            aliasList.getAlias().add(alias);
            alias.setContent("Tromba");
            instrument.setAliasList(aliasList);

            doc.addField(InstrumentIndexField.TYPE, "Brass");
            instrument.setType("Brass");

            doc.addField(InstrumentIndexField.INSTRUMENT_STORE, MMDSerializer.serialize(instrument));
            writer.addDocument(doc.getLuceneDocument());
        }


        {
            MbDocument doc = new MbDocument();
            Instrument instrument = of.createInstrument();
            doc.addField(MetaIndexField.META, MetaIndexField.META_VALUE);
            doc.addNumericField(MetaIndexField.LAST_UPDATED, new Date().getTime());
            doc.addField(LabelIndexField.LABEL_STORE, MMDSerializer.serialize(instrument));
            writer.addDocument(doc.getLuceneDocument());
        }

        writer.close();
        SearcherManager searcherManager = new SearcherManager(ramDir, new MusicBrainzSearcherFactory(ResourceType.INSTRUMENT));
        ss = new InstrumentSearch(searcherManager);
        sd = new InstrumentDismaxSearch(ss);
    }

    @Test
    public void testFindInstrumentById() throws Exception {
        Results res = ss.search("iid:\"ff571ff4-04cb-4b9c-8a1c-354c330f863c\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(InstrumentIndexField.INSTRUMENT_ID));
        assertEquals("Trombone", doc.get(InstrumentIndexField.INSTRUMENT));
    }

    @Test
    public void testFindInstrumentByName() throws Exception {
        Results res = ss.search("instrument:\"Trombone\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(InstrumentIndexField.INSTRUMENT_ID));
        assertEquals("Trombone", doc.get(InstrumentIndexField.INSTRUMENT));
    }


    @Test
    public void testFindInstrumentByDesciption() throws Exception {
        Results res = ss.search("description:\"Brassy\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(InstrumentIndexField.INSTRUMENT_ID));
        assertEquals("Trombone", doc.get(InstrumentIndexField.INSTRUMENT));
    }

    @Test
    public void testFindInstrumentByComment() throws Exception {
        Results res = ss.search("comment:\"series 2 was best\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(InstrumentIndexField.INSTRUMENT_ID));
        assertEquals("Trombone", doc.get(InstrumentIndexField.INSTRUMENT));
    }

    @Test
    public void testFindInstrumentByDismax1() throws Exception {
        Results res = sd.search("Trombone", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(InstrumentIndexField.INSTRUMENT_ID));
        assertEquals("Trombone", doc.get(InstrumentIndexField.INSTRUMENT));
    }


    @Test
    public void testFindInstrumentByDefault() throws Exception {

        {
            Results res = ss.search("\"Trombone\"", 0, 10);
            assertEquals(1, res.getTotalHits());
            Result result = res.results.get(0);
            MbDocument doc = result.getDoc();
            assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(InstrumentIndexField.INSTRUMENT_ID));
            assertEquals("Trombone", doc.get(InstrumentIndexField.INSTRUMENT));
        }

    }

    @Test
    public void testFindInstrumentByType() throws Exception {
        Results res = ss.search("type:\"Brass\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();

        //(This will always come first because searcher sots by score and then docno, and this doc added first)
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(InstrumentIndexField.INSTRUMENT_ID));
        assertEquals("Trombone", doc.get(InstrumentIndexField.INSTRUMENT));
    }


    /**
     * @throws Exception exception
     */
    @Test
    public void testOutputJson() throws Exception {

        Results res = ss.search("instrument:\"Trombone\"", 0, 10);
        org.musicbrainz.search.servlet.mmd2.ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON);
        pr.close();

        String output = sw.toString();
        System.out.println("Json is" + output);

        assertTrue(output.contains("id\":\"ff571ff4-04cb-4b9c-8a1c-354c330f863c\""));
        assertTrue(output.contains("\"instrument-list\""));
        assertTrue(output.contains("\"count\":1"));
        assertTrue(output.contains("\"offset\":0,"));
        assertTrue(output.contains("\"type\":\"Brass\""));
        assertTrue(output.contains("name\":\"Trombone\""));
        assertTrue(output.contains("\"description\":\"Brassy\""));
        assertTrue(output.contains("\"disambiguation\":\"series 2 was best\""));
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testOutputJsonNew() throws Exception {

        Results res = ss.search("instrument:\"Trombone\"", 0, 10);
        org.musicbrainz.search.servlet.mmd2.ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON_NEW);
        pr.close();

        String output = sw.toString();
        System.out.println("Json New is" + output);

        assertTrue(output.contains("id\":\"ff571ff4-04cb-4b9c-8a1c-354c330f863c\""));
        assertTrue(output.contains("\"instruments\""));
        assertTrue(output.contains("\"type\":\"Brass\""));
        assertTrue(output.contains("name\":\"Trombone\""));
        assertTrue(output.contains("\"description\":\"Brassy\""));
        assertTrue(output.contains("\"aliases\":[{\"name\":\"Tromba\",\"locale\":null,\"type\":null,\"primary\":null,\"begin-date\":null,\"end-date\":null}]"));
        assertTrue(output.contains("\"count\":1"));
        assertTrue(output.contains("\"offset\":0,"));
        assertTrue(output.contains("\"disambiguation\":\"series 2 was best\""));

    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testOutputJsonNewIdent() throws Exception {

        Results res = ss.search("instrument:\"Trombone\"", 0, 10);
        org.musicbrainz.search.servlet.mmd2.ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON_NEW, true);
        pr.close();

        String output = sw.toString();
        System.out.println("Json New Ident is" + output);
        assertTrue(output.contains("\"offset\" : 0"));

    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testOutputXmlIdent() throws Exception {

        Results res = ss.search("instrument:\"Trombone\"", 0, 10);
        org.musicbrainz.search.servlet.mmd2.ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_XML, true);
        pr.close();

        String output = sw.toString();
        System.out.println("Xml Ident is" + output);
        assertTrue(output.contains("<instrument id=\"ff571ff4-04cb-4b9c-8a1c-354c330f863c\" type=\"Brass\" ext:score=\"100\">"));
        assertTrue(output.contains("<instrument-list count=\"1\" offset=\"0\">"));
        assertTrue(output.contains("<name>Trombone</name>"));
        assertTrue(output.contains("<description>Brassy</description>"));
        assertTrue(output.contains("<disambiguation>series 2 was best</disambiguation>"));
        assertTrue(output.contains("<alias>Tromba</alias>"));


    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testOutputXml() throws Exception {

        Results res = ss.search("instrument:\"Trombone\"", 0, 10);
        org.musicbrainz.search.servlet.mmd2.ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_XML);
        pr.close();

        String output = sw.toString();
        System.out.println("Xml Ident is" + output);
        assertTrue(output.contains("<instrument id=\"ff571ff4-04cb-4b9c-8a1c-354c330f863c\" type=\"Brass\" ext:score=\"100\">"));
        assertTrue(output.contains("<instrument-list count=\"1\" offset=\"0\">"));
        assertTrue(output.contains("<name>Trombone</name>"));
        assertTrue(output.contains("<description>Brassy</description>"));
        assertTrue(output.contains("<disambiguation>series 2 was best</disambiguation>"));
        assertTrue(output.contains("<alias>Tromba</alias>"));

    }
}