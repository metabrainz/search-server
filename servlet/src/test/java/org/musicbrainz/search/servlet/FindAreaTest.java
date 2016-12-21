package org.musicbrainz.search.servlet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Test;
import org.musicbrainz.mmd2.*;
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
        IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
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

            doc.addField(AreaIndexField.COMMENT, "A comment");
            area.setDisambiguation("A comment");

            doc.addField(AreaIndexField.ALIAS, "Afghany");
            AliasList aliasList = of.createAliasList();
            Alias alias = of.createAlias();
            aliasList.getAlias().add(alias);
            alias.setContent("Afghany");
            alias.setSortName("Afghan");
            area.setAliasList(aliasList);

            doc.addField(AreaIndexField.BEGIN, "1993");
            doc.addField(AreaIndexField.END, "2004");
            doc.addField(AreaIndexField.ENDED, "true");
            LifeSpan lifespan = of.createLifeSpan();
            area.setLifeSpan(lifespan);
            lifespan.setBegin("1993");
            lifespan.setEnd("2004");
            lifespan.setEnded("true");

            doc.addField(AreaIndexField.TYPE,"Country");
            area.setType("Country");


            Iso31661CodeList iso1 = of.createIso31661CodeList();
            iso1.getIso31661Code().add("AF");
            doc.addField(AreaIndexField.ISO1,"AF");
            doc.addField(AreaIndexField.ISO,"AF");

            area.setIso31661CodeList(iso1);

            Iso31662CodeList iso2 = of.createIso31662CodeList();
            iso2.getIso31662Code().add("North");
            doc.addField(AreaIndexField.ISO2,"North");
            doc.addField(AreaIndexField.ISO,"North");

            area.setIso31662CodeList(iso2);

            Iso31663CodeList iso3 = of.createIso31663CodeList();
            iso3.getIso31663Code().add("Kabu");
            doc.addField(AreaIndexField.ISO3,"Kabu");
            doc.addField(AreaIndexField.ISO,"Kabu");

            area.setIso31663CodeList(iso3);

            doc.addField(LabelIndexField.TAG, "desert");
            TagList tagList = of.createTagList();
            Tag tag = of.createTag();
            tag.setName("desert");
            tag.setCount(BigInteger.valueOf(22));
            tagList.getTag().add(tag);
            area.setTagList(tagList);

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
    public void testFindAreaByComment() throws Exception {
        Results res = ss.search("comment:\"a comment\"", 0, 10);
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
    //Always the same as name
    public void testFindAreaBySortName() throws Exception {
        Results res = ss.search("sortname:\"Afghanistan\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(AreaIndexField.AREA_ID));
        assertEquals("Afghanistan", doc.get(AreaIndexField.AREA));
    }

    @Test
    public void testFindAreaByDefaultName() throws Exception {
        Results res = ss.search("\"Afghanistan\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(AreaIndexField.AREA_ID));
        assertEquals("Afghanistan", doc.get(AreaIndexField.AREA));
    }


    @Test
    public void testFindAreaByDefaultAlias() throws Exception {
        Results res = ss.search("\"Afghany\"", 0, 10);
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
    public void testFindAreaByType() throws Exception {
        Results res = ss.search("type:\"Country\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(AreaIndexField.AREA_ID));
        assertEquals("Afghanistan", doc.get(AreaIndexField.AREA));
    }

    @Test
    public void testFindAreaByBegin() throws Exception {
        Results res = ss.search("begin:1993", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(AreaIndexField.AREA_ID));
        assertEquals("Afghanistan", doc.get(AreaIndexField.AREA));
    }

    @Test
    public void testFindAreaByEnd() throws Exception {
        Results res = ss.search("end:2004", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(AreaIndexField.AREA_ID));
        assertEquals("Afghanistan", doc.get(AreaIndexField.AREA));
    }

    @Test
    public void testFindAreaByEnded() throws Exception {
        Results res = ss.search("ended:true", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(AreaIndexField.AREA_ID));
        assertEquals("Afghanistan", doc.get(AreaIndexField.AREA));
    }

    @Test
    public void testFindAreaByIso1() throws Exception {
        Results res = ss.search("iso1:af", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(AreaIndexField.AREA_ID));
        assertEquals("Afghanistan", doc.get(AreaIndexField.AREA));
    }

    @Test
    public void testFindAreaByIso2() throws Exception {
        Results res = ss.search("iso2:north", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(AreaIndexField.AREA_ID));
        assertEquals("Afghanistan", doc.get(AreaIndexField.AREA));
    }


    @Test
    public void testFindAreaByIso3() throws Exception {
        Results res = ss.search("iso3:kabu", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(AreaIndexField.AREA_ID));
        assertEquals("Afghanistan", doc.get(AreaIndexField.AREA));
    }

    @Test
    public void testFindAreaByIso() throws Exception {
        Results res = ss.search("iso:af", 0, 10);
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

    @Test
    public void testFindAreaByDismax2() throws Exception {
        Results res = sd.search("afghany", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(AreaIndexField.AREA_ID));
        assertEquals("Afghanistan", doc.get(AreaIndexField.AREA));
    }

    @Test
    public void testFindAreaByDismax3() throws Exception {
        Results res = sd.search("af", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("ff571ff4-04cb-4b9c-8a1c-354c330f863c", doc.get(AreaIndexField.AREA_ID));
        assertEquals("Afghanistan", doc.get(AreaIndexField.AREA));
    }

    @Test
    public void testFindEventByTag() throws Exception {
        Results res = ss.search("tag:desert", 0, 10);
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

        assertTrue(output.contains("<begin>1993</begin"));
        assertTrue(output.contains("<end>2004</end>"));
        assertTrue(output.contains("<ended>true</ended>"));
        assertTrue(output.contains("<iso-3166-1-code-list><iso-3166-1-code>AF</iso-3166-1-code>"));
        assertTrue(output.contains("<iso-3166-2-code-list><iso-3166-2-code>North</iso-3166-2-code>"));
        assertTrue(output.contains("<iso-3166-3-code-list><iso-3166-3-code>Kabu</iso-3166-3-code>"));
        assertTrue(output.contains("<disambiguation>A comment</disambiguation"));

    }

    @Test
    public void testOutputAsXmlIdent() throws Exception {

        Results res = ss.search("area:\"Afghanistan\"", 0, 1);
        ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_XML, true);
        pr.close();
        String output = sw.toString();
        System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
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
        assertTrue(output.contains("life-span\":{\"begin\":\"1993\""));
        assertTrue(output.contains("\"ended\":\"true\""));
        assertTrue(output.contains("\"iso-3166-1-code-list\":{\"iso-3166-1-code\":[\"AF\"]}"));
        assertTrue(output.contains("\"iso-3166-2-code-list\":{\"iso-3166-2-code\":[\"North\"]}"));
        assertTrue(output.contains("\"iso-3166-3-code-list\":{\"iso-3166-3-code\":[\"Kabu\"]}"));
        assertTrue(output.contains("\"disambiguation\":\"A comment\","));

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

        assertTrue(output.contains("areas"));
        assertTrue(output.contains("id\":\"ff571ff4-04cb-4b9c-8a1c-354c330f863c\""));
        assertTrue(output.contains("\"type\":\"Country\""));
        assertTrue(output.contains("name\":\"Afghanistan\""));
        assertTrue(output.contains("\"sort-name\":\"Afghanistan\""));
        assertTrue(output.contains("\"aliases\":[{\"sort-name\":\"Afghan\",\"name\":\"Afghany\",\"locale\":null,\"type\":null,\"primary\":null,\"begin-date\":null,\"end-date\":null}"));
        assertTrue(output.contains("life-span\":{\"begin\":\"1993\""));
        assertTrue(output.contains("\"ended\":true"));
        assertTrue(output.contains("\"end\":\"2004\""));
        assertTrue(output.contains("\"iso-3166-1-codes\":[\"AF\"]"));
        assertTrue(output.contains("\"iso-3166-2-codes\":[\"North\"]"));
        assertTrue(output.contains("\"iso-3166-3-codes\":[\"Kabu\"]"));
        assertTrue(output.contains("\"disambiguation\":\"A comment\","));
        assertTrue(output.contains("\"tags\":[{\"count\":22,\"name\":\"desert\""));
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testOutputJsonNewIdent() throws Exception {

        Results res = ss.search("area:\"Afghanistan\"", 0, 10);
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
