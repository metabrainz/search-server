package org.musicbrainz.search.analysis;

import org.apache.lucene.analysis.Analyzer;
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
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;
import org.musicbrainz.search.LuceneVersion;

import static org.junit.Assert.assertEquals;

public class Issue1446Test {

    private Analyzer analyzer = new MusicbrainzAnalyzer();
    private RAMDirectory dir = new RAMDirectory();

    public Issue1446Test() {
    }

    @Test
    public void testUppercaseKanatakaMatchesLowercaseKanataka() throws Exception {

        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        Document doc = new Document();
        doc.add(new Field("name", "ァ", TextField.TYPE_STORED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
        Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("ア");
        TopDocs docs = searcher.search(q,10);
        ScoreDoc scoredocs[] = docs.scoreDocs;
        assertEquals(1, docs.totalHits);
        assertEquals("ァ", searcher.doc(scoredocs[0].doc).getField("name").stringValue());
    }
	

    @Test
    public void testLowercaseKanatakaMatchesUppercaseKanataka() throws Exception {

        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);

        Document doc = new Document();
        doc.add(new Field("name", "ヨ", TextField.TYPE_STORED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
        Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("ョ");
        TopDocs docs = searcher.search(q,10);
        ScoreDoc scoredocs[] = docs.scoreDocs;
        assertEquals(1, docs.totalHits);
        assertEquals("ヨ", searcher.doc(scoredocs[0].doc).getField("name").stringValue());
    }

    @Test
    public void testUppercaseHiruganaMatchesLowercaseHirugana() throws Exception {

        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);

        Document doc = new Document();
        doc.add(new Field("name", "ぇ", TextField.TYPE_STORED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
        Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("え");
        TopDocs docs = searcher.search(q,10);
        ScoreDoc scoredocs[] = docs.scoreDocs;
        assertEquals(1, docs.totalHits);
        assertEquals("ぇ", searcher.doc(scoredocs[0].doc).getField("name").stringValue());
    }

    @Test
    public void testLowercaseHiruganaMatchesUppercaseHirugana() throws Exception {

        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);

        Document doc = new Document();
        doc.add(new Field("name", "つ", TextField.TYPE_STORED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
        Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("っ");
        TopDocs docs = searcher.search(q,10);
        ScoreDoc scoredocs[] = docs.scoreDocs;
        assertEquals(1, docs.totalHits);
        assertEquals("つ", searcher.doc(scoredocs[0].doc).getField("name").stringValue());
    }

}

