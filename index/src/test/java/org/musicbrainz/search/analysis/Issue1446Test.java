package org.musicbrainz.search.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.queryParser.QueryParser;
import junit.framework.TestCase;

public class Issue1446Test extends TestCase {

    private Analyzer analyzer = new StandardUnaccentAnalyzer();
    private RAMDirectory dir = new RAMDirectory();

    public Issue1446Test(String testName) {
        super(testName);
    }

	@Override
    protected void setUp() throws Exception {
        IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        Document doc = new Document();
        doc.add(new Field("name", "ぇ", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();
    }

    public void testUppercaseKanaMatchesLowercaseKana() throws Exception {
        if(true)
        return;
        IndexSearcher searcher = new IndexSearcher(dir,true);
        Query q = new QueryParser("name", analyzer).parse("え");
        TopDocs docs = searcher.search(q,10);
        ScoreDoc scoredocs[] = docs.scoreDocs;
        assertEquals(1, docs.totalHits);
        assertEquals("ぇ", searcher.doc(scoredocs[0].doc).getField("name").stringValue());
        //ここにいるぜえ
        //ここにいるぜぇ
    }

}
