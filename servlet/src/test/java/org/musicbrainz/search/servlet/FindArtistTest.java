package org.musicbrainz.search.servlet;

import junit.framework.TestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.MusicbrainzSimilarity;
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.index.ArtistType;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.servlet.mmd1.ArtistMmd1XmlWriter;
import org.musicbrainz.search.servlet.mmd1.Mmd1XmlWriter;
import org.musicbrainz.search.servlet.mmd2.ArtistWriter;
import org.musicbrainz.search.servlet.mmd2.ResultsWriter;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Test retrieving artist from index and Outputting as Xml
 */
public class FindArtistTest extends TestCase {

    private SearchServer ss;
    private SearchServer sd;

    public FindArtistTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        RAMDirectory ramDir = new RAMDirectory();
        Analyzer analyzer = DatabaseIndex.getAnalyzer(ArtistIndexField.class);
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        writerConfig.setSimilarity(new MusicbrainzSimilarity());
        IndexWriter writer = new IndexWriter(ramDir, writerConfig);

                
        //General Purpose Artist
        {
            MbDocument doc = new MbDocument();
            doc.addField(ArtistIndexField.ARTIST_ID, "4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
            doc.addField(ArtistIndexField.ARTIST, "Farming Incident");
            doc.addField(ArtistIndexField.SORTNAME, "Incident, Farming");
            doc.addField(ArtistIndexField.BEGIN, "1999-04");
            doc.addField(ArtistIndexField.TYPE, "Group");
            doc.addField(ArtistIndexField.COMMENT, "the real one");
            doc.addField(ArtistIndexField.COUNTRY, "AF");
            doc.addField(ArtistIndexField.GENDER, "male");
            doc.addField(ArtistIndexField.TAG, "thrash");
            doc.addField(ArtistIndexField.TAGCOUNT, "5");
            doc.addField(ArtistIndexField.TAG, "güth");
            doc.addField(ArtistIndexField.TAGCOUNT, "11");
            doc.addField(ArtistIndexField.IPI,"1001");

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
            writer.addDocument(doc.getLuceneDocument());
        }

        //Artist, type person unknown gender
        {
            MbDocument doc = new MbDocument();
            doc.addField(ArtistIndexField.ARTIST_ID, "dde4879c-5e88-4385-b131-bf65296bf245");
            doc.addField(ArtistIndexField.ARTIST, "PJ Harvey");
            doc.addField(ArtistIndexField.TYPE, ArtistType.PERSON.getName());
            doc.addField(ArtistIndexField.GENDER, "unknown");
            writer.addDocument(doc.getLuceneDocument());
        }

        writer.close();
        ss = new ArtistSearch(new IndexSearcher(ramDir, true));
        sd = new ArtistDismaxSearch(new IndexSearcher(ramDir, true));

    }


    public void testFindArtistById() throws Exception {
        Results res = ss.searchLucene("arid:\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.ARTIST));
        assertEquals("1999-04", doc.get(ArtistIndexField.BEGIN));
        assertEquals("the real one", doc.get(ArtistIndexField.COMMENT));
        assertNull(doc.get(ArtistIndexField.END));
        assertNull(doc.get(ArtistIndexField.ALIAS));
        assertEquals("Incident, Farming", doc.get(ArtistIndexField.SORTNAME));
        assertEquals("Group", doc.get(ArtistIndexField.TYPE));
    }

    public void testFindArtistByName() throws Exception {
        Results res = ss.searchLucene("artist:\"Farming Incident\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
    }

    public void testFindArtistDismaxSingleTerm() throws Exception {
        Results res = sd.searchLucene("Farming", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
    }

    public void testFindArtistDismaxPhrase() throws Exception {
        Results res = sd.searchLucene("Farming Incident", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
    }

    public void testFindArtistDismaxFuzzy() throws Exception {
            Results res = sd.searchLucene("Farmin", 0, 10);
            assertEquals(1, res.totalHits);
            Result result = res.results.get(0);
            MbDocument doc = result.doc;
            assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
        }

    public void testFindArtistBySortName() throws Exception {
        Results res = ss.searchLucene("sortname:\"Incident, Farming\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
    }


    public void testFindArtistByType() throws Exception {
        Results res = ss.searchLucene("type:\"group\"", 0, 10);
        assertEquals(2, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
    }

    public void testFindArtistByIpi() throws Exception {
        Results res = ss.searchLucene("ipi:1001", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
    }

    public void testFindArtistByNumericType() throws Exception {
        Results res = ss.searchLucene("type:2", 0, 10);
        assertEquals(2, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
    }


    public void testFindArtistByBeginDate() throws Exception {
        Results res = ss.searchLucene("begin:\"1999-04\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
    }

    public void testFindArtistByEndDate() throws Exception {
        Results res = ss.searchLucene("end:\"1999-04\"", 0, 10);
        assertEquals(0, res.totalHits);
    }

    public void testFindArtistByTypePerson() throws Exception {
        Results res = ss.searchLucene("type:\"person\"", 0, 10);
        assertEquals(1, res.totalHits);
    }

    public void testFindArtistByAlias() throws Exception {
        Results res = ss.searchLucene("alias:\"Echo And The Bunnymen\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", doc.get(ArtistIndexField.ARTIST_ID));

    }

    public void testFindArtistByCountry() throws Exception {
        Results res = ss.searchLucene("country:\"af\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.ARTIST));
    }

    public void testFindArtistWithNoCountry() throws Exception {
        Results res = ss.searchLucene("country:unknown", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", doc.get(ArtistIndexField.ARTIST_ID));
    }

    public void testFindArtistWithNoGender() throws Exception {
        Results res = ss.searchLucene("gender:unknown", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("dde4879c-5e88-4385-b131-bf65296bf245", doc.get(ArtistIndexField.ARTIST_ID));
    }


    public void testFindArtistByCountryUppercase() throws Exception {
        Results res = ss.searchLucene("country:\"AF\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.ARTIST));
    }

    public void testFindArtistByGenderLowercase() throws Exception {
        Results res = ss.searchLucene("gender:\"male\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.ARTIST));
    }

    public void testFindArtistByGenderTitlecase() throws Exception {
        Results res = ss.searchLucene("gender:\"Male\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.ARTIST));
    }

    public void testFindArtistByTag() throws Exception {
        Results res = ss.searchLucene("tag:Thrash", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
        assertEquals("Farming Incident", doc.get(ArtistIndexField.ARTIST));
    }

    public void testFindArtistByDefaultField() throws Exception {

        //Matches on name field without it being specified
        {
            Results res = ss.searchLucene("\"Echo & The Bunnymen\"", 0, 10);
            assertEquals(1, res.totalHits);
            Result result = res.results.get(0);
            MbDocument doc = result.doc;
            assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", doc.get(ArtistIndexField.ARTIST_ID));
            assertEquals("Echo & The Bunnymen", doc.get(ArtistIndexField.ARTIST));
            assertEquals("1978", doc.get(ArtistIndexField.BEGIN));
            assertEquals("Echo & The Bunnymen", doc.get(ArtistIndexField.SORTNAME));
            assertEquals("group", doc.get(ArtistIndexField.TYPE));
        }

        //and alias field  field without it being specified
        {
            Results res = ss.searchLucene("\"Echo & The Bunnyman\"", 0, 10);
            assertEquals(1, res.totalHits);
            Result result = res.results.get(0);
            MbDocument doc = result.doc;
            assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", doc.get(ArtistIndexField.ARTIST_ID));
            assertEquals("Echo & The Bunnymen", doc.get(ArtistIndexField.ARTIST));
            assertEquals("1978", doc.get(ArtistIndexField.BEGIN));
            assertEquals("Echo & The Bunnymen", doc.get(ArtistIndexField.SORTNAME));
            assertEquals("group", doc.get(ArtistIndexField.TYPE));
        }

        //but doesn't search default fields if a field is specified 
        {
            Results res = ss.searchLucene("type:\"Echo & The Bunnyman\"", 0, 10);
            assertEquals(0, res.totalHits);

        }
    }

    /**
     * Tests get same results as
     * http://musicbrainz.org/ws/1/artist/?type=xml&query=%22Farming%20Incident%22
     *
     * @throws Exception
     */
    public void testOutputAsMmd1Xml() throws Exception {

        Results res = ss.searchLucene("artist:\"Farming Incident\"", 0, 1);
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
    public void testOutputXml() throws Exception {

        Results res = ss.searchLucene("artist:\"Farming Incident\"", 0, 1);
        ResultsWriter v1Writer = new ArtistWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        v1Writer.write(pr, res);
        pr.close();

        String output = sw.toString();
        System.out.println("Xml is" + output);
        assertTrue(output.contains("id=\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\""));
        assertTrue(output.contains("xmlns:ext=\"http://musicbrainz.org/ns/ext#-2.0\""));
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("type=\"Group\""));
        assertTrue(output.contains("<name>Farming Incident</name>"));
        assertTrue(output.contains("<sort-name>Incident, Farming</sort-name>"));
        assertTrue(output.contains("<life-span><begin>1999-04</begin></life-span>"));
        assertTrue(output.contains("<country>AF</country>"));
        assertTrue(output.contains("<gender>male</gender>"));
        assertTrue(output.contains("thrash</name>"));
        assertTrue(output.contains("güth</name>"));
        assertFalse(output.contains("alias"));
        assertFalse(output.contains("disambugation"));
    }

    /**
     * @throws Exception
     */
    public void testOutputXml2() throws Exception {

        Results res = ss.searchLucene("artist:\"Echo & the Bunnymen\"", 0, 1);
        ResultsWriter v1Writer = new ArtistWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        v1Writer.write(pr, res);
        pr.close();
        String output = sw.toString();
        System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("type=\"group\""));
        assertTrue(output.contains("<name>Echo &amp; The Bunnymen</name>"));
        assertTrue(output.contains("<sort-name>Echo &amp; The Bunnymen</sort-name>"));
        assertTrue(output.contains("<life-span><begin>1978</begin></life-span>"));
        assertTrue(output.contains("<alias>Echo And The Bunnymen</alias>"));
        assertTrue(output.contains("<alias>Echo &amp; The Bunnyman</alias>"));
        assertTrue(output.contains("<alias>Echo and The Bunymen</alias>"));
        assertTrue(output.contains("<alias>Echo &amp; The Bunymen</alias>"));
    }

    /**
     * @throws Exception
     */
    public void testOutputXml3() throws Exception {

        Results res = ss.searchLucene("artist:\"PJ Harvey\"", 0, 1);
        ResultsWriter v1Writer = new ArtistWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        v1Writer.write(pr, res);
        pr.close();
        String output = sw.toString();
        System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("type=\"person\""));
        assertTrue(output.contains("<name>PJ Harvey</name>"));
        assertFalse(output.contains("<gender>")); //Not shown because unknown
   }

    /**
     * Tests that & is converted to valid xml
     *
     * @throws Exception
     */
    public void testOutputAsMmd1XmlSpecialCharacters() throws Exception {

        Results res = ss.searchLucene("alias:\"Echo And The Bunnymen\"", 0, 1);
        Mmd1XmlWriter v1Writer = new ArtistMmd1XmlWriter();
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
    public void testOutputJson() throws Exception {

        Results res = ss.searchLucene("artist:\"Farming Incident\"", 0, 1);
        ResultsWriter writer = new ArtistWriter();
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
        assertTrue(output.contains("\"life-span\":{\"begin\":\"1999-04\"}"));
        assertTrue(output.contains("\"country\":\"AF\""));
        assertTrue(output.contains("\"gender\":\"male\""));
        assertTrue(output.contains("\"tag\":[{\"count\":5,\"name\":\"thrash\"},{\"count\":11,\"name\":\"güth\"}"));
    }

    public void testOutputJsonMultiple() throws Exception {
        Results res = ss.searchLucene("artist:\"Farming Incident\" OR artist:\"Echo & The Bunnymen\"", 0, 2);

        ResultsWriter writer = new ArtistWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON);
        pr.close();
        String output = sw.toString();
        assertTrue(output.contains("\"score\":\"100\""));
        assertTrue(output.contains("\"score\":\"31\""));
    }


}