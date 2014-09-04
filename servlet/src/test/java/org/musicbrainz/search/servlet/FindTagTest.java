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
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.MetaIndexField;
import org.musicbrainz.search.index.TagIndexField;

public class FindTagTest {

  private AbstractSearchServer ss;

  @Before
  public void setUp() throws Exception {
    RAMDirectory ramDir = new RAMDirectory();

    Analyzer analyzer = DatabaseIndex.getAnalyzer(TagIndexField.class);
    IndexWriterConfig  writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
    IndexWriter writer = new IndexWriter(ramDir, writerConfig);

    {
      MbDocument doc = new MbDocument();
      doc.addField(TagIndexField.TAG, "rock");
      writer.addDocument(doc.getLuceneDocument());

      doc = new MbDocument();
      doc.addField(TagIndexField.TAG, "classic rock");
      writer.addDocument(doc.getLuceneDocument());
    }

    {
      MbDocument doc = new MbDocument();
      doc.addField(MetaIndexField.META, MetaIndexField.META_VALUE);
      doc.addNumericField(MetaIndexField.LAST_UPDATED, new Date().getTime());
      writer.addDocument(doc.getLuceneDocument());
    }

    writer.close();
    SearcherManager searcherManager = new SearcherManager(ramDir, new MusicBrainzSearcherFactory(ResourceType.TAG));
    ss = new TagSearch(searcherManager);
  }


  @Test
  public void testFindTagByName() throws Exception {
    Results res = ss.search("tag:rock", 0, 10);
    assertEquals(2, res.getTotalHits());
    Result result = res.results.get(0);

    MbDocument doc = result.getDoc();
    assertEquals("rock", doc.get(TagIndexField.TAG));

    result = res.results.get(1);
    doc = result.getDoc();
    assertEquals("classic rock", doc.get(TagIndexField.TAG));
  }

  @Test
  public void testOutputAsXml() throws Exception {

    Results res = ss.search("tag:rock", 0, 10);
    ResultsWriter writer = ss.getMmd2Writer();
    StringWriter sw = new StringWriter();
    PrintWriter pr = new PrintWriter(sw);
    writer.write(pr, res);
    pr.close();
    String output = sw.toString();
    System.out.println("Xml is" + output);
    assertTrue(output.contains("count=\"2\""));
    assertTrue(output.contains("offset=\"0\""));
    assertTrue(output.contains("<name>rock</name>"));
    assertTrue(output.contains("<name>classic rock</name>"));
  }

  @Test
  public void testOutputAsJson() throws Exception {

    Results res = ss.search("tag:rock", 0, 10);
    ResultsWriter writer = ss.getMmd2Writer();
    StringWriter sw = new StringWriter();
    PrintWriter pr = new PrintWriter(sw);
    writer.write(pr, res, SearchServerServlet.RESPONSE_JSON);
    pr.close();

    String output = sw.toString();
    System.out.println("Json is" + output);

    assertTrue(output.contains("\"name\":\"rock\""));
    assertTrue(output.contains("\"name\":\"classic rock\""));
    assertTrue(output.contains("\"count\":2"));
    assertTrue(output.contains("\"offset\":0"));


  }

  @Test
  public void testOutputAsJsonNew() throws Exception {

    Results res = ss.search("tag:rock", 0, 10);
    ResultsWriter writer = ss.getMmd2Writer();
    StringWriter sw = new StringWriter();
    PrintWriter pr = new PrintWriter(sw);
    writer.write(pr, res, SearchServerServlet.RESPONSE_JSON_NEW);
    pr.close();

    String output = sw.toString();
    System.out.println("Json New is" + output);
    assertTrue(output.contains("tags"));
    assertTrue(output.contains("\"name\":\"rock\""));
    assertTrue(output.contains("\"name\":\"classic rock\""));
    assertTrue(output.contains("\"count\":2"));
    assertTrue(output.contains("\"offset\":0"));


  }

  @Test
  public void testOutputAsJsonNewPretty() throws Exception {

    Results res = ss.search("tag:rock", 0, 10);
    ResultsWriter writer = ss.getMmd2Writer();
    StringWriter sw = new StringWriter();
    PrintWriter pr = new PrintWriter(sw);
    writer.write(pr, res, SearchServerServlet.RESPONSE_JSON_NEW, true);
    pr.close();

    String output = sw.toString();
    System.out.println("Json New Pretty is" + output);
    assertTrue(output.contains("\"offset\" : 0"));


  }
}