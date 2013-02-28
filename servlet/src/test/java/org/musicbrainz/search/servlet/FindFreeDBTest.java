package org.musicbrainz.search.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Test;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.FreeDBIndexField;
import org.musicbrainz.search.servlet.mmd2.FreeDBWriter;
import org.musicbrainz.search.servlet.mmd2.ResultsWriter;
/**
 * Test retrieving FreeDB entries from index and Outputting as Html
 */
public class FindFreeDBTest  {

  private AbstractSearchServer ss;

  @Before
  public void setUp() throws Exception {
    RAMDirectory ramDir = new RAMDirectory();
    Analyzer analyzer = DatabaseIndex.getAnalyzer(FreeDBIndexField.class);
    IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
    IndexWriter writer = new IndexWriter(ramDir, writerConfig);

    //A complete FreeDB entry
    {
      MbDocument doc = new MbDocument();
      doc.addField(FreeDBIndexField.ARTIST, "Ska-P");
      doc.addField(FreeDBIndexField.TITLE, "L\u00e1grimas & Gozos");
      doc.addField(FreeDBIndexField.CATEGORY, "folk");
      doc.addField(FreeDBIndexField.DISCID, "c20c4b0d");
      doc.addField(FreeDBIndexField.TRACKS, "13");
      doc.addField(FreeDBIndexField.YEAR, "2008");
      writer.addDocument(doc.getLuceneDocument());
    }

    writer.close();
    SearcherManager searcherManager = new SearcherManager(ramDir, new MusicBrainzSearcherFactory(ResourceType.FREEDB));
    ss = new FreeDBSearch(searcherManager);
  }

  @Test
  public void testSearchFreeDBByArtist() throws Exception {
    Results res = ss.search("artist:\"Ska-P\"", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("Ska-P", doc.get(FreeDBIndexField.ARTIST));
    assertEquals("L\u00e1grimas & Gozos", doc.get(FreeDBIndexField.TITLE));
    assertEquals("folk", doc.get(FreeDBIndexField.CATEGORY));
    assertEquals("c20c4b0d", doc.get(FreeDBIndexField.DISCID));
    assertEquals("13", doc.get(FreeDBIndexField.TRACKS));
    assertEquals("2008", doc.get(FreeDBIndexField.YEAR));
  }

  @Test
  public void testSearchFreeDBByTitle() throws Exception {
    Results res = ss.search("title:\"L\u00e1grimas & Gozos\"", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("Ska-P", doc.get(FreeDBIndexField.ARTIST));
    assertEquals("L\u00e1grimas & Gozos", doc.get(FreeDBIndexField.TITLE));
    assertEquals("folk", doc.get(FreeDBIndexField.CATEGORY));
    assertEquals("c20c4b0d", doc.get(FreeDBIndexField.DISCID));
    assertEquals("13", doc.get(FreeDBIndexField.TRACKS));
    assertEquals("2008", doc.get(FreeDBIndexField.YEAR));
  }

  @Test
  public void testSearchFreeDBByDiscId() throws Exception {
    Results res = ss.search("discid:\"c20c4b0d\"", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("Ska-P", doc.get(FreeDBIndexField.ARTIST));
    assertEquals("L\u00e1grimas & Gozos", doc.get(FreeDBIndexField.TITLE));
    assertEquals("folk", doc.get(FreeDBIndexField.CATEGORY));
    assertEquals("c20c4b0d", doc.get(FreeDBIndexField.DISCID));
    assertEquals("13", doc.get(FreeDBIndexField.TRACKS));
    assertEquals("2008", doc.get(FreeDBIndexField.YEAR));
  }

  @Test
  public void testSearchFreeDBByYear() throws Exception {
    Results res = ss.search("year:\"2008\"", 0, 10);
    assertEquals(1, res.getTotalHits());
    Result result = res.results.get(0);
    MbDocument doc = result.getDoc();
    assertEquals("Ska-P", doc.get(FreeDBIndexField.ARTIST));
    assertEquals("L\u00e1grimas & Gozos", doc.get(FreeDBIndexField.TITLE));
    assertEquals("folk", doc.get(FreeDBIndexField.CATEGORY));
    assertEquals("c20c4b0d", doc.get(FreeDBIndexField.DISCID));
    assertEquals("13", doc.get(FreeDBIndexField.TRACKS));
    assertEquals("2008", doc.get(FreeDBIndexField.YEAR));
  }

  @Test
  public void testSearchFreeDBByDefaultField() throws Exception {

    //by artist
    {
      Results res = ss.search("\"Ska-P\"", 0, 10);
      assertEquals(1, res.getTotalHits());
      Result result = res.results.get(0);
      MbDocument doc = result.getDoc();
      assertEquals("Ska-P", doc.get(FreeDBIndexField.ARTIST));
      assertEquals("L\u00e1grimas & Gozos", doc.get(FreeDBIndexField.TITLE));
      assertEquals("folk", doc.get(FreeDBIndexField.CATEGORY));
      assertEquals("c20c4b0d", doc.get(FreeDBIndexField.DISCID));
      assertEquals("13", doc.get(FreeDBIndexField.TRACKS));
      assertEquals("2008", doc.get(FreeDBIndexField.YEAR));
    }

    //by title
    {
      Results res = ss.search("\"L\u00e1grimas & Gozos\"", 0, 10);
      assertEquals(1, res.getTotalHits());
      Result result = res.results.get(0);
      MbDocument doc = result.getDoc();
      assertEquals("Ska-P", doc.get(FreeDBIndexField.ARTIST));
      assertEquals("L\u00e1grimas & Gozos", doc.get(FreeDBIndexField.TITLE));
      assertEquals("folk", doc.get(FreeDBIndexField.CATEGORY));
      assertEquals("c20c4b0d", doc.get(FreeDBIndexField.DISCID));
      assertEquals("13", doc.get(FreeDBIndexField.TRACKS));
      assertEquals("2008", doc.get(FreeDBIndexField.YEAR));
    }

  }

  /**
   * @throws Exception exception
   */
  @Test
  public void testOutputXml() throws Exception {

    Results res = ss.search("discid:\"c20c4b0d\"", 0, 10);
    org.musicbrainz.search.servlet.mmd2.ResultsWriter writer = new FreeDBWriter();
    StringWriter sw = new StringWriter();
    PrintWriter pr = new PrintWriter(sw);
    writer.write(pr, res);
    pr.close();

    String output = sw.toString();
    System.out.println("Xml is" + output);
    assertTrue(output.contains("xmlns:ext=\"http://musicbrainz.org/ns/ext#-2.0\""));
    assertTrue(output.contains("count=\"1\""));
    assertTrue(output.contains("offset=\"0\""));
    assertTrue(output.contains("score=\"100\""));
    assertTrue(output.contains("id=\"c20c4b0d\""));
    assertTrue(output.contains("<title>L\u00e1grimas &amp; Gozos</title>"));
    assertTrue(output.contains("<artist>Ska-P</artist>"));
    assertTrue(output.contains("<year>2008</year>"));
    assertTrue(output.contains("<category>folk</category>"));
    assertTrue(output.contains("<track-list count=\"13\"/>"));
  }

  @Test
  public void testOutputJson() throws Exception {

    Results res = ss.search("discid:\"c20c4b0d\"", 0, 10);
    ResultsWriter writer = new FreeDBWriter();
    StringWriter sw = new StringWriter();
    PrintWriter pr = new PrintWriter(sw);
    writer.write(pr, res, SearchServerServlet.RESPONSE_JSON);
    pr.close();

    String output = sw.toString();
    System.out.println("Json is" + output);

    assertTrue(output.contains("\"count\":1"));
    assertTrue(output.contains("\"offset\":0,"));
    assertTrue(output.contains("\"score\":\"100\","));
    assertTrue(output.contains("\"id\":\"c20c4b0d\""));
    assertTrue(output.contains("\"title\":\"L\u00e1grimas & Gozos\""));
    assertTrue(output.contains("\"artist\":\"Ska-P\""));
    assertTrue(output.contains("\"year\":\"2008\""));
    assertTrue(output.contains("\"category\":\"folk\""));
    assertTrue(output.contains("\"track-list\":{\"count\":13}"));

  }

  @Test
  public void testOutputJsonNew() throws Exception {

    Results res = ss.search("discid:\"c20c4b0d\"", 0, 10);
    ResultsWriter writer = new FreeDBWriter();
    StringWriter sw = new StringWriter();
    PrintWriter pr = new PrintWriter(sw);
    writer.write(pr, res, SearchServerServlet.RESPONSE_JSON_NEW);
    pr.close();

    String output = sw.toString();
    System.out.println("Json New is" + output);

    assertTrue(output.contains("\"score\":\"100\","));
    assertTrue(output.contains("\"id\":\"c20c4b0d\""));
    assertTrue(output.contains("\"title\":\"L\u00e1grimas & Gozos\""));
    assertTrue(output.contains("\"artist\":\"Ska-P\""));
    assertTrue(output.contains("\"year\":\"2008\""));
    assertTrue(output.contains("\"category\":\"folk\""));
    assertTrue(output.contains("\"count\":13,"));
    assertTrue(output.contains("\"count\":1"));
    assertTrue(output.contains("\"offset\":0,"));

  }

  @Test
  public void testOutputJsonNewPretty() throws Exception {

    Results res = ss.search("discid:\"c20c4b0d\"", 0, 10);
    ResultsWriter writer = new FreeDBWriter();
    StringWriter sw = new StringWriter();
    PrintWriter pr = new PrintWriter(sw);
    writer.write(pr, res, SearchServerServlet.RESPONSE_JSON_NEW, true);
    pr.close();

    String output = sw.toString();
    System.out.println("Json New Pretty is" + output);
    assertTrue(output.contains("\"offset\" : 0,"));

  }
}