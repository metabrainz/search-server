package org.musicbrainz.search.servlet;

import java.util.Date;

import junit.framework.TestCase;

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
import org.musicbrainz.search.index.Index;
import org.musicbrainz.search.index.MetaIndexField;
import org.musicbrainz.search.index.ReleaseIndexField;

import static org.junit.Assert.assertEquals;

public class IssueSearch174Test extends TestCase {

  private AbstractSearchServer ss;

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
      doc.addField(ReleaseIndexField.STATUS, Index.NO_VALUE);
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
    Terms terms = fields.terms("status");
    TermsEnum termsEnum = terms.iterator(null);
    termsEnum.next();
    assertEquals(1, termsEnum.docFreq());
    assertEquals("-", termsEnum.term().utf8ToString());

    SearcherManager searcherManager = new SearcherManager(ramDir, new MusicBrainzSearcherFactory(ResourceType.RELEASE));
    ss = new ReleaseSearch(searcherManager);

  }

  @Test
  public void testFindReleaseByUnknownValue() throws Exception {

    SearcherManager searcherManager = ss.getSearcherManager();
    IndexSearcher searcher = searcherManager.acquire();
    try {
      Query q = ss.parseQuery("status:\\-");
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
