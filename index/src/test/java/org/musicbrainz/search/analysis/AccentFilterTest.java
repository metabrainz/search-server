package org.musicbrainz.search.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Test;
import org.musicbrainz.search.LuceneVersion;

import static org.junit.Assert.assertEquals;

public class AccentFilterTest  {

    private Analyzer analyzer = new MusicbrainzAnalyzer();
    private RAMDirectory dir = new RAMDirectory();

    public AccentFilterTest() {
    }

	@Before
    public void setUp() throws Exception {
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        Document doc = new Document();
        doc.add(new Field("name", "test", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);

        doc = new Document();
        doc.add(new Field("name", "ŃåᴊıÃšņ", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);

        doc = new Document();
        doc.add(new Field("name", "tést", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);

        doc = new Document();
        doc.add(new Field("name", "ábcáef", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);

        doc = new Document();
        doc.add(new Field("name", "qwe 1", Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("name", "qwe 2", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);

        doc = new Document();
        doc.add(new Field("name", "qwee 2", Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("name", "aaa", Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("name", "aaa", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);


        //
        writer.close();
    }

    @Test
    public void testSearchUnaccented() throws Exception {
        IndexSearcher searcher = new IndexSearcher(IndexReader.open(dir));
        Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("test");
        TopDocs docs = searcher.search(q,10);
        assertEquals(2, docs.totalHits);
        ScoreDoc scoredocs[] = docs.scoreDocs;
        assertEquals("test", searcher.doc(scoredocs[0].doc).getField("name").stringValue());
        assertEquals("tést", searcher.doc(scoredocs[1].doc).getField("name").stringValue());
    }

    @Test
    public void testSearchAccented() throws Exception {
        IndexSearcher searcher = new IndexSearcher(IndexReader.open(dir));
        Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("tést");
        TopDocs docs = searcher.search(q,10);
        assertEquals(2, docs.totalHits);
        ScoreDoc scoredocs[] = docs.scoreDocs;
        assertEquals("test", searcher.doc(scoredocs[0].doc).getField("name").stringValue());
        assertEquals("tést", searcher.doc(scoredocs[1].doc).getField("name").stringValue());
    }

    @Test
    public void testSearchAccented2() throws Exception {
        IndexSearcher searcher = new IndexSearcher(IndexReader.open(dir));
        Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("abcaef");
        TopDocs docs = searcher.search(q,10);
        ScoreDoc scoredocs[] = docs.scoreDocs;
        assertEquals(1, docs.totalHits);
        assertEquals("ábcáef", searcher.doc(scoredocs[0].doc).getField("name").stringValue());
    }
                /*
    public void testSearchAccented3() throws Exception {

        IndexSearcher searcher = new IndexSearcher(IndexReader.open(dir));
        Query q = new QueryParser("name", analyzer).parse("NaJiAsn");
        TopDocs docs = searcher.search(q,10);
        ScoreDoc scoredocs[] = docs.scoreDocs;
        assertEquals(1, docs.totalHits);
        assertEquals("ŃåᴊıÃšņ", searcher.doc(scoredocs[0].doc).getField("name").stringValue());

        searcher = new IndexSearcher(IndexReader.open(dir));
        q = new QueryParser("name", analyzer).parse("ŃåᴊıÃšņ");
        docs = searcher.search(q,10);
        scoredocs = docs.scoreDocs;
        assertEquals(1, docs.totalHits);
        assertEquals("ŃåᴊıÃšņ", searcher.doc(scoredocs[0].doc).getField("name").stringValue());
    }             */


    /**
     * Only one doc matches (even though two terms within doc that match)
     * @throws Exception exception
     */
    @Test
    public void testSearchQe() throws Exception {
        IndexSearcher searcher = new IndexSearcher(IndexReader.open(dir));
        Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("qwe");
        TopDocs docs = searcher.search(q,10);
        ScoreDoc scoredocs[] = docs.scoreDocs;
        assertEquals(1, docs.totalHits);
        String[] values = searcher.doc(scoredocs[0].doc).getValues("name");
        assertEquals("qwe 1", values[0]);
        assertEquals("qwe 2", values[1]);
    }

    @Test
    public void testSearchQe2() throws Exception {
        IndexSearcher searcher = new IndexSearcher(IndexReader.open(dir));
        Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("qwee");
        TopDocs docs = searcher.search(q,10);
        ScoreDoc scoredocs[] = docs.scoreDocs;
        assertEquals(1, docs.totalHits);
        String[] values =searcher.doc(scoredocs[0].doc).getValues("name");
        assertEquals("qwee 2", values[0]);
        assertEquals("aaa", values[1]);
        assertEquals("aaa", values[2]);
    }

}
