package org.musicbrainz.search.analysis;

import junit.framework.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.store.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.document.*;
import org.apache.lucene.queryParser.*;
import org.apache.lucene.util.Version;
import org.musicbrainz.search.LuceneVersion;

public class AccentFilterTest extends TestCase {

    private Analyzer analyzer = new StandardUnaccentAnalyzer();
    private RAMDirectory dir = new RAMDirectory();

    public AccentFilterTest(String testName) {
        super(testName);
    }

	@Override
    protected void setUp() throws Exception {
        IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
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

    public void testSearchUnaccented() throws Exception {
        IndexSearcher searcher = new IndexSearcher(dir,true);
        Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("test");
        TopDocs docs = searcher.search(q,10);
        assertEquals(2, docs.totalHits);
        ScoreDoc scoredocs[] = docs.scoreDocs;
        assertEquals("test", searcher.doc(scoredocs[0].doc).getField("name").stringValue());
        assertEquals("tést", searcher.doc(scoredocs[1].doc).getField("name").stringValue());
    }

    public void testSearchAccented() throws Exception {
        IndexSearcher searcher = new IndexSearcher(dir,true);
        Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("tést");
        TopDocs docs = searcher.search(q,10);
        assertEquals(2, docs.totalHits);
        ScoreDoc scoredocs[] = docs.scoreDocs;
        assertEquals("test", searcher.doc(scoredocs[0].doc).getField("name").stringValue());
        assertEquals("tést", searcher.doc(scoredocs[1].doc).getField("name").stringValue());
    }

    public void testSearchAccented2() throws Exception {
        IndexSearcher searcher = new IndexSearcher(dir,true);
        Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("abcaef");
        TopDocs docs = searcher.search(q,10);
        ScoreDoc scoredocs[] = docs.scoreDocs;
        assertEquals(1, docs.totalHits);
        assertEquals("ábcáef", searcher.doc(scoredocs[0].doc).getField("name").stringValue());
    }
                /*
    public void testSearchAccented3() throws Exception {

        IndexSearcher searcher = new IndexSearcher(dir,true);
        Query q = new QueryParser("name", analyzer).parse("NaJiAsn");
        TopDocs docs = searcher.search(q,10);
        ScoreDoc scoredocs[] = docs.scoreDocs;
        assertEquals(1, docs.totalHits);
        assertEquals("ŃåᴊıÃšņ", searcher.doc(scoredocs[0].doc).getField("name").stringValue());

        searcher = new IndexSearcher(dir,true);
        q = new QueryParser("name", analyzer).parse("ŃåᴊıÃšņ");
        docs = searcher.search(q,10);
        scoredocs = docs.scoreDocs;
        assertEquals(1, docs.totalHits);
        assertEquals("ŃåᴊıÃšņ", searcher.doc(scoredocs[0].doc).getField("name").stringValue());
    }             */


    /**
     * Only one doc matches (even though two terms within doc that match)
     * @throws Exception
     */
    public void testSearchQe() throws Exception {
        IndexSearcher searcher = new IndexSearcher(dir,true);
        Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("qwe");
        TopDocs docs = searcher.search(q,10);
        ScoreDoc scoredocs[] = docs.scoreDocs;
        assertEquals(1, docs.totalHits);
        String[] values = searcher.doc(scoredocs[0].doc).getValues("name");
        assertEquals("qwe 1", values[0]);
        assertEquals("qwe 2", values[1]);
    }

    public void testSearchQe2() throws Exception {
        IndexSearcher searcher = new IndexSearcher(dir,true);
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
