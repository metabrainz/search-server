package org.musicbrainz.search.servlet;

import junit.framework.TestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Test;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.MusicbrainzSimilarity;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.Index;
import org.musicbrainz.search.index.ReleaseIndexField;

public class IssueSearch174Test extends TestCase {

    private SearchServer ss;

    @Before
    public void setUp() throws Exception {
        RAMDirectory ramDir = new RAMDirectory();
        Analyzer analyzer = DatabaseIndex.getAnalyzer(ReleaseIndexField.class);
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        writerConfig.setSimilarity(new MusicbrainzSimilarity());
        IndexWriter writer = new IndexWriter(ramDir, writerConfig);

        //Release, Empty fields
        {
            MbDocument doc = new MbDocument();
            doc.addField(ReleaseIndexField.RELEASE_ID, "11111111-1cf0-4d1f-aca7-2a6f89e34b36");
            doc.addField(ReleaseIndexField.CATALOG_NO, Index.NO_VALUE);
            writer.addDocument(doc.getLuceneDocument());
        }


        writer.close();

        IndexReader ir = IndexReader.open(ramDir);
        TermEnum tr = ir.terms(new Term("catno",""));
        assertNotNull(tr);
        assertNotNull(tr.term());
        assertEquals("catno", tr.term().field());
        assertEquals(1, tr.docFreq());
        assertEquals("-", tr.term().text());

        ss = new ReleaseSearch(new IndexSearcher(IndexReader.open(ramDir)));


    }

    @Test
    public void testFindReleaseByUnknownValue() throws Exception {

        IndexSearcher searcher = ss.getIndexSearcher();
        Query q = ss.parseQuery("catno:\\-");
        System.out.println(q);
        TopDocs topdocs = searcher.search(q, 10);
        assertEquals(1, topdocs.scoreDocs.length);
        for(ScoreDoc match:topdocs.scoreDocs)
        {
            Explanation explain = searcher.explain(q, match.doc);
            System.out.println(explain);
        }
    }

}
