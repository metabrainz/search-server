package org.musicbrainz.search.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;
import org.musicbrainz.search.LuceneVersion;

import java.io.StringReader;

import static org.junit.Assert.*;

public class Issue4775Test  {

    @Test
    public void testApostropheHandling() throws Exception {

        Analyzer analyzer = new MusicbrainzAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        Document doc = new Document();
        doc.add(new Field("name", "O'reilly", Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("name", "There's", Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("name", "its", Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("name", "John’s", Field.Store.YES, Field.Index.ANALYZED));

        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(IndexReader.open(dir));
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("Oreilly");
            assertEquals(1, searcher.search(q,10).totalHits);
        }

        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("Theres");
            assertEquals(1, searcher.search(q,10).totalHits);
        }

        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("there's");
            assertEquals(1, searcher.search(q,10).totalHits);
        }

        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("There");
            assertEquals(0, searcher.search(q,10).totalHits);
        }

        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("it's'");
            assertEquals(1, searcher.search(q,10).totalHits);
        }

        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("johns");
            assertEquals(1, searcher.search(q,10).totalHits);
        }
    }

    @Test
    public void testTokenizeApostrophe() throws Exception {

        Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("There's"));
        assertTrue(tokenizer.incrementToken());
        CharTermAttribute term = tokenizer.addAttribute(CharTermAttribute.class);
        TypeAttribute type = tokenizer.addAttribute(TypeAttribute.class);
        OffsetAttribute offset = tokenizer.addAttribute(OffsetAttribute.class);
        assertEquals("<APOSTROPHE>", type.type());
        assertEquals("There's", new String(term.buffer(),0,term.length()));
        assertEquals(0, offset.startOffset());
        assertEquals(7, offset.endOffset());
        assertFalse(tokenizer.incrementToken());
    }

    @Test
    public void testFilterApostrophe() throws Exception {

        Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("There's"));
        MusicbrainzTokenizerFilter filter = new MusicbrainzTokenizerFilter(tokenizer);
        assertTrue(filter.incrementToken());
        CharTermAttribute term = tokenizer.addAttribute(CharTermAttribute.class);
        TypeAttribute type = tokenizer.addAttribute(TypeAttribute.class);
        OffsetAttribute offset = tokenizer.addAttribute(OffsetAttribute.class);
        assertEquals("<APOSTROPHE>", type.type());
        assertEquals("Theres", new String(term.buffer(),0,term.length()));
        assertEquals(0, offset.startOffset());
        assertEquals(7, offset.endOffset());
        assertFalse(tokenizer.incrementToken());
    }

    @Test
    public void testTokenizeDoubleApostrophe() throws Exception {

        Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("it's'"));
        assertTrue(tokenizer.incrementToken());
        CharTermAttribute term = tokenizer.addAttribute(CharTermAttribute.class);
        TypeAttribute type = tokenizer.addAttribute(TypeAttribute.class);
        OffsetAttribute offset = tokenizer.addAttribute(OffsetAttribute.class);
        assertEquals("<ALPHANUMANDPUNCTUATION>", type.type());
        assertEquals("it's'", new String(term.buffer(),0,term.length()));
        assertEquals(0, offset.startOffset());
        assertEquals(5, offset.endOffset());
        assertFalse(tokenizer.incrementToken());
    }

    @Test
    public void testFilterDoubleApostrophe() throws Exception {

        Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("it's'"));
        MusicbrainzTokenizerFilter filter = new MusicbrainzTokenizerFilter(tokenizer);
        assertTrue(filter.incrementToken());
        CharTermAttribute term = tokenizer.addAttribute(CharTermAttribute.class);
        TypeAttribute type = tokenizer.addAttribute(TypeAttribute.class);
        OffsetAttribute offset = tokenizer.addAttribute(OffsetAttribute.class);
        assertEquals("<ALPHANUMANDPUNCTUATION>", type.type());
        assertEquals("its", new String(term.buffer(),0,term.length()));
        assertEquals(0, offset.startOffset());
        assertEquals(5, offset.endOffset());
        assertFalse(tokenizer.incrementToken());
    }

    @Test
    public void testDashHandling() throws Exception {

        Analyzer analyzer = new MusicbrainzAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        Document doc = new Document();
        doc.add(new Field("name", "1999-2000", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);

        doc = new Document();
        doc.add(new Field("name", "1999–2000", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(IndexReader.open(dir));
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("1999-2000");
            assertEquals(2, searcher.search(q,10).totalHits);
        }

        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("1999–2000");
            assertEquals(2, searcher.search(q,10).totalHits);
        }

    }

    /** Check same as standard filter
     *
     * @throws Exception
     */
    @Test
    public void testAcronymHandling() throws Exception {
        Analyzer analyzer = new MusicbrainzAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        Document doc = new Document();
        doc.add(new Field("name", "R.E.S.", Field.Store.YES, Field.Index.ANALYZED));

        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(IndexReader.open(dir));
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("res");
            assertEquals(1, searcher.search(q,10).totalHits);
        }

        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("R.E.S.");
            assertEquals(1, searcher.search(q,10).totalHits);
        }

        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("R.E.S");
            assertEquals(1, searcher.search(q,10).totalHits);
        }

    }

}
