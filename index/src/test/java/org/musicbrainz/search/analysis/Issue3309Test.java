package org.musicbrainz.search.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;
import org.musicbrainz.search.LuceneVersion;

import java.io.StringReader;

import static org.junit.Assert.*;

public class Issue3309Test {

    @Test
    public void testTokenizeAcronyms() throws Exception {

        Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("R.E.S"));
        assertTrue(tokenizer.incrementToken());
        CharTermAttribute term = tokenizer.addAttribute(CharTermAttribute.class);
        TypeAttribute type = tokenizer.addAttribute(TypeAttribute.class);
        OffsetAttribute offset = tokenizer.addAttribute(OffsetAttribute.class);
        assertEquals("<ACRONYM>", type.type());
        assertEquals("R.E.S", new String(term.buffer(),0,term.length()));
        assertEquals(0, offset.startOffset());
        assertEquals(5, offset.endOffset());
        assertFalse(tokenizer.incrementToken());
    }

    @Test
    public void testFilterAcronyms() throws Exception {

        Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("R.E.S"));
        MusicbrainzTokenizerFilter filter = new MusicbrainzTokenizerFilter(tokenizer);
        assertTrue(filter.incrementToken());
        CharTermAttribute term = tokenizer.addAttribute(CharTermAttribute.class);
        TypeAttribute type = tokenizer.addAttribute(TypeAttribute.class);
        OffsetAttribute offset = tokenizer.addAttribute(OffsetAttribute.class);
        assertEquals("<ACRONYM>", type.type());
        assertEquals("RES", new String(term.buffer(),0,term.length()));
        assertEquals(0, offset.startOffset());
        assertEquals(5, offset.endOffset());
        assertFalse(tokenizer.incrementToken());
    }

    @Test
    public void testFilterAcronyms2() throws Exception {

            Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("R.E.S."));
            MusicbrainzTokenizerFilter filter = new MusicbrainzTokenizerFilter(tokenizer);
            assertTrue(filter.incrementToken());
            CharTermAttribute term = tokenizer.addAttribute(CharTermAttribute.class);
            TypeAttribute type = tokenizer.addAttribute(TypeAttribute.class);
            OffsetAttribute offset = tokenizer.addAttribute(OffsetAttribute.class);
            assertEquals("<ACRONYM>", type.type());
            assertEquals("RES", new String(term.buffer(),0,term.length()));
            assertEquals(0, offset.startOffset());
            assertEquals(6, offset.endOffset());
            assertFalse(tokenizer.incrementToken());
        }


    @Test
    public void testMatchAcronymnsWithoutTrailingDot() throws Exception {

        Analyzer analyzer = new MusicbrainzAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        Document doc = new Document();
        doc.add(new Field("name", "R.E.S.", TextField.TYPE_STORED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
        Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("R.E.S");
        TopDocs docs = searcher.search(q,10);
        assertEquals(1, docs.totalHits);
    }

    @Test
    public void testMatchAcronymnsWithTrailingDot() throws Exception {

        Analyzer analyzer = new MusicbrainzAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        Document doc = new Document();
        doc.add(new Field("name", "R.E.S", TextField.TYPE_STORED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
        Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("R.E.S.");
        TopDocs docs = searcher.search(q,10);
        assertEquals(1, docs.totalHits);
    }
}
