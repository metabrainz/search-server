package org.musicbrainz.search.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Test;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.MusicbrainzSimilarity;
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.index.ArtistType;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.MetaIndexField;
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
      doc.addField(ArtistIndexField.ENDED, "true");
      doc.addField(ArtistIndexField.TYPE, "Group");
      doc.addField(ArtistIndexField.COMMENT, "the real one");
      doc.addField(ArtistIndexField.COUNTRY, "AF");
      doc.addField(ArtistIndexField.GENDER, "male");
      doc.addField(ArtistIndexField.TAG, "thrash");
      doc.addField(ArtistIndexField.TAGCOUNT, "5");
      doc.addField(ArtistIndexField.TAG, "güth");
      doc.addField(ArtistIndexField.TAGCOUNT, "11");
      doc.addField(ArtistIndexField.IPI,"1001");
      doc.addField(ArtistIndexField.IPI,"1002");

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

  @Test
  public void testFindArtistById() throws Exception {
    Results res = ss.search("arid:\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\"", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
    assertEquals("Farming Incident", doc.get(ArtistIndexField.ARTIST));
    assertEquals("1999-04", doc.get(ArtistIndexField.BEGIN));
    assertEquals("the real one", doc.get(ArtistIndexField.COMMENT));
    assertNull(doc.get(ArtistIndexField.END));
    assertNull(doc.get(ArtistIndexField.ALIAS));
    assertEquals("Incident, Farming", doc.get(ArtistIndexField.SORTNAME));
    assertEquals("Group", doc.get(ArtistIndexField.TYPE));
  }

  @Test
  public void testFindArtistByName() throws Exception {
    Results res = ss.search("artist:\"Farming Incident\"", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
  }

  @Test
  public void testFindArtistDismaxSingleTerm() throws Exception {
    Results res = sd.search("Farming", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
  }

  @Test
  public void testFindArtistDismaxPhrase() throws Exception {
    Results res = sd.search("Farming Incident", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
  }

  @Test
  public void testFindArtistDismaxFuzzy() throws Exception {
    Results res = sd.search("Farmin", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
  }

  @Test
  public void testFindArtistBySortName() throws Exception {
    Results res = ss.search("sortname:\"Incident, Farming\"", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
  }


  @Test
  public void testFindArtistByType() throws Exception {
    Results res = ss.search("type:\"group\"", 0, 10);
    assertEquals(2, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
  }

  @Test
  public void testFindArtistByIpi() throws Exception {
    Results res = ss.search("ipi:1001", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
  }

  @Test
  public void testFindArtistByNumericType() throws Exception {
    Results res = ss.search("type:2", 0, 10);
    assertEquals(2, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
  }

  @Test
  public void testFindArtistByBeginDate() throws Exception {
    Results res = ss.search("begin:\"1999-04\"", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
  }

  @Test
  public void testFindArtistByEnded() throws Exception {
    Results res = ss.search("ended:\"true\"", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
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
  }

  @Test
  public void testFindArtistByAlias() throws Exception {
    Results res = ss.search("alias:\"Echo And The Bunnymen\"", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", doc.get(ArtistIndexField.ARTIST_ID));

  }

  @Test
  public void testFindArtistByCountry() throws Exception {
    Results res = ss.search("country:\"af\"", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
    assertEquals("Farming Incident", doc.get(ArtistIndexField.ARTIST));
  }

  @Test
  public void testFindArtistWithNoCountry() throws Exception {
    Results res = ss.search("country:unknown", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", doc.get(ArtistIndexField.ARTIST_ID));
  }

  @Test
  public void testFindArtistWithNoGender() throws Exception {
    Results res = ss.search("gender:unknown", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("dde4879c-5e88-4385-b131-bf65296bf245", doc.get(ArtistIndexField.ARTIST_ID));
  }


  @Test
  public void testFindArtistByCountryUppercase() throws Exception {
    Results res = ss.search("country:\"AF\"", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
    assertEquals("Farming Incident", doc.get(ArtistIndexField.ARTIST));
  }

  @Test
  public void testFindArtistByGenderLowercase() throws Exception {
    Results res = ss.search("gender:\"male\"", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
    assertEquals("Farming Incident", doc.get(ArtistIndexField.ARTIST));
  }

  @Test
  public void testFindArtistByGenderTitlecase() throws Exception {
    Results res = ss.search("gender:\"Male\"", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
    assertEquals("Farming Incident", doc.get(ArtistIndexField.ARTIST));
  }

  @Test
  public void testFindArtistByTag() throws Exception {
    Results res = ss.search("tag:Thrash", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
    assertEquals("Farming Incident", doc.get(ArtistIndexField.ARTIST));
  }

  @Test
  public void testFindArtistByDefaultField() throws Exception {

    //Matches on name field without it being specified
    {
      Results res = ss.search("\"Echo & The Bunnymen\"", 0, 10);
      assertEquals(1, res.getTotalHits());
      Result result = res.results.get(0);
      MbDocument doc = result.getDoc();
      assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", doc.get(ArtistIndexField.ARTIST_ID));
      assertEquals("Echo & The Bunnymen", doc.get(ArtistIndexField.ARTIST));
      assertEquals("1978", doc.get(ArtistIndexField.BEGIN));
      assertEquals("Echo & The Bunnymen", doc.get(ArtistIndexField.SORTNAME));
      assertEquals("group", doc.get(ArtistIndexField.TYPE));
    }

    //and alias field  field without it being specified
    {
      Results res = ss.search("\"Echo & The Bunnyman\"", 0, 10);
      assertEquals(1, res.getTotalHits());
      Result result = res.results.get(0);
      MbDocument doc = result.getDoc();
      assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", doc.get(ArtistIndexField.ARTIST_ID));
      assertEquals("Echo & The Bunnymen", doc.get(ArtistIndexField.ARTIST));
      assertEquals("1978", doc.get(ArtistIndexField.BEGIN));
      assertEquals("Echo & The Bunnymen", doc.get(ArtistIndexField.SORTNAME));
      assertEquals("group", doc.get(ArtistIndexField.TYPE));
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
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
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
    assertTrue(output.contains("type=\"person\""));
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
    assertTrue(output.contains("\"aliases\" : [ \"Echo And The Bunnymen\", \"Echo & The Bunnyman\", \"Echo and The Bunymen\", \"Echo & The Bunymen\" ]"));
  }

}