package org.musicbrainz.search.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.Date;

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
import org.musicbrainz.search.servlet.mmd1.ArtistMmd1XmlWriter;
import org.musicbrainz.search.servlet.mmd1.Mmd1XmlWriter;
import org.musicbrainz.search.servlet.mmd2.ResultsWriter;

/**
 * Test retrieving artist from index and Outputting as Xml
 */
public class FindArtistTest {

    private AbstractSearchServer ss;
    private AbstractDismaxSearchServer sd;


    @Before
    public void setUp() throws Exception {
        ObjectFactory of = new ObjectFactory();
        RAMDirectory ramDir = new RAMDirectory();
        Analyzer analyzer = DatabaseIndex.getAnalyzer(ArtistIndexField.class);
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION, analyzer);
        writerConfig.setSimilarity(new MusicbrainzSimilarity());
        IndexWriter writer = new IndexWriter(ramDir, writerConfig);


        //General Purpose Artist
        {
            MbDocument doc = new MbDocument();
            doc.addField(ArtistIndexField.ARTIST_ID, "4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
            doc.addField(ArtistIndexField.ARTIST, "Farming Incident");
            doc.addField(ArtistIndexField.SORTNAME, "Incident, Farming");
            doc.addField(ArtistIndexField.BEGIN, "1999-04");
            doc.addField(ArtistIndexField.ENDED, "true");
            doc.addField(ArtistIndexField.TYPE, "Group");
            doc.addField(ArtistIndexField.COMMENT, "the real one");
            doc.addField(ArtistIndexField.COUNTRY, "AF");
            doc.addField(ArtistIndexField.GENDER, "male");
            doc.addField(ArtistIndexField.TAG, "thrash");
            doc.addField(ArtistIndexField.TAG, "güth");
            doc.addField(ArtistIndexField.IPI, "1001");
            doc.addField(ArtistIndexField.IPI, "1002");

            Artist artist = of.createArtist();
            artist.setId("4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
            artist.setName("Farming Incident");
            artist.setSortName("Incident, Farming");
            LifeSpan lifespan = of.createLifeSpan();
            lifespan.setBegin("1999-04");
            lifespan.setEnded("true");
            artist.setLifeSpan(lifespan);
            artist.setType("Group");
            artist.setDisambiguation("the real one");
            artist.setCountry("AF");

            DefAreaElementInner area =  of.createDefAreaElementInner();
            area.setId("5302e264-1cf0-4d1f-aca7-2a6f89e34b36");
            area.setName("Afghanistan");
            area.setSortName("Afghanistan");
            artist.setArea(area);
            doc.addField(ArtistIndexField.AREA, "Afghanistan");

            DefAreaElementInner beginArea =  of.createDefAreaElementInner();
            beginArea.setId("6302e264-1cf0-4d1f-aca7-2a6f89e34b36");
            beginArea.setName("Canada");
            beginArea.setSortName("Canada");
            artist.setBeginArea(beginArea);
            doc.addField(ArtistIndexField.BEGIN_AREA, "Canada");

            doc.addField(ArtistIndexField.END_AREA, "-");

            artist.setGender("male");
            TagList tagList = of.createTagList();
            Tag tag = of.createTag();
            tag.setName("thrash");
            tag.setCount(BigInteger.valueOf(5));
            tagList.getTag().add(tag);

            tag = of.createTag();
            tag.setName("güth");
            tag.setCount(BigInteger.valueOf(11));
            tagList.getTag().add(tag);
            artist.setTagList(tagList);

            IpiList ipiList = of.createIpiList();
            ipiList.getIpi().add("1001");
            ipiList.getIpi().add("1002");
            artist.setIpiList(ipiList);

            doc.addField(ArtistIndexField.ARTIST_STORE, MMDSerializer.serialize(artist));
            writer.addDocument(doc.getLuceneDocument());
        }

        //Artist with & on name and aliases
        {
            MbDocument doc = new MbDocument();
            doc.addField(ArtistIndexField.ARTIST_ID, "ccd4879c-5e88-4385-b131-bf65296bf245");
            doc.addField(ArtistIndexField.ARTIST, "Echo & The Bunnymen");
            doc.addField(ArtistIndexField.SORTNAME, "Echo & The Bunnymen");
            doc.addField(ArtistIndexField.BEGIN, "1978");
            doc.addField(ArtistIndexField.COUNTRY, "unknown");
            doc.addField(ArtistIndexField.TYPE, ArtistType.GROUP.getName());
            doc.addField(ArtistIndexField.ALIAS, "Echo And The Bunnymen");
            doc.addField(ArtistIndexField.ALIAS, "Echo & The Bunnyman");
            doc.addField(ArtistIndexField.ALIAS, "Echo and The Bunymen");
            doc.addField(ArtistIndexField.ALIAS, "Echo & The Bunymen");

            Artist artist = of.createArtist();
            artist.setId("ccd4879c-5e88-4385-b131-bf65296bf245");

            artist.setName("Echo & The Bunnymen");
            artist.setSortName("Echo & The Bunnymen");
            LifeSpan lifespan = of.createLifeSpan();
            lifespan.setBegin("1978");
            artist.setLifeSpan(lifespan);
            artist.setType("Group");

            AliasList aliasList = of.createAliasList();
            Alias alias = of.createAlias();
            alias.setContent("Echo And The Bunnymen");
            alias.setSortName("Buunymen, Echo And The");
            alias.setPrimary("primary");
            alias.setLocale("en");
            alias.setType("Artist name");
            alias.setBeginDate("1978-05-01");
            aliasList.getAlias().add(alias);

            alias = of.createAlias();
            alias.setContent("Echo & The Bunnyman");
            aliasList.getAlias().add(alias);

            alias = of.createAlias();
            alias.setContent("Echo and The Bunymen");
            aliasList.getAlias().add(alias);

            alias = of.createAlias();
            alias.setContent("Echo & The Bunymen");
            aliasList.getAlias().add(alias);
            artist.setAliasList(aliasList);
            doc.addField(ArtistIndexField.ARTIST_STORE, MMDSerializer.serialize(artist));
            writer.addDocument(doc.getLuceneDocument());
        }

        //Artist, type person unknown gender
        {
            MbDocument doc = new MbDocument();
            doc.addField(ArtistIndexField.ARTIST_ID, "dde4879c-5e88-4385-b131-bf65296bf245");
            doc.addField(ArtistIndexField.ARTIST, "PJ Harvey");
            doc.addField(ArtistIndexField.TYPE, "Person");
            doc.addField(ArtistIndexField.GENDER, "unknown");

            Artist artist = of.createArtist();
            artist.setId("dde4879c-5e88-4385-b131-bf65296bf245");
            artist.setName("PJ Harvey");
            artist.setType("Person");

            doc.addField(ArtistIndexField.ARTIST_STORE, MMDSerializer.serialize(artist));
            writer.addDocument(doc.getLuceneDocument());
        }

        {
            MbDocument doc = new MbDocument();
            doc.addField(MetaIndexField.META, MetaIndexField.META_VALUE);
            doc.addNumericField(MetaIndexField.LAST_UPDATED, new Date().getTime());
            writer.addDocument(doc.getLuceneDocument());
        }

        writer.close();
        SearcherManager searcherManager = new SearcherManager(ramDir, new MusicBrainzSearcherFactory(ResourceType.ARTIST));
        ss = new ArtistSearch(searcherManager);
        sd = new ArtistDismaxSearch(ss);

    }

    protected String getArtistId(MbDocument doc) {
        Artist artist = (Artist) MMDSerializer.unserialize(doc.get(ArtistIndexField.ARTIST_STORE), Artist.class);
        return artist.getId();
    }


    @Test
    public void testFindArtistById() throws Exception {
        Results res = ss.search("arid:\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", getArtistId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindArtistByName() throws Exception {
        Results res = ss.search("artist:\"Farming Incident\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", getArtistId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindArtistByArea() throws Exception {
        Results res = ss.search("area:\"Afghanistan\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", getArtistId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindArtistByBeginArea() throws Exception {
        Results res = ss.search("beginarea:\"Canada\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", getArtistId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindArtistByNoEndArea() throws Exception {
        Results res = ss.search("endarea:\"-\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", getArtistId(res.results.get(0).getDoc()));
    }


    @Test
    public void testFindArtistDismaxSingleTerm() throws Exception {
        Results res = sd.search("Farming", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", getArtistId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindArtistDismaxPhrase() throws Exception {
        Results res = sd.search("Farming Incident", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", getArtistId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindArtistDismaxFuzzy() throws Exception {
        Results res = sd.search("Farmin", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", getArtistId(res.results.get(0).getDoc()));

    }

    @Test
    public void testFindArtistBySortName() throws Exception {
        Results res = ss.search("sortname:\"Incident, Farming\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", getArtistId(res.results.get(0).getDoc()));

    }


    @Test
    public void testFindArtistByType() throws Exception {
        Results res = ss.search("type:\"group\"", 0, 10);
        assertEquals(2, res.getTotalHits());
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", getArtistId(res.results.get(0).getDoc()));

    }

    @Test
    public void testFindArtistByIpi() throws Exception {
        Results res = ss.search("ipi:1001", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", getArtistId(res.results.get(0).getDoc()));

    }

    @Test
    public void testFindArtistByNumericType() throws Exception {
        Results res = ss.search("type:2", 0, 10);
        assertEquals(2, res.getTotalHits());
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", getArtistId(res.results.get(0).getDoc()));

    }

    @Test
    public void testFindArtistByBeginDate() throws Exception {
        Results res = ss.search("begin:\"1999-04\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", getArtistId(res.results.get(0).getDoc()));

    }

    @Test
    public void testFindArtistByEnded() throws Exception {
        Results res = ss.search("ended:\"true\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", getArtistId(res.results.get(0).getDoc()));

    }


    @Test
    public void testFindArtistByEndDate() throws Exception {
        Results res = ss.search("end:\"1999-04\"", 0, 10);
        assertEquals(0, res.getTotalHits());
    }

    @Test
    public void testFindArtistByTypePerson() throws Exception {
        Results res = ss.search("type:\"person\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("dde4879c-5e88-4385-b131-bf65296bf245", getArtistId(res.results.get(0).getDoc()));

    }

    @Test
    public void testFindArtistByAlias() throws Exception {
        Results res = ss.search("alias:\"Echo And The Bunnymen\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", getArtistId(res.results.get(0).getDoc()));


    }

    @Test
    public void testFindArtistByCountry() throws Exception {
        Results res = ss.search("country:\"af\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", getArtistId(res.results.get(0).getDoc()));

    }

    @Test
    public void testFindArtistWithNoCountry() throws Exception {
        Results res = ss.search("country:unknown", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", getArtistId(res.results.get(0).getDoc()));

    }

    @Test
    public void testFindArtistWithNoGender() throws Exception {
        Results res = ss.search("gender:unknown", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("dde4879c-5e88-4385-b131-bf65296bf245", getArtistId(res.results.get(0).getDoc()));

    }


    @Test
    public void testFindArtistByCountryUppercase() throws Exception {
        Results res = ss.search("country:\"AF\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", getArtistId(res.results.get(0).getDoc()));

    }

    @Test
    public void testFindArtistByGenderLowercase() throws Exception {
        Results res = ss.search("gender:\"male\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", getArtistId(res.results.get(0).getDoc()));

    }

    @Test
    public void testFindArtistByGenderTitlecase() throws Exception {
        Results res = ss.search("gender:\"Male\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", getArtistId(res.results.get(0).getDoc()));

    }

    @Test
    public void testFindArtistByTag() throws Exception {
        Results res = ss.search("tag:Thrash", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", getArtistId(res.results.get(0).getDoc()));

    }

    @Test
    public void testFindArtistByDefaultField() throws Exception {

        //Matches on name field without it being specified
        {
            Results res = ss.search("\"Echo & The Bunnymen\"", 0, 10);
            assertEquals(1, res.getTotalHits());
            assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", getArtistId(res.results.get(0).getDoc()));
        }

        //and alias field  field without it being specified
        {
            Results res = ss.search("\"Echo & The Bunnyman\"", 0, 10);
            assertEquals(1, res.getTotalHits());
            assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", getArtistId(res.results.get(0).getDoc()));
        }

        //but doesn't search default fields if a field is specified
        {
            Results res = ss.search("type:\"Echo & The Bunnyman\"", 0, 10);
            assertEquals(0, res.getTotalHits());

        }
    }

    @Test
    public void testFindArtistByExcalamation() throws Exception {
        Results res = ss.search("Farming\\!", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", getArtistId(res.results.get(0).getDoc()));

    }

    /**
     * Tests get same results as
     * http://musicbrainz.org/ws/1/artist/?type=xml&query=%22Farming%20Incident%22
     *
     * @throws Exception
     */
    @Test
    public void testOutputAsMmd1Xml() throws Exception {

        Results res = ss.search("artist:\"Farming Incident\"", 0, 1);
        Mmd1XmlWriter v1Writer = new ArtistMmd1XmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        v1Writer.write(pr, res);
        pr.close();

        String output = sw.toString();
        System.out.println("Xml is" + output);
        //assertTrue(output.contains("<artist id=\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\""));  group comes before id in output
        //assertTrue(output.contains("<artist-list count=\"1\" offset=\"0\">"));               offset comes before count in output
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("type=\"Group\""));
        assertTrue(output.contains("<name>Farming Incident</name>"));
        assertTrue(output.contains("<sort-name>Incident, Farming</sort-name>"));
        assertTrue(output.contains("<life-span begin=\"1999-04\""));
        assertFalse(output.contains("end"));
        assertFalse(output.contains("alias"));
        assertFalse(output.contains("disambugation"));
    }


    /**
     * @throws Exception
     */
    @Test
    public void testOutputXml() throws Exception {

        Results res = ss.search("artist:\"Farming Incident\"", 0, 1);
        ResultsWriter v1Writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        v1Writer.write(pr, res);
        pr.close();

        String output = sw.toString();
        System.out.println("Xml1 is" + output);
        assertTrue(output.contains("id=\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\""));
        assertTrue(output.contains("xmlns:ext=\"http://musicbrainz.org/ns/ext#-2.0\""));
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("type=\"Group\""));
        assertTrue(output.contains("<name>Farming Incident</name>"));
        assertTrue(output.contains("<sort-name>Incident, Farming</sort-name>"));
        assertTrue(output.contains("<begin>1999-04</begin>"));
        assertTrue(output.contains("<country>AF</country>"));
        assertTrue(output.contains("<ended>true</ended>"));
        assertTrue(output.contains("<gender>male</gender>"));
        assertTrue(output.contains("<ipi-list><ipi>1001</ipi><ipi>1002</ipi></ipi-list>"));
        assertTrue(output.contains("thrash</name>"));
        assertTrue(output.contains("güth</name>"));
        assertFalse(output.contains("alias"));
        assertFalse(output.contains("disambugation"));
        assertTrue(output.contains("<area id=\"5302e264-1cf0-4d1f-aca7-2a6f89e34b36\"><name>Afghanistan</name><sort-name>Afghanistan</sort-name></area>"));
        assertTrue(output.contains("<begin-area id=\"6302e264-1cf0-4d1f-aca7-2a6f89e34b36\"><name>Canada</name><sort-name>Canada</sort-name></begin-area>"));
        assertFalse(output.contains("end-area"));

    }

    /**
     * @throws Exception
     */
    @Test
    public void testOutputXml2() throws Exception {

        Results res = ss.search("artist:\"Echo & the Bunnymen\"", 0, 1);
        ResultsWriter v1Writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        v1Writer.write(pr, res);
        pr.close();
        String output = sw.toString();
        System.out.println("Xml2 is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("type=\"Group\""));
        assertTrue(output.contains("<name>Echo &amp; The Bunnymen</name>"));
        assertTrue(output.contains("<sort-name>Echo &amp; The Bunnymen</sort-name>"));
        assertTrue(output.contains("<life-span><begin>1978</begin></life-span>"));
        assertTrue(output.contains("<alias locale=\"en\" sort-name=\"Buunymen, Echo And The\" type=\"Artist name\" primary=\"primary\" begin-date=\"1978-05-01\">Echo And The Bunnymen</alias>"));
        assertTrue(output.contains("<alias>Echo &amp; The Bunnyman</alias>"));
        assertTrue(output.contains("<alias>Echo and The Bunymen</alias>"));
        assertTrue(output.contains("<alias>Echo &amp; The Bunymen</alias>"));
    }

    /**
     * @throws Exception
     */
    @Test
    public void testOutputXml3() throws Exception {

        Results res = ss.search("artist:\"PJ Harvey\"", 0, 1);
        ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();
        String output = sw.toString();
        System.out.println("Xml3 is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("type=\"Person\""));
        assertTrue(output.contains("<name>PJ Harvey</name>"));
        assertFalse(output.contains("<gender>")); //Not shown because unknown
    }

    /**
     * Tests that & is converted to valid xml
     *
     * @throws Exception
     */
    @Test
    public void testOutputAsMmd1XmlSpecialCharacters() throws Exception {

        Results res = ss.search("alias:\"Echo And The Bunnymen\"", 0, 1);
        Mmd1XmlWriter v1Writer = ss.getMmd1Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        v1Writer.write(pr, res);
        pr.close();

        String output = sw.toString();
        //System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("type=\"Group\""));
        assertTrue(output.contains("<name>Echo &amp; The Bunnymen</name>"));
    }

    /**
     * @throws Exception
     */
    @Test
    public void testOutputJson() throws Exception {

        Results res = ss.search("artist:\"Farming Incident\"", 0, 1);
        ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON);
        pr.close();

        String output = sw.toString();
        System.out.println("Json is" + output);

        assertTrue(output.contains("id\":\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\""));
        assertTrue(output.contains("\"count\":1"));
        assertTrue(output.contains("\"offset\":0,"));
        assertTrue(output.contains("\"type\":\"Group\""));
        assertTrue(output.contains("name\":\"Farming Incident\""));
        assertTrue(output.contains("\"sort-name\":\"Incident, Farming\""));
        assertTrue(output.contains("\"begin\":\"1999-04\""));
        assertTrue(output.contains("\"ended\":\"true\""));
        assertTrue(output.contains("\"country\":\"AF\""));
        assertTrue(output.contains("\"gender\":\"male\""));
        assertTrue(output.contains("\"tag\":[{\"count\":5,\"name\":\"thrash\"},{\"count\":11,\"name\":\"güth\"}"));
        assertTrue(output.contains("\"id\":\"5302e264-1cf0-4d1f-aca7-2a6f89e34b36\","));
        assertTrue(output.contains("\"name\":\"Afghanistan\","));
        assertTrue(output.contains("\"sort-name\":\"Afghanistan\""));
        assertTrue(output.contains("\"id\":\"6302e264-1cf0-4d1f-aca7-2a6f89e34b36\","));
        assertTrue(output.contains("\"name\":\"Canada\","));
        assertTrue(output.contains("\"sort-name\":\"Canada\""));

    }


    @Test
    public void testOutputJsonMultiple() throws Exception {
        Results res = ss.search("artist:\"Farming Incident\" OR artist:\"Echo & The Bunnymen\"", 0, 2);

        ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON);
        pr.close();
        String output = sw.toString();
        assertTrue(output.contains("\"score\":\"100\""));
        assertTrue(output.contains("\"score\":\"31\""));
    }

    /**
     * @throws Exception
     */
    @Test
    public void testOutputJsonNew() throws Exception {

        Results res = ss.search("artist:\"Farming Incident\"", 0, 1);
        ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON_NEW);
        pr.close();

        String output = sw.toString();
        System.out.println("JNew is" + output);

        assertTrue(output.contains("id\":\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\""));
        assertTrue(output.contains("\"type\":\"Group\""));
        assertTrue(output.contains("name\":\"Farming Incident\""));
        assertTrue(output.contains("\"sort-name\":\"Incident, Farming\""));
        assertTrue(output.contains("\"begin\":\"1999-04\""));
        assertTrue(output.contains("\"ended\":true"));
        assertTrue(output.contains("\"country\":\"AF\""));
        assertTrue(output.contains("\"gender\":\"male\""));
        assertTrue(output.contains("\"tags\":[{"));
        assertTrue(output.contains("\"count\":5"));
        assertTrue(output.contains("\"name\":\"thrash\""));
        assertTrue(output.contains("\"name\":\"güth\""));
        assertTrue(output.contains("\"count\":1"));
        assertTrue(output.contains("\"offset\":0"));
        assertTrue(output.contains("\"id\":\"5302e264-1cf0-4d1f-aca7-2a6f89e34b36\","));
        assertTrue(output.contains("\"name\":\"Afghanistan\","));
        assertTrue(output.contains("\"sort-name\":\"Afghanistan\""));
        assertTrue(output.contains("\"id\":\"6302e264-1cf0-4d1f-aca7-2a6f89e34b36\","));
        assertTrue(output.contains("\"name\":\"Canada\","));
        assertTrue(output.contains("\"sort-name\":\"Canada\""));

    }

    /**
     * @throws Exception
     */
    @Test
    public void testOutputJsonNewPretty() throws Exception {

        Results res = ss.search("artist:\"Farming Incident\"", 0, 1);
        ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON_NEW, true);
        pr.close();

        String output = sw.toString();
        System.out.println("JSON New Pretty is" + output);
        assertTrue(output.contains("\"offset\" : 0"));

    }


    /**
     * @throws Exception
     */
    @Test
    public void testOutputJsonNewPrettyWithAliases() throws Exception {

        Results res = ss.search("arid:ccd4879c-5e88-4385-b131-bf65296bf245", 0, 1);
        ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON_NEW, true);
        pr.close();

        String output = sw.toString();
        System.out.println("JSON New Pretty is" + output);
        assertTrue(output.contains("\"sort-name\" : \"Echo & The Bunnymen\""));
        assertTrue(output.contains("\"type\" : \"Artist name\","));
        assertTrue(output.contains("\"primary\" : \"true\","));
        assertTrue(output.contains("\"begin-date\" : \"1978-05-01\""));
        assertTrue(output.contains(" \"end-date\" : null"));
        assertTrue(output.contains("\"name\" : \"Echo And The Bunnymen\""));
        assertTrue(output.contains("\"locale\" : null,"));
        assertTrue(output.contains("\"type\" : null,"));

    }

}