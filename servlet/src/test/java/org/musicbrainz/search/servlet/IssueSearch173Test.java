package org.musicbrainz.search.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.NumericUtils;
import org.junit.Before;
import org.junit.Test;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.MusicbrainzSimilarity;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.MetaIndexField;
import org.musicbrainz.search.index.ReleaseIndexField;

public class IssueSearch173Test  {

  private AbstractSearchServer ss;
  private AbstractDismaxSearchServer sd;

  @Before
  public void setUp() throws Exception {
    RAMDirectory ramDir = new RAMDirectory();
    Analyzer analyzer = DatabaseIndex.getAnalyzer(ReleaseIndexField.class);
    IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
    writerConfig.setSimilarity(new MusicbrainzSimilarity());
    IndexWriter writer = new IndexWriter(ramDir, writerConfig);

    //Release
    {
      MbDocument doc = new MbDocument();
      doc.addField(ReleaseIndexField.RELEASE_ID, "11111111-1cf0-4d1f-aca7-2a6f89e34b36");
      doc.addField(ReleaseIndexField.CATALOG_NO, "AD 17T");
      writer.addDocument(doc.getLuceneDocument());
    }

    {
      MbDocument doc = new MbDocument();
      doc.addField(MetaIndexField.META, MetaIndexField.META_VALUE);
        doc.addNumericField(MetaIndexField.LAST_UPDATED, new Date().getTime());
      writer.addDocument(doc.getLuceneDocument());
    }


    writer.close();

    IndexReader ir = DirectoryReader.open(ramDir);
    Fields fields = MultiFields.getFields(ir);
    Terms terms = fields.terms("catno");
    TermsEnum termsEnum = terms.iterator(null);
    termsEnum.next();
    assertEquals(1, termsEnum.docFreq());
    assertEquals("ad17t", termsEnum.term().utf8ToString());

    SearcherManager searcherManager = new SearcherManager(ramDir, new MusicBrainzSearcherFactory(ResourceType.RELEASE));
    ss = new ReleaseSearch(searcherManager);
    sd = new ReleaseDismaxSearch(ss);
  }

  @Test
  public void testFindReleaseByCatnoDismax() throws Exception {

    SearcherManager searcherManager = ss.getSearcherManager();
    IndexSearcher searcher = searcherManager.acquire();
    try {
      Query q = sd.parseQuery("ad17t");
      System.out.println(q);
      TopDocs topdocs = searcher.search(q, 10);
      assertEquals(1, topdocs.scoreDocs.length);
      for(ScoreDoc match:topdocs.scoreDocs)
      {
        Explanation explain = searcher.explain(q, match.doc);
        System.out.println(explain);
      }
    } finally {
      searcherManager.release(searcher);
    }
  }

  @Test
  public void testFindReleaseByCatnoWithSpacesDismax() throws Exception {

    SearcherManager searcherManager = ss.getSearcherManager();
    IndexSearcher searcher = searcherManager.acquire();
    try {
      Query q = sd.parseQuery("ad 17t");
      System.out.println(q);
      TopDocs topdocs = searcher.search(q, 10);
      assertEquals(1, topdocs.scoreDocs.length);
      for(ScoreDoc match:topdocs.scoreDocs)
      {
        Explanation explain = searcher.explain(q, match.doc);
        System.out.println(explain);
      }
    } finally {
      searcherManager.release(searcher);
    }
  }

  @Test
  public void testFindReleaseByCatno() throws Exception {

    SearcherManager searcherManager = ss.getSearcherManager();
    IndexSearcher searcher = searcherManager.acquire();
    try {
      Query q = ss.parseQuery("catno:ad17t");
      System.out.println(q);
      TopDocs topdocs = searcher.search(q, 10);
      assertEquals(1, topdocs.scoreDocs.length);
      for(ScoreDoc match:topdocs.scoreDocs)
      {
        Explanation explain = searcher.explain(q, match.doc);
        System.out.println(explain);
      }
    } finally {
      searcherManager.release(searcher);
    }
  }

  @Test
  public void testFindReleaseByCatnoWithSpaces() throws Exception {

    SearcherManager searcherManager = ss.getSearcherManager();
    IndexSearcher searcher = searcherManager.acquire();
    try {
      Query q = ss.parseQuery("catno:\"ad 17t\"");
      System.out.println(q);
      TopDocs topdocs = searcher.search(q, 10);
      assertEquals(1, topdocs.scoreDocs.length);
      for(ScoreDoc match:topdocs.scoreDocs)
      {
        Explanation explain = searcher.explain(q, match.doc);
        System.out.println(explain);
      }
    } finally {
      searcherManager.release(searcher);
    }
  }
}
