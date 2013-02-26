package org.musicbrainz.search.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;
import org.musicbrainz.search.LuceneVersion;

import static org.junit.Assert.assertEquals;

public class IssueSearch220Test {

    /*
    @Test
    public void testWhitespaceHandling() throws Exception {

        Analyzer analyzer = new TitleAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        Document doc = new Document();
        doc.add(new Field("name", "森山\u3000直太朗", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("\"森山\u0020直太朗\"");
            assertEquals(1, searcher.search(q,10).totalHits);

        }

        searcher = new IndexSearcher(DirectoryReader.open(dir));
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("\"森山\u3000直太朗\"");
            assertEquals(1, searcher.search(q,10).totalHits);

        }
    }

    @Test
    public void testWhitespaceHandling2() throws Exception {

        Analyzer analyzer = new TitleAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        Document doc = new Document();
        doc.add(new Field("name", "森山\u0020直太朗", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("\"森山\u0020直太朗\"");
            assertEquals(1, searcher.search(q,10).totalHits);

        }

        searcher = new IndexSearcher(DirectoryReader.open(dir));
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("\"森山\u3000直太朗\"");
            assertEquals(1, searcher.search(q,10).totalHits);

        }
    }
      */
    @Test
    public void testWhitespaceHandling3() throws Exception {

        Analyzer analyzer = new TitleAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        Document doc = new Document();
        doc.add(new Field("name", "fred\u3000james", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("\"fred\u0020james\"");
            assertEquals(1, searcher.search(q,10).totalHits);

        }

        searcher = new IndexSearcher(DirectoryReader.open(dir));
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("\"fred\u3000james\"");
            assertEquals(1, searcher.search(q,10).totalHits);

        }
    }

}