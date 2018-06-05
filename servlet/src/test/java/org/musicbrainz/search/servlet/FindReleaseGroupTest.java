package org.musicbrainz.search.servlet;

import static org.junit.Assert.assertEquals;
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
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.MMDSerializer;
import org.musicbrainz.search.index.MetaIndexField;
import org.musicbrainz.search.index.ReleaseGroupIndexField;
import org.musicbrainz.search.servlet.mmd1.ReleaseGroupMmd1XmlWriter;
import org.musicbrainz.search.servlet.mmd1.ReleaseGroupType;

/**
 * Assumes an index has been built stored and in the data folder, I've picked a fairly obscure bside so hopefully
 * will not get added to another release
 */
public class FindReleaseGroupTest {

  private AbstractSearchServer ss;
  private AbstractDismaxSearchServer sd;



  @Before
  public void setUp() throws Exception {
    RAMDirectory ramDir = new RAMDirectory();
    Analyzer analyzer = DatabaseIndex.getAnalyzer(ReleaseGroupIndexField.class);
    IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
    IndexWriter writer = new IndexWriter(ramDir, writerConfig);
    ObjectFactory of = new ObjectFactory();
    //Release Group with single artist

    {
      MbDocument doc = new MbDocument();
      doc.addField(ReleaseGroupIndexField.RELEASEGROUP_ID, "2c7d81da-8fc3-3157-99c1-e9195ac92c45");
      doc.addField(ReleaseGroupIndexField.RELEASEGROUP, "Nobody's Twisting Your Arm");
      doc.addField(ReleaseGroupIndexField.RELEASE_ID, "2c7d81da-8fc3-3157-99c1-e9195ac92c46");
      doc.addField(ReleaseGroupIndexField.RELEASESTATUS, "Official");
      doc.addField(ReleaseGroupIndexField.RELEASE, "secret");
      doc.addField(ReleaseGroupIndexField.TAG, "indie");
      doc.addField(ReleaseGroupIndexField.TAGCOUNT, "101");

      doc.addField(ReleaseGroupIndexField.TYPE, "Single");
      doc.addField(ReleaseGroupIndexField.PRIMARY_TYPE, "Single");
      doc.addField(ReleaseGroupIndexField.SECONDARY_TYPE, "Live");

      doc.addField(ReleaseGroupIndexField.ARTIST_ID, "707622da-475f-48e1-905d-248718df6521");
      doc.addField(ReleaseGroupIndexField.ARTIST_NAME, "The Wedding Present");
      doc.addField(ReleaseGroupIndexField.ARTIST, "The Wedding Present");
      doc.addField(ReleaseGroupIndexField.ARTIST_NAMECREDIT, "The Wedding Present");
      doc.addNumericField(ReleaseGroupIndexField.NUM_RELEASES,1);

      Alias alias = of.createAlias();
      alias.setContent("The Weddoes");
      alias.setSortName("Weddoes, The");
      AliasList aliasList = of.createAliasList();
      aliasList.getAlias().add(alias);
      ArtistCredit ac = of.createArtistCredit();
      NameCredit nc = of.createNameCredit();
      Artist artist = of.createArtist();
      artist.setId("707622da-475f-48e1-905d-248718df6521");
      artist.setName("The Wedding Present");
      artist.setSortName("Wedding Present, The");
      artist.setAliasList(aliasList);
      nc.setArtist(artist);
      ac.getNameCredit().add(nc);
      doc.addField(ReleaseGroupIndexField.ARTIST_CREDIT, MMDSerializer.serialize(ac));
      writer.addDocument(doc.getLuceneDocument());

      //Release Group with multiple Artist and different name credit and no releases
      doc = new MbDocument();
      doc.addField(ReleaseGroupIndexField.RELEASEGROUP_ID, "0011c128-b1f2-300e-88cc-c33c30dce704");
      doc.addField(ReleaseGroupIndexField.RELEASEGROUP, "Epics");


      doc.addField(ReleaseGroupIndexField.TYPE, ReleaseGroupType.ALBUM.getName());
      doc.addField(ReleaseGroupIndexField.ARTIST_ID, "99845d0c-f239-4051-a6b1-4b5e9f7ede0b");
      doc.addField(ReleaseGroupIndexField.ARTIST_NAME, "Erich Kunzel");
      doc.addField(ReleaseGroupIndexField.ARTIST_NAMECREDIT, "Erich Kunzel");

      doc.addField(ReleaseGroupIndexField.ARTIST_ID, "d8fbd94c-cd06-4e8b-a559-761ad969d07e");
      doc.addField(ReleaseGroupIndexField.ARTIST_NAME, "The Cincinnati Pops Orchestra");
      doc.addField(ReleaseGroupIndexField.ARTIST_NAMECREDIT, "Cincinnati Pops");
      doc.addField(ReleaseGroupIndexField.COMMENT, "demo");

      doc.addField(ReleaseGroupIndexField.ARTIST, "Erich Kunzel and Cincinnati Pops");
      ac = of.createArtistCredit();
      nc = of.createNameCredit();
      artist = of.createArtist();
      artist.setId("99845d0c-f239-4051-a6b1-4b5e9f7ede0b");
      artist.setName("Erich Kunzel");
      artist.setSortName("Kunzel, Eric");
      nc.setArtist(artist);
      nc.setName("Erich Kunzel");
      nc.setJoinphrase("and");
      ac.getNameCredit().add(nc);
      nc = of.createNameCredit();
      artist = of.createArtist();
      artist.setId("d8fbd94c-cd06-4e8b-a559-761ad969d07e");
      artist.setName("The Cincinnati Pops Orchestra");
      artist.setSortName("Cincinnati Pops Orchestra, The");
      nc.setArtist(artist);
      nc.setName("Cincinnati Pops");
      ac.getNameCredit().add(nc);
      doc.addField(ReleaseGroupIndexField.ARTIST_CREDIT, MMDSerializer.serialize(ac));
      writer.addDocument(doc.getLuceneDocument());
    }

    {
      MbDocument doc = new MbDocument();
      doc.addField(MetaIndexField.META, MetaIndexField.META_VALUE);
      doc.addNumericField(MetaIndexField.LAST_UPDATED, new Date().getTime());
      writer.addDocument(doc.getLuceneDocument());
    }

    writer.close();
    SearcherManager searcherManager = new SearcherManager(ramDir, new MusicBrainzSearcherFactory(
        ResourceType.RELEASE_GROUP));
    ss = new ReleaseGroupSearch(searcherManager);
    sd = new ReleaseGroupDismaxSearch(ss);
  }

  @Test
  public void testFindReleaseGroupById() throws Exception {
    Results res = ss.search("rgid:\"2c7d81da-8fc3-3157-99c1-e9195ac92c45\"", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
    assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
    assertEquals("secret", doc.get(ReleaseGroupIndexField.RELEASE));
    assertEquals("Single", doc.get(ReleaseGroupIndexField.TYPE));
  }

  @Test
  public void testFindReleaseGroupByName() throws Exception {
    Results res = ss.search("releasegroup:\"Nobody's Twisting Your Arm\"", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
    assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
    assertEquals("Single", doc.get(ReleaseGroupIndexField.TYPE));
  }

  @Test
  public void testFindReleaseGroupByReleaseStatus() throws Exception {
    Results res = ss.search("status:official", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
    assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
    assertEquals("Single", doc.get(ReleaseGroupIndexField.TYPE));
  }
  @Test
  public void testFindReleaseGroupByDismax1() throws Exception {
    Results res = sd.search("Twisting", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
    assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
    assertEquals("Single", doc.get(ReleaseGroupIndexField.TYPE));
  }

  @Test
  public void testFindReleaseGroupByDismax2() throws Exception {
    Results res = sd.search("secret", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
    assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
    assertEquals("Single", doc.get(ReleaseGroupIndexField.TYPE));
  }

  @Test
  public void testFindReleaseGroupByDismax3() throws Exception {
    Results res = sd.search("wedding", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
    assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
    assertEquals("Single", doc.get(ReleaseGroupIndexField.TYPE));
  }

  @Test
  public void testFindReleaseGroupByRelease() throws Exception {
    Results res = ss.search("releasegroup:\"secret\"", 0, 10);
    assertEquals(0, res.getTotalHits());
    res = ss.search("release:secret", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
    assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
    assertEquals("Single", doc.get(ReleaseGroupIndexField.TYPE));
  }

  @Test
  public void testFindReleaseByNumberofReleases() throws Exception {
    Results res = ss.search("releases:1", 0, 10);
    assertEquals(1, res.getTotalHits());
  }

  @Test
  public void testFindReleaseGroupByReleaseId() throws Exception {
    Results res = ss.search("releaseid:\"2c7d81da-8fc3-3157-99c1-e9195ac92c46\"", 0, 10);
    assertEquals(0, res.getTotalHits());
    res = ss.search("release:secret", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
    assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
    assertEquals("Single", doc.get(ReleaseGroupIndexField.TYPE));
  }

  @Test
  public void testFindReleaseGroupByArtist() throws Exception {
    Results res = ss.search("artist:\"The Wedding Present\"", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
    assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
    assertEquals("Single", doc.get(ReleaseGroupIndexField.TYPE));
  }



  @Test
  public void testFindReleaseGroupByType() throws Exception {
    Results res = ss.search("type:\"single\"", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
    assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
    assertEquals("Single", doc.get(ReleaseGroupIndexField.TYPE));
  }

  @Test
  public void testFindReleaseGroupByPrimaryType() throws Exception {
    Results res = ss.search("primarytype:\"single\"", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
    assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
    assertEquals("Single", doc.get(ReleaseGroupIndexField.TYPE));
  }

  @Test
  public void testFindReleaseGroupBySecondaryType() throws Exception {
    Results res = ss.search("secondarytype:\"live\"", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
    assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
    assertEquals("Single", doc.get(ReleaseGroupIndexField.TYPE));
  }

  @Test
  public void testFindReleaseGroupByNumericType() throws Exception {
    Results res = ss.search("type:2", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
    assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
    assertEquals("Single", doc.get(ReleaseGroupIndexField.TYPE));
  }

  @Test
  public void testFindReleaseGroupByDefault() throws Exception {
    Results res = ss.search("\"secret\"", 0, 10);
    assertEquals(0, res.getTotalHits());
    res = ss.search("\"Nobody's Twisting Your Arm\"", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
    assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
    assertEquals("Single", doc.get(ReleaseGroupIndexField.TYPE));
    assertEquals("secret", doc.get(ReleaseGroupIndexField.RELEASE));

  }


  @Test
  public void testFindReleaseGroupByArtist2() throws Exception {
    Results res = ss.search("artist:\"Erich Kunzel\"", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("0011c128-b1f2-300e-88cc-c33c30dce704", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
    assertEquals("Epics", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
  }

  @Test
  public void testFindReleaseGroupByAllArtist2() throws Exception {
    Results res = ss.search("artist:\"Erich Kunzel and Cincinnati Pops\"", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("0011c128-b1f2-300e-88cc-c33c30dce704", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
    assertEquals("Epics", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
  }

  @Test
  public void testFindReleaseGroupByArtistName() throws Exception {
    Results res = ss.search("artistname:\"Erich Kunzel\"", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("0011c128-b1f2-300e-88cc-c33c30dce704", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
    assertEquals("Epics", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
  }

  @Test
  public void testFindReleaseGroupByAllArtistName() throws Exception {
    Results res = ss.search("artistname:\"Erich Kunzel\" AND artistname:\"Cincinnati Pops\"", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("0011c128-b1f2-300e-88cc-c33c30dce704", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
    assertEquals("Epics", doc.get(ReleaseGroupIndexField.RELEASEGROUP));

  }

  @Test
  public void testFindReleaseGroupByTag() throws Exception {
    Results res = ss.search("tag:indie", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("2c7d81da-8fc3-3157-99c1-e9195ac92c45", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
    assertEquals("Nobody's Twisting Your Arm", doc.get(ReleaseGroupIndexField.RELEASEGROUP));
  }

  @Test
  public void testFindReleaseGroupByComment() throws Exception {
    Results res = ss.search("comment:demo", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("0011c128-b1f2-300e-88cc-c33c30dce704", doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
    assertEquals("demo", doc.get(ReleaseGroupIndexField.COMMENT));
  }


  /**
   * Tests get same results as
   * http://musicbrainz.org/ws/1/release-group/?type=xml&query=%22Nobody%27s%20Twisting%20Your%20Arm%22
   *
   * @throws Exception exception
   */
  @Test
  public void testOutputAsAsMmd1Xml() throws Exception {

    Results res = ss.search("releasegroup:\"Nobody's Twisting Your Arm\"", 0, 1);
    ResultsWriter writer = new ReleaseGroupMmd1XmlWriter();
    StringWriter sw = new StringWriter();
    PrintWriter pr = new PrintWriter(sw);
    writer.write(pr, res);
    pr.close();
    String output = sw.toString();
    //System.out.println("Xml is" + output);
    assertTrue(output.contains("count=\"1\""));
    assertTrue(output.contains("offset=\"0\""));
    assertTrue(output.contains("id=\"2c7d81da-8fc3-3157-99c1-e9195ac92c45\""));
    assertTrue(output.contains("<title>Nobody's Twisting Your Arm</title>"));
    assertTrue(output.contains("<name>The Wedding Present</name>"));
    assertTrue(output.contains("<sort-name>Wedding Present, The</sort-name>"));
    assertTrue(output.contains("artist id=\"707622da-475f-48e1-905d-248718df6521\""));
    assertTrue(output.contains("type=\"Single\""));


  }

  /**
   * Tests get same results as
   * http://musicbrainz.org/ws/1/release-group/?type=xml&query=%22Nobody%27s%20Twisting%20Your%20Arm%22
   *
   * @throws Exception exception
   */
  @Test
  public void testOutputAsAsXml() throws Exception {

    Results res = ss.search("releasegroup:\"Nobody's Twisting Your Arm\"", 0, 1);
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
    assertTrue(output.contains("id=\"2c7d81da-8fc3-3157-99c1-e9195ac92c45\""));
    assertTrue(output.contains("<title>Nobody's Twisting Your Arm</title>"));
    assertTrue(output.contains("<name>The Wedding Present</name>"));
    assertTrue(output.contains("<sort-name>Wedding Present, The</sort-name>"));
    assertTrue(output.contains("<name-credit>"));
    assertTrue(output.contains("<artist-credit>"));
    assertTrue(output.contains("artist id=\"707622da-475f-48e1-905d-248718df6521\""));
    assertTrue(output.contains("type=\"Single\""));
    assertTrue(output.contains("release-list count=\"1\""));
    assertTrue(output.contains("<release id=\"2c7d81da-8fc3-3157-99c1-e9195ac92c46\"><title>secret</title><status>Official</status></release>"));
    assertTrue(output.contains("indie</name>"));
    assertTrue(output.contains("<secondary-type-list><secondary-type>Live</secondary-type></secondary-type-list>"));
  }


  /**
   * Tests get same results as
   * http://musicbrainz.org/ws/1/release-group/?type=xml&query=%22Nobody%27s%20Twisting%20Your%20Arm%22
   *
   * @throws Exception exception
   */
  @Test
  public void testOutputAsAsMmd1Xml2() throws Exception {

    Results res = ss.search("releasegroup:Epics", 0, 1);
    ResultsWriter writer = new ReleaseGroupMmd1XmlWriter();
    StringWriter sw = new StringWriter();
    PrintWriter pr = new PrintWriter(sw);
    writer.write(pr, res);
    pr.close();
    String output = sw.toString();
    //System.out.println("Xml is" + output);
    assertTrue(output.contains("count=\"1\""));
    assertTrue(output.contains("offset=\"0\""));
    assertTrue(output.contains("id=\"0011c128-b1f2-300e-88cc-c33c30dce704\""));
    assertTrue(output.contains("<title>Epics</title>"));
    assertTrue(output.contains("<name>Erich Kunzel</name>"));
    assertTrue(output.contains("<sort-name>Kunzel, Eric</sort-name>"));
    assertTrue(output.contains("artist id=\"99845d0c-f239-4051-a6b1-4b5e9f7ede0b\""));
    assertTrue(output.contains("type=\"Album\""));


  }

  /**
   * @throws Exception exception
   */
  @Test
  public void testOutputAsAsXml2() throws Exception {

    Results res = ss.search("releasegroup:Epics", 0, 1);
    ResultsWriter writer = ss.getMmd2Writer();
    StringWriter sw = new StringWriter();
    PrintWriter pr = new PrintWriter(sw);
    writer.write(pr, res);
    pr.close();
    String output = sw.toString();
    System.out.println("Xml is" + output);
    assertTrue(output.contains("count=\"1\""));
    assertTrue(output.contains("offset=\"0\""));
    assertTrue(output.contains("id=\"0011c128-b1f2-300e-88cc-c33c30dce704\""));
    assertTrue(output.contains("type=\"album\""));
    assertTrue(output.contains("<title>Epics</title>"));
    assertTrue(output.contains("<disambiguation>demo</disambiguation>"));
    assertTrue(output.contains("<name-credit joinphrase=\"and\">"));
    assertTrue(output.contains("<name>Erich Kunzel</name>"));
    assertTrue(output.contains("<sort-name>Kunzel, Eric</sort-name>"));
    assertTrue(output.contains("<name-credit>"));
    assertTrue(output.contains("<artist-credit>"));
    assertTrue(output.contains("artist id=\"99845d0c-f239-4051-a6b1-4b5e9f7ede0b\""));
    assertTrue(output.contains("<name-credit><name>Cincinnati Pops</name>"));
    assertTrue(output.contains("<name>The Cincinnati Pops Orchestra</name>"));
    assertTrue(output.contains("<sort-name>Cincinnati Pops Orchestra, The</sort-name>"));
    assertTrue(output.contains("artist id=\"d8fbd94c-cd06-4e8b-a559-761ad969d07e\""));
    assertTrue(output.contains("release-list count=\"0\""));
  }

  @Test
  public void testOutputJson() throws Exception {

    Results res = ss.search("releasegroup:Epics", 0, 10);
    ResultsWriter writer = ss.getMmd2Writer();
    StringWriter sw = new StringWriter();
    PrintWriter pr = new PrintWriter(sw);
    writer.write(pr, res, SearchServerServlet.RESPONSE_JSON);
    pr.close();

    String output = sw.toString();
    System.out.println("Json is" + output);

    assertTrue(output.contains("id\":\"0011c128-b1f2-300e-88cc-c33c30dce704\""));
    assertTrue(output.contains("\"count\":1"));
    assertTrue(output.contains("\"offset\":0,"));
    assertTrue(output.contains("\"type\":\"album\""));
    assertTrue(output.contains("title\":\"Epics\""));
  }

  @Test
  public void testOutputJsonMultiple() throws Exception {
    Results res = ss.search("rgid:2c7d81da-8fc3-3157-99c1-e9195ac92c45  OR artist:kunzel", 0, 10);
    org.musicbrainz.search.servlet.mmd2.ResultsWriter writer = ss.getMmd2Writer();
    StringWriter sw = new StringWriter();
    PrintWriter pr = new PrintWriter(sw);
    writer.write(pr, res, SearchServerServlet.RESPONSE_JSON);
    pr.close();
    String output = sw.toString();
    System.out.println("Json is" + output);

    assertTrue(output.contains("\"score\":100"));
    assertTrue(output.contains("\"score\":43"));
    assertTrue(output.contains("\"tag\":[{\"count\":101,\"name\":\"indie\"}"));
  }

  @Test
  public void testOutputJsonNew() throws Exception {

    Results res = ss.search("releasegroup:Epics", 0, 10);
    ResultsWriter writer = ss.getMmd2Writer();
    StringWriter sw = new StringWriter();
    PrintWriter pr = new PrintWriter(sw);
    writer.write(pr, res, SearchServerServlet.RESPONSE_JSON_NEW);
    pr.close();

    String output = sw.toString();
    System.out.println("Json New is" + output);
    assertTrue(output.contains("release-groups"));
    assertTrue(output.contains("id\":\"0011c128-b1f2-300e-88cc-c33c30dce704\""));
    assertTrue(output.contains("title\":\"Epics\""));
    assertTrue(output.contains("\"count\":1"));
    assertTrue(output.contains("\"offset\":0,"));
    assertTrue(output.contains("\"disambiguation\":\"demo\""));
    assertTrue(output.contains("\"name\":\"Erich Kunzel\""));
    assertTrue(output.contains("\"sort-name\":\"Kunzel, Eric\""));
    assertTrue(output.contains("\"joinphrase\":\"and\""));
    assertTrue(output.contains("\"id\":\"99845d0c-f239-4051-a6b1-4b5e9f7ede0b\""));




  }

  @Test
  public void testOutputJsonNewPretty() throws Exception {

    Results res = ss.search("releasegroup:Epics", 0, 10);
    ResultsWriter writer = ss.getMmd2Writer();
    StringWriter sw = new StringWriter();
    PrintWriter pr = new PrintWriter(sw);
    writer.write(pr, res, SearchServerServlet.RESPONSE_JSON_NEW,true);
    pr.close();

    String output = sw.toString();
    System.out.println("Json New Pretty is" + output);
    assertTrue(output.contains("\"offset\" : 0,"));

  }

  @Test
  public void testOutputJsonNewPretty2() throws Exception {

    Results res = ss.search("rgid:2c7d81da-8fc3-3157-99c1-e9195ac92c45", 0, 10);
    ResultsWriter writer = ss.getMmd2Writer();
    StringWriter sw = new StringWriter();
    PrintWriter pr = new PrintWriter(sw);
    writer.write(pr, res, SearchServerServlet.RESPONSE_JSON_NEW,true);
    pr.close();

    String output = sw.toString();
    System.out.println("Json New Pretty is" + output);
    assertTrue(output.contains("\"offset\" : 0,"));
    assertTrue(output.contains("\"tags\" : [ {"));
    assertTrue(output.contains("\"name\" : \"indie\""));

  }
}
