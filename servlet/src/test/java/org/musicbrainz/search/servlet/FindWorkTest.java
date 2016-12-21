package org.musicbrainz.search.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.Date;

import com.google.common.base.Strings;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Test;
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.MMDSerializer;
import org.musicbrainz.search.index.MetaIndexField;
import org.musicbrainz.search.index.WorkIndexField;

/**
 * Assumes an index has been built stored and in the data folder, I've picked a fairly obscure bside so hopefully
 * will not get added to another release
 */
public class FindWorkTest {

    private AbstractSearchServer ss;
    private AbstractDismaxSearchServer sd;

    @Before
    public void setUp() throws Exception {
        RAMDirectory ramDir = new RAMDirectory();
        ObjectFactory of = new ObjectFactory();

        Analyzer analyzer = DatabaseIndex.getAnalyzer(WorkIndexField.class);
        IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(ramDir, writerConfig);
        {
            MbDocument doc = new MbDocument();

            Work work = of.createWork();
            doc.addField(WorkIndexField.WORK_ID, "4ff89cf0-86af-11de-90ed-001fc6f176ff");
            work.setId("4ff89cf0-86af-11de-90ed-001fc6f176ff");
            doc.addField(WorkIndexField.WORK, "Symphony No. 5");
            work.setTitle("Symphony No. 5");

            IswcList iswcList = of.createIswcList();
            iswcList.getIswc().add("T-101779304-1");
            iswcList.getIswc().add("B-101779304-1");
            doc.addField(WorkIndexField.ISWC, "T-101779304-1");
            doc.addField(WorkIndexField.ISWC, "B-101779304-1");
            work.setIswcList(iswcList);

            doc.addField(WorkIndexField.ARTIST_ID, "1f9df192-a621-4f54-8850-2c5373b7eac9");
            doc.addField(WorkIndexField.ARTIST, "Пётр Ильич Чайковский");

            doc.addField(WorkIndexField.COMMENT, "demo");
            work.setDisambiguation("demo");

            doc.addField(WorkIndexField.LYRICS_LANG, "eng");
            work.setLanguage("eng");

            doc.addField(WorkIndexField.TYPE, "Opera");
            work.setType("Opera");

            AliasList aliasList = of.createAliasList();
            doc.addField(WorkIndexField.ALIAS, "Symp5");
            Alias alias = of.createAlias();
            alias.setContent("Symp5");
            aliasList.getAlias().add(alias);
            work.setAliasList(aliasList);

            doc.addField(WorkIndexField.TAG, "classical");
            TagList tagList = of.createTagList();
            Tag tag = of.createTag();
            tag.setName("classical");
            tag.setCount(BigInteger.valueOf(10));
            tagList.getTag().add(tag);
            work.setTagList(tagList);


            RelationList rl = of.createRelationList();
            rl.setTargetType("artist");
            {
                Relation relation = of.createRelation();
                Relation.AttributeList al = of.createRelationAttributeList();
                Artist artist1 = of.createArtist();
                artist1.setId("1f9df192-a621-4f54-8850-2c5373b7eac9");
                artist1.setName("Пётр Ильич Чайковский");
                artist1.setSortName("Пётр Ильич Чайковский");
                relation.setArtist(artist1);
                relation.setType("composer");
                relation.setDirection(DefDirection.BACKWARD);
                Relation.AttributeList.Attribute attribute = of.createRelationAttributeListAttribute();
                attribute.setContent("additional");
                al.getAttribute().add(attribute);
                relation.setAttributeList(al);
                rl.getRelation().add(relation);
            }
            {
                Relation relation = of.createRelation();
                Relation.AttributeList al = of.createRelationAttributeList();
                Artist artist1 = of.createArtist();
                artist1.setId("abcdefgh-a621-4f54-8850-2c5373b7eac9");
                artist1.setName("frank");
                artist1.setSortName("turner");
                relation.setArtist(artist1);
                relation.setType("writer");
                relation.setDirection(DefDirection.BACKWARD);
                rl.getRelation().add(relation);
            }
            work.getRelationList().add(rl);

            RelationList r2 = of.createRelationList();
            r2.setTargetType("recording");
            {
                Relation relation = of.createRelation();
                Relation.AttributeList al = of.createRelationAttributeList();
                Recording recording1 = of.createRecording();
                recording1.setId("abcdef");
                recording1.setTitle("fred");

                relation.setRecording(recording1);
                relation.setType("cover");
                r2.getRelation().add(relation);
            }

            doc.addField(WorkIndexField.RECORDING_ID, "abcdef");
            doc.addField(WorkIndexField.RECORDING, "fred");
            work.getRelationList().add(r2);

            String store = MMDSerializer.serialize(work);
            doc.addField(WorkIndexField.WORK_STORE, store);
            writer.addDocument(doc.getLuceneDocument());
        }

        {
            Work work = of.createWork();
            MbDocument doc = new MbDocument();
            doc.addField(WorkIndexField.WORK_ID, "bba1da16-6a0d-3299-aacf-042f8e13b0b7");
            work.setId("bba1da16-6a0d-3299-aacf-042f8e13b0b7");

            doc.addField(WorkIndexField.WORK, "Debaser");
            work.setTitle("Debaser");

            doc.addField(WorkIndexField.LYRICS_LANG, "esp");
            work.setLanguage("esp");

            doc.addField(WorkIndexField.TYPE, "Song");
            work.setType("Song");

            AliasList aliasList = of.createAliasList();
            doc.addField(WorkIndexField.ALIAS, "Debaser (Clif Norrell mix)");
            Alias alias = of.createAlias();
            alias.setContent("Debaser (Clif Norrell mix)");
            aliasList.getAlias().add(alias);
            work.setAliasList(aliasList);

            doc.addField(WorkIndexField.ISWC, "-");


            RelationList rl = of.createRelationList();
            rl.setTargetType("artist");
            {
                Relation relation = of.createRelation();
                Relation.AttributeList al = of.createRelationAttributeList();
                Artist artist = of.createArtist();
                artist.setId("789f6768-5830-4b08-8b4f-f38566b2eb1d");
                artist.setName("Black Francis");
                artist.setSortName("Francis, Black");
                doc.addField(WorkIndexField.ARTIST_ID, "789f6768-5830-4b08-8b4f-f38566b2eb1d");
                doc.addField(WorkIndexField.ARTIST, "Back Francis");
                relation.setArtist(artist);
                relation.setType("composer");
                relation.setDirection(DefDirection.BACKWARD);
                Relation.AttributeList.Attribute attribute = of.createRelationAttributeListAttribute();
                attribute.setContent("additional");
                al.getAttribute().add(attribute);
                relation.setAttributeList(al);
                rl.getRelation().add(relation);
            }
            work.getRelationList().add(rl);
            String store = MMDSerializer.serialize(work);
            doc.addField(WorkIndexField.WORK_STORE, store);

            writer.addDocument(doc.getLuceneDocument());
        }

        {
            MbDocument doc = new MbDocument();
            doc.addField(MetaIndexField.META, MetaIndexField.META_VALUE);
            doc.addNumericField(MetaIndexField.LAST_UPDATED, new Date().getTime());
            writer.addDocument(doc.getLuceneDocument());
        }

        writer.close();
        SearcherManager searcherManager = new SearcherManager(ramDir, new MusicBrainzSearcherFactory(ResourceType.WORK));
        ss = new

                WorkSearch(searcherManager);

        sd = new

                WorkDismaxSearch(ss);

    }

    @Test
    public void testFindWorkById() throws Exception {
        Results res = ss.search("wid:\"4ff89cf0-86af-11de-90ed-001fc6f176ff\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("4ff89cf0-86af-11de-90ed-001fc6f176ff", doc.get(WorkIndexField.WORK_ID));
        assertEquals("Symphony No. 5", doc.get(WorkIndexField.WORK));
    }

    @Test
    public void testFindWorkByName() throws Exception {
        Results res = ss.search("work:\"Symphony No. 5\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("4ff89cf0-86af-11de-90ed-001fc6f176ff", doc.get(WorkIndexField.WORK_ID));
        assertEquals("Symphony No. 5", doc.get(WorkIndexField.WORK));
    }

    @Test
    public void testFindWorkByLyricsLang() throws Exception {
        Results res = ss.search("lang:eng", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("4ff89cf0-86af-11de-90ed-001fc6f176ff", doc.get(WorkIndexField.WORK_ID));
        assertEquals("Symphony No. 5", doc.get(WorkIndexField.WORK));
    }

    @Test
    public void testFindWorkByDismax1() throws Exception {
        Results res = sd.search("Symphony No. 5", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("4ff89cf0-86af-11de-90ed-001fc6f176ff", doc.get(WorkIndexField.WORK_ID));
        assertEquals("Symphony No. 5", doc.get(WorkIndexField.WORK));
    }

    @Test
    public void testFindWorkByDismax2() throws Exception {
        Results res = sd.search("Symphony", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("4ff89cf0-86af-11de-90ed-001fc6f176ff", doc.get(WorkIndexField.WORK_ID));
        assertEquals("Symphony No. 5", doc.get(WorkIndexField.WORK));
    }

    @Test
    public void testFindWorkByDismax3() throws Exception {
        Results res = sd.search("demo", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("4ff89cf0-86af-11de-90ed-001fc6f176ff", doc.get(WorkIndexField.WORK_ID));
        assertEquals("Symphony No. 5", doc.get(WorkIndexField.WORK));
    }
    @Test
    public void testFindWorkByComment() throws Exception {
        Results res = ss.search("comment:demo", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("4ff89cf0-86af-11de-90ed-001fc6f176ff", doc.get(WorkIndexField.WORK_ID));
        assertEquals("demo", doc.get(WorkIndexField.COMMENT));
    }

    @Test
    public void testFindWorkByArtist() throws Exception {
        Results res = ss.search("artist:\"Пётр Ильич Чайковский\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("4ff89cf0-86af-11de-90ed-001fc6f176ff", doc.get(WorkIndexField.WORK_ID));
        assertEquals("Symphony No. 5", doc.get(WorkIndexField.WORK));
    }


    @Test
    public void testFindWorkByRecording() throws Exception {
        Results res = ss.search("recording:fred", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("4ff89cf0-86af-11de-90ed-001fc6f176ff", doc.get(WorkIndexField.WORK_ID));
        assertEquals("Symphony No. 5", doc.get(WorkIndexField.WORK));
    }

    @Test
    public void testFindWorkByRecordingId() throws Exception {
        Results res = ss.search("rid:abcdef", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("4ff89cf0-86af-11de-90ed-001fc6f176ff", doc.get(WorkIndexField.WORK_ID));
        assertEquals("Symphony No. 5", doc.get(WorkIndexField.WORK));
    }


    @Test
    public void testFindWorkByISWC() throws Exception {
        Results res = ss.search("iswc:\"T-101779304-1\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("4ff89cf0-86af-11de-90ed-001fc6f176ff", doc.get(WorkIndexField.WORK_ID));
        assertEquals("Symphony No. 5", doc.get(WorkIndexField.WORK));
    }

    @Test
    public void testFindWorkByType() throws Exception {
        Results res = ss.search("type:\"opera\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("4ff89cf0-86af-11de-90ed-001fc6f176ff", doc.get(WorkIndexField.WORK_ID));
        assertEquals("Symphony No. 5", doc.get(WorkIndexField.WORK));
    }

    @Test
    public void testFindWorkByAlias() throws Exception {
        Results res = ss.search("alias:symp5", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("4ff89cf0-86af-11de-90ed-001fc6f176ff", doc.get(WorkIndexField.WORK_ID));
        assertEquals("Symphony No. 5", doc.get(WorkIndexField.WORK));
    }

    @Test
    public void testFindWorkByTag() throws Exception {
        Results res = ss.search("tag:classical", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("4ff89cf0-86af-11de-90ed-001fc6f176ff", doc.get(WorkIndexField.WORK_ID));
        assertEquals("Symphony No. 5", doc.get(WorkIndexField.WORK));
    }

    @Test
    public void testFindWorkByDefaultUsingName() throws Exception {
        Results res = ss.search("\"Symphony No. 5\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("4ff89cf0-86af-11de-90ed-001fc6f176ff", doc.get(WorkIndexField.WORK_ID));
        assertEquals("Symphony No. 5", doc.get(WorkIndexField.WORK));
    }

    @Test
    public void testFindWorkByDefaultUsingAlias() throws Exception {
        Results res = ss.search("symp5", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("4ff89cf0-86af-11de-90ed-001fc6f176ff", doc.get(WorkIndexField.WORK_ID));
        assertEquals("Symphony No. 5", doc.get(WorkIndexField.WORK));
    }

    @Test
    public void testFindWorkByNoISWC() throws Exception {
        Results res = ss.search("iswc:\\-", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("bba1da16-6a0d-3299-aacf-042f8e13b0b7", doc.get(WorkIndexField.WORK_ID));
    }

    @Test
    public void testOutputNoISWCInXml() throws Exception {

        Results res = ss.search("iswc:\\-", 0, 1);
        ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_XML, true);
        pr.close();
        String output = sw.toString();
        System.out.println("Xml is" + output);
        assertTrue(!output.contains("<iswc>"));
    }

    /**
     * Tests
     *
     * @throws Exception
     */
    @Test
    public void testOutputAsXml() throws Exception {

        Results res = ss.search("work:\"Symphony No. 5\"", 0, 1);
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
        assertTrue(output.contains("<title>Symphony No. 5</title>"));
        assertTrue(output.contains("<name>Пётр Ильич Чайковский</name>"));
        assertTrue(output.contains("<disambiguation>demo</disambiguation>"));
        assertTrue(output.contains("<sort-name>Пётр Ильич Чайковский</sort-name>"));
        assertTrue(output.contains("<relation type=\"composer\""));
        assertTrue(output.contains("<iswc>T-101779304-1</iswc>"));
        assertTrue(output.contains("<iswc>B-101779304-1</iswc>"));
        assertTrue(output.contains("<language>eng</language>"));
        assertTrue(output.contains("<relation-list target-type=\"artist\">"));
        assertTrue(output.contains("<direction>backward</direction>"));
        assertTrue(output.contains("<attribute-list>"));
        assertTrue(output.contains("<attribute>additional</attribute>"));
        assertTrue(output.contains("type=\"Opera\""));
        assertTrue(output.contains("<alias>Symp5</alias>"));
        assertTrue(output.contains("<tag-list>"));
        assertTrue(output.contains("<tag count=\"10\">"));
        assertTrue(output.contains("<name>classical</name>"));
        assertTrue(output.contains("<relation-list target-type=\"recording\">"));
        assertTrue(output.contains("<relation type=\"cover\""));
        assertTrue(output.contains("<recording id=\"abcdef\">"));

    }

    /**
     * Tests
     *
     * @throws Exception
     */
    @Test
    public void testOutputAsJson() throws Exception {

        Results res = ss.search("work:\"Symphony No. 5\"", 0, 1);
        ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON);
        pr.close();

        String output = sw.toString();
        System.out.println("Json is" + output);

        assertTrue(output.contains("\"count\":1"));
        assertTrue(output.contains("\"offset\":0"));
        assertTrue(output.contains("\"work\":[{\"id\":\"4ff89cf0-86af-11de-90ed-001fc6f176ff\""));
        assertTrue(output.contains("\"type\":\"Opera\""));
        assertTrue(output.contains("\"score\":\"100\""));
        assertTrue(output.contains("\"title\":\"Symphony No. 5\""));
        assertTrue(output.contains("\"language\":\"eng\""));
        assertTrue(output.contains("\"iswc-list\":{\"iswc\":[\"T-101779304-1\",\"B-101779304-1\"]}"));
        assertTrue(output.contains("\"disambiguation\":\"demo\""));
        assertTrue(output.contains("\"alias-list\":{\"alias\":[\"Symp5\"]}"));
        assertTrue(output.contains("\"relation-list\":[{\"target-type\":\"artist\""));
        assertTrue(output.contains("\"relation\":[{\"type\":\"composer\",\"direction\":\"backward\",\"attribute-list\":{\"attribute\":[\"additional\"]}"));
        assertTrue(output.contains("\"artist\":{\"id\":\"1f9df192-a621-4f54-8850-2c5373b7eac9\""));
        assertTrue(output.contains("\"name\":\"Пётр Ильич Чайковский\""));
        assertTrue(output.contains("\"sort-name\":\"Пётр Ильич Чайковский\""));
        assertTrue(output.contains("\"tag-list\":{\"tag\""));
        assertTrue(output.contains("\"name\":\"classical\""));
        assertTrue(output.contains("\"count\":10"));
        assertTrue(output.contains(""));
    }

    /**
     * Tests
     *
     * @throws Exception
     */
    @Test
    public void testOutputAsJsonNew() throws Exception {

        Results res = ss.search("work:\"Symphony No. 5\"", 0, 1);
        ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON_NEW);
        pr.close();

        String output = sw.toString();
        System.out.println("Json New is" + output);
        assertTrue(output.contains("works"));
        assertTrue(output.contains("\"count\":1"));
        assertTrue(output.contains("\"offset\":0"));
        assertTrue(output.contains("\"works\":[{\"id\":\"4ff89cf0-86af-11de-90ed-001fc6f176ff\""));
        assertTrue(output.contains("\"type\":\"Opera\""));
        assertTrue(output.contains("\"score\":\"100\""));
        assertTrue(output.contains("\"title\":\"Symphony No. 5\""));
        assertTrue(output.contains("\"language\":\"eng\""));
        assertTrue(output.contains("iswcs\":[\"T-101779304-1\",\"B-101779304-1\"]"));
        assertTrue(output.contains("\"disambiguation\":\"demo\""));
        assertTrue(output.contains("\"aliases\":[{\"name\":\"Symp5\",\"locale\":null,\"type\":null,\"primary\":null,\"begin-date\":null,\"end-date\":null}]"));
        assertTrue(output.contains("\"relations\":[{"));
        assertTrue(output.contains("\"artist\":{\"id\":\"1f9df192-a621-4f54-8850-2c5373b7eac9\""));
        assertTrue(output.contains("\"name\":\"Пётр Ильич Чайковский\""));
        assertTrue(output.contains("\"sort-name\":\"Пётр Ильич Чайковский\""));
        assertTrue(output.contains("\"tags\":[{\"count\":10,\"name\":\"classical\"}"));
        assertTrue(output.contains("\"count\":10"));
        assertTrue(output.contains(""));
    }

    /**
     * Tests
     *
     * @throws Exception
     */
    @Test
    public void testOutputAsJsonNewPretty() throws Exception {

        Results res = ss.search("work:\"Symphony No. 5\"", 0, 1);
        ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON_NEW, true);
        pr.close();

        String output = sw.toString();
        System.out.println("Json New Pretty is" + output);

        assertTrue(output.contains("\"count\" : 1"));
        assertTrue(output.contains(""));
    }


    @Test
    public void testOutputAsXmlIsPretty() throws Exception {

        Results res = ss.search("work:\"Symphony No. 5\"", 0, 1);
        ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_XML, true);
        pr.close();
        String output = sw.toString();
        System.out.println("Xml is" + output);
        assertTrue(output.split("\n").length > 20);

    }
}
