package org.musicbrainz.search.servlet;

import java.util.Date;

import junit.framework.TestCase;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
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
import org.musicbrainz.search.index.Index;
import org.musicbrainz.search.index.MetaIndexField;
import org.musicbrainz.search.index.ReleaseIndexField;

public class IssueSearch174Test extends TestCase {

  private SearchServer ss;

  @Override
  @Before
  public void setUp() throws Exception {
    RAMDirectory ramDir = new RAMDirectory();
    Analyzer analyzer = DatabaseIndex.getAnalyzer(ReleaseIndexField.class);
    IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION, analyzer);
    writerConfig.setSimilarity(new MusicbrainzSimilarity());
    IndexWriter writer = new IndexWriter(ramDir, writerConfig);

    // Release, Empty fields
    {
      MbDocument doc = new MbDocument();
      doc.addField(ReleaseIndexField.RELEASE_ID, "11111111-1cf0-4d1f-aca7-2a6f89e34b36");
      doc.addField(ReleaseIndexField.CATALOG_NO, Index.NO_VALUE);
      writer.addDocument(doc.getLuceneDocument());
    }

    {
      MbDocument doc = new MbDocument();
      doc.addField(MetaIndexField.META, MetaIndexField.META_VALUE);
      doc.addField(MetaIndexField.LAST_UPDATED, NumericUtils.longToPrefixCoded(new Date().getTime()));
      writer.addDocument(doc.getLuceneDocument());
    }

    writer.close();

    IndexReader ir = IndexReader.open(ramDir);
    TermEnum tr = ir.terms(new Term("catno", ""));
    assertNotNull(tr);
    assertNotNull(tr.term());
    assertEquals("catno", tr.term().field());
    assertEquals(1, tr.docFreq());
    assertEquals("-", tr.term().text());

    SearcherManager searcherManager = new SearcherManager(ramDir, new MusicBrainzSearcherFactory(ResourceType.RELEASE));
    ss = new ReleaseSearch(searcherManager);

  }

  @Test
  public void testFindReleaseByUnknownValue() throws Exception {

    SearcherManager searcherManager = ss.getSearcherManager();
    IndexSearcher searcher = searcherManager.acquire();
    try {
      Query q = ss.parseQuery("catno:\\-");
      System.out.println(q);
      TopDocs topdocs = searcher.search(q, 10);
      assertEquals(1, topdocs.scoreDocs.length);
      for (ScoreDoc match : topdocs.scoreDocs) {
        Explanation explain = searcher.explain(q, match.doc);
        System.out.println(explain);
      }
    } finally {
      searcherManager.release(searcher);
    }
  }

}
