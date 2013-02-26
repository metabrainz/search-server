package org.musicbrainz.search.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
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

import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class StopWordsTest
{
    public StopWordsTest() {
    }


    @Test
     public void testUnableToFindStopWordsWithStandardAnalyser() throws Exception {

        Analyzer analyzer = new StandardAnalyzer(LuceneVersion.LUCENE_VERSION);
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        Document doc = new Document();
        doc.add(new Field("name", "that", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
        Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("that");
        TopDocs docs = searcher.search(q,null,100);
        assertEquals(0, docs.totalHits);

    }

    @Test
    public void testAbleToFindStopWordsWithStandardAnalyserIfNoStopWords() throws Exception {

        Analyzer analyzer = new StandardAnalyzer(LuceneVersion.LUCENE_VERSION, CharArraySet.EMPTY_SET);
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        Document doc = new Document();
        doc.add(new Field("name", "that", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
        Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("that");
        TopDocs docs = searcher.search(q,null,100);
        assertEquals(1, docs.totalHits);
    }

    @Test
     public void testAbleToFindStopWords() throws Exception {

        Analyzer analyzer = new MusicbrainzAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        Document doc = new Document();
        doc.add(new Field("name", "that", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
        Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("that");
        TopDocs docs = searcher.search(q,null,100);
        assertEquals(1, docs.totalHits);

    }
}
