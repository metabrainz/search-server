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
import org.musicbrainz.search.servlet.mmd2.AreaWriter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Assumes an index has been built stored and in the data folder, I've picked a fairly obscure bside so hopefully
 * will not get added to another release
 */
public class FindAreaTest {

    private AbstractSearchServer ss;
    private AbstractDismaxSearchServer sd;


    @Before
    public void setUp() throws Exception {
        ObjectFactory of = new ObjectFactory();
        RAMDirectory ramDir = new RAMDirectory();
        Analyzer analyzer = DatabaseIndex.getAnalyzer(AreaIndexField.class);
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION, analyzer);
        writerConfig.setSimilarity(new MusicbrainzSimilarity());
        IndexWriter writer = new IndexWriter(ramDir, writerConfig);

        {
            MbDocument doc = new MbDocument();

            DefAreaElementInner area = of.createDefAreaElementInner();
            doc.addField(AreaIndexField.AREA_ID, "ff571ff4-04cb-4b9c-8a1c-354c330f863c");
            area.setId("ff571ff4-04cb-4b9c-8a1c-354c330f863c");

            doc.addField(AreaIndexField.AREA, "Afghanistan");
            area.setName("Afghanistan");

            doc.addField(AreaIndexField.SORTNAME, "Afghanistan");
            area.setSortName("Afghanistan");


            doc.addField(AreaIndexField.ALIAS, "Afghany");
            AliasList aliasList = of.createAliasList();
            Alias alias = of.createAlias();
            aliasList.getAlias().add(alias);
            alias.setContent("Afghany");
            alias.setSortName("Afghan");
            area.setAliasList(aliasList);

            /*
            doc.addField(AreaIndexField.CODE, 1234);
            area.setAreaCode(BigInteger.valueOf(1234));

            doc.addField(AreaIndexField.BEGIN, "1993");
            doc.addField(AreaIndexField.END, "2004");
            doc.addField(AreaIndexField.ENDED, "true");
            LifeSpan lifespan = of.createLifeSpan();
            area.setLifeSpan(lifespan);
            lifespan.setBegin("1993");
            lifespan.setEnd("2004");
            lifespan.setEnded("true");
           */
            area.setType("Country");

            AreaList areaList = of.createAreaList();
            areaList.getArea().add(area);
            doc.addField(AreaIndexField.AREA_STORE, MMDSerializer.serialize(areaList));
            writer.addDocument(doc.getLuceneDocument());
        }

        {
            MbDocument doc = new MbDocument();
            AreaList areaList = of.createAreaList();
            DefAreaElementInner area = of.createDefAreaElementInner();
            areaList.getArea().add(area);
            doc.addField(MetaIndexField.META, MetaIndexField.META_VALUE);
            doc.addNumericField(MetaIndexField.LAST_UPDATED, new Date().getTime());
            doc.addField(AreaIndexField.AREA_STORE, MMDSerializer.serialize(areaList));
            writer.addDocument(doc.getLuceneDocument());
        }


        writer.close();
        SearcherManager searcherManager = new SearcherManager(ramDir, new MusicBrainzSearcherFactory(ResourceType.AREA));
        ss = new AreaSearch(searcherManager);
        sd = new AreaDismaxSearch(ss);
    }

    @Test
    public void testFindAreaById() throws Exception {
        Results res = ss.search("aid:\"ff571ff4-04cb-4b9c-8a1c-354c330f863c\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(AreaIndexField.AREA_ID));
        assertEquals("Afghanistan", doc.get(AreaIndexField.AREA));
    }

    @Test
    public void testFindAreaByName() throws Exception {
        Results res = ss.search("area:\"Afghanistan\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(AreaIndexField.AREA_ID));
        assertEquals("Afghanistan", doc.get(AreaIndexField.AREA));
    }

    @Test
    public void testFindAreaBySortName() throws Exception {
        Results res = ss.search("sortname:\"Afghanistan\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(AreaIndexField.AREA_ID));
        assertEquals("Afghanistan", doc.get(AreaIndexField.AREA));
    }

    @Test
    public void testFindAreaByAlias() throws Exception {
        Results res = ss.search("alias:\"Afghany\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(AreaIndexField.AREA_ID));
        assertEquals("Afghanistan", doc.get(AreaIndexField.AREA));
    }


    @Test
    public void testFindAreaByDismax1() throws Exception {
        Results res = sd.search("Afghanistan", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(AreaIndexField.AREA_ID));
        assertEquals("Afghanistan", doc.get(AreaIndexField.AREA));
    }


    /**
     * Tests get same results as
     * http://musicbrainz.org/ws/1/area/?type=xml&query=%22Jockey%20Slut%22
     *
     * @throws Exception exception
     */
    @Test
    public void testOutputAsXml() throws Exception {

        Results res = ss.search("area:\"Afghanistan\"", 0, 1);
        ResultsWriter writer = ss.getMmd2Writer();
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
        assertTrue(output.contains("type=\"Country\""));
        assertTrue(output.contains("<name>Afghanistan</name>"));
        assertTrue(output.contains("<sort-name>Afghanistan</sort-name>"));
        assertTrue(output.contains("<alias sort-name=\"Afghan\">Afghany</alias></alias-list>"));
        /*
        assertTrue(output.contains("<begin>1993</begin"));
        assertTrue(output.contains("<end>2004</end>"));
        assertTrue(output.contains("<area-code>1234</area-code>"));
        assertTrue(output.contains("<country>GB</country>"));
        assertTrue(output.contains("<ended>true</ended>"));
        assertTrue(output.contains("dance</name>"));
        assertTrue(output.contains("<ipi-list><ipi>1001</ipi></ipi-list>"));
  */

    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testOutputJson() throws Exception {

        Results res = ss.search("area:\"Afghanistan\"", 0, 10);
        org.musicbrainz.search.servlet.mmd2.ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON);
        pr.close();

        String output = sw.toString();
        System.out.println("Json is" + output);

        assertTrue(output.contains("id\":\"ff571ff4-04cb-4b9c-8a1c-354c330f863c\""));
        assertTrue(output.contains("\"count\":1"));
        assertTrue(output.contains("\"offset\":0,"));
        assertTrue(output.contains("\"type\":\"Country\""));
        assertTrue(output.contains("name\":\"Afghanistan\""));
        assertTrue(output.contains("\"sort-name\":\"Afghanistan\""));
        /*
        assertTrue(output.contains("life-span\":{\"begin\":\"1993\""));
        assertTrue(output.contains("\"area-code\":1234"));
        assertTrue(output.contains("\"country\":\"GB\""));
        assertTrue(output.contains("tag-list\":{\"tag\":[{\"count\":22,\"name\":\"dance\"}"));
        assertTrue(output.contains("\"ended\":\"true\""));
        */
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testOutputJsonNew() throws Exception {

        Results res = ss.search("area:\"Afghanistan\"", 0, 10);
        org.musicbrainz.search.servlet.mmd2.ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON_NEW);
        pr.close();

        String output = sw.toString();
        System.out.println("Json New is" + output);

        assertTrue(output.contains("id\":\"ff571ff4-04cb-4b9c-8a1c-354c330f863c\""));
        assertTrue(output.contains("\"type\":\"Country\""));
        assertTrue(output.contains("name\":\"Afghanistan\""));
        assertTrue(output.contains("\"sort-name\":\"Afghanistan\""));
        assertTrue(output.contains("\"aliases\":[{\"locale\":\"\",\"sort-name\":\"Afghan\",\"type\":\"\",\"primary\":\"false\",\"begin-date\":\"\",\"end-date\":\"\",\"value\":\"Afghany\""));

        /*
        assertTrue(output.contains("life-span\":{\"begin\":\"1993\""));
        assertTrue(output.contains("\"country\":\"GB\""));
        assertTrue(output.contains("\"tags\":[{\"count\":22,\"name\":\"dance\"}]"));
        assertTrue(output.contains("\"ended\":true"));
        assertTrue(output.contains("\"ipis\":[\"1001\""));
        assertTrue(output.contains("\"end\":\"2004\""));
        assertTrue(output.contains("\"area-code\":1234"));
        assertTrue(output.contains("\"count\":1"));
        assertTrue(output.contains("\"offset\":0,"));      */

    }

}