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

    public void testUppercaseKanatakaMatchesLowercaseKanataka() throws Exception {

        IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);

        Document doc = new Document();
        doc.add(new Field("name", "ァ", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(dir,true);
        Query q = new QueryParser("name", analyzer).parse("ア");
        TopDocs docs = searcher.search(q,10);
        ScoreDoc scoredocs[] = docs.scoreDocs;
        assertEquals(1, docs.totalHits);
        assertEquals("ァ", searcher.doc(scoredocs[0].doc).getField("name").stringValue());
    }
	

    public void testLowercaseKanatakaMatchesUppercaseKanataka() throws Exception {

        IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);

        Document doc = new Document();
        doc.add(new Field("name", "ヨ", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(dir,true);
        Query q = new QueryParser("name", analyzer).parse("ョ");
        TopDocs docs = searcher.search(q,10);
        ScoreDoc scoredocs[] = docs.scoreDocs;
        assertEquals(1, docs.totalHits);
        assertEquals("ヨ", searcher.doc(scoredocs[0].doc).getField("name").stringValue());
    }

    public void testUppercaseHiruganaMatchesLowercaseHirugana() throws Exception {

        IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);

        Document doc = new Document();
        doc.add(new Field("name", "ぇ", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(dir,true);
        Query q = new QueryParser("name", analyzer).parse("え");
        TopDocs docs = searcher.search(q,10);
        ScoreDoc scoredocs[] = docs.scoreDocs;
        assertEquals(1, docs.totalHits);
        assertEquals("ぇ", searcher.doc(scoredocs[0].doc).getField("name").stringValue());
    }


    public void testLowercaseHiruganaMatchesUppercaseHirugana() throws Exception {

        IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);

        Document doc = new Document();
        doc.add(new Field("name", "つ", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(dir,true);
        Query q = new QueryParser("name", analyzer).parse("っ");
        TopDocs docs = searcher.search(q,10);
        ScoreDoc scoredocs[] = docs.scoreDocs;
        assertEquals(1, docs.totalHits);
        assertEquals("つ", searcher.doc(scoredocs[0].doc).getField("name").stringValue());
    }

}

