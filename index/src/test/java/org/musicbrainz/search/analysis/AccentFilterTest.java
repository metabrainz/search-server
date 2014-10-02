package org.musicbrainz.search.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Test;
import org.musicbrainz.search.LuceneVersion;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
        doc.add(new Field("name", "test", TextField.TYPE_STORED));
        writer.addDocument(doc);

        doc = new Document();
        doc.add(new Field("name", "ŃåᴊıÃšņ", TextField.TYPE_STORED));
        writer.addDocument(doc);

        doc = new Document();
        doc.add(new Field("name", "tést", TextField.TYPE_STORED));
        writer.addDocument(doc);

        doc = new Document();
        doc.add(new Field("name", "ábcáef", TextField.TYPE_STORED));
        writer.addDocument(doc);

        doc = new Document();
        doc.add(new Field("name", "qwe 1", TextField.TYPE_STORED));
        doc.add(new Field("name", "qwe 2", TextField.TYPE_STORED));
        writer.addDocument(doc);

        doc = new Document();
        doc.add(new Field("name", "qwee 2", TextField.TYPE_STORED));
        doc.add(new Field("name", "aaa", TextField.TYPE_STORED));
        doc.add(new Field("name", "aaa", TextField.TYPE_STORED));
        writer.addDocument(doc);


        //
        writer.close();
    }

    @Test
    public void testSearchUnaccented() throws Exception {
        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
        Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("test");
        TopDocs docs = searcher.search(q,10);
        assertEquals(2, docs.totalHits);
        ScoreDoc scoredocs[] = docs.scoreDocs;
        assertEquals("test", searcher.doc(scoredocs[0].doc).getField("name").stringValue());
        assertEquals("tést", searcher.doc(scoredocs[1].doc).getField("name").stringValue());
    }

    @Test
    public void testSearchAccented() throws Exception {
        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
        Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("tést");
        TopDocs docs = searcher.search(q,10);
        assertEquals(2, docs.totalHits);
        ScoreDoc scoredocs[] = docs.scoreDocs;
        assertEquals("test", searcher.doc(scoredocs[0].doc).getField("name").stringValue());
        assertEquals("tést", searcher.doc(scoredocs[1].doc).getField("name").stringValue());
    }

    @Test
    public void testSearchAccented2() throws Exception {
        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
        Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("abcaef");
        TopDocs docs = searcher.search(q,10);
        ScoreDoc scoredocs[] = docs.scoreDocs;
        assertEquals(1, docs.totalHits);
        assertEquals("ábcáef", searcher.doc(scoredocs[0].doc).getField("name").stringValue());
    }

    @Test
    public void testSearchAccented3() throws Exception {

        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
        Query q = new QueryParser(LuceneVersion.LUCENE_VERSION,"name", analyzer).parse("NaJiAsn");
        TopDocs docs = searcher.search(q,10);
        ScoreDoc scoredocs[] = docs.scoreDocs;
        assertEquals(1, docs.totalHits);
        assertEquals("ŃåᴊıÃšņ", searcher.doc(scoredocs[0].doc).getField("name").stringValue());

        searcher = new IndexSearcher(DirectoryReader.open(dir));
        q = new QueryParser(LuceneVersion.LUCENE_VERSION,"name", analyzer).parse("ŃåᴊıÃšņ");
        docs = searcher.search(q,10);
        scoredocs = docs.scoreDocs;
        assertEquals(1, docs.totalHits);
        assertEquals("ŃåᴊıÃšņ", searcher.doc(scoredocs[0].doc).getField("name").stringValue());
    }

    /**
     * Only one doc matches (even though two terms within doc that match)
     * @throws Exception exception
     */
    @Test
    public void testSearchQe() throws Exception {
        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
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
        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
        Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("qwee");
        TopDocs docs = searcher.search(q,10);
        ScoreDoc scoredocs[] = docs.scoreDocs;
        assertEquals(1, docs.totalHits);
        String[] values =searcher.doc(scoredocs[0].doc).getValues("name");
        assertEquals("qwee 2", values[0]);
        assertEquals("aaa", values[1]);
        assertEquals("aaa", values[2]);
    }

    @Test
    public void testShowHowAccentsAndSpecialCharsConverted()throws Exception
    {
        //Tokenizer on its won has no effect
        Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("ŃåᴊıÃšņ"));
        tokenizer.reset();
        assertTrue(tokenizer.incrementToken());
        CharTermAttribute term = tokenizer.addAttribute(CharTermAttribute.class);
        TypeAttribute type = tokenizer.addAttribute(TypeAttribute.class);
        assertEquals("<ALPHANUM>", type.type());
        assertEquals("ŃåᴊıÃšņ", new String(term.buffer(), 0, term.length()));

        //Analyse field
        Analyzer analyzer = new MusicbrainzAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        Document doc = new Document();
        doc.add(new Field("name", "ŃåᴊıÃšņ", TextField.TYPE_STORED));
        writer.addDocument(doc);
        writer.close();

        //Show how it has been converted
        IndexReader ir = DirectoryReader.open(dir);
        Fields fields = MultiFields.getFields(ir);
        Terms terms = fields.terms("name");
        TermsEnum termsEnum = terms.iterator(null);
        termsEnum.next();
        assertEquals(1, termsEnum.docFreq());
        assertEquals("najiasn", termsEnum.term().utf8ToString());
        assertNull(termsEnum.next());
    }
}
