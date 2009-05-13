package org.musicbrainz.search.analysis;

import junit.framework.TestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Hits;
import org.apache.lucene.queryParser.QueryParser;

import java.util.HashSet;

public class StopWordsTest extends TestCase
{
    public StopWordsTest(String testName) {
        super(testName);
    }

	@Override
    protected void setUp() throws Exception {

    }

     public void testUnableToFindStopWordsWithStandardAnalyser() throws Exception {

        Analyzer analyzer = new StandardAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        Document doc = new Document();
        doc.add(new Field("name", "that", Field.Store.YES, Field.Index.TOKENIZED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(dir);
        Query q = new QueryParser("name", analyzer).parse("that");
        Hits hits = searcher.search(q);
        assertEquals(0, hits.length());

    }

    public void testAbleToFindStopWordsWithStandardAnalyserIfNoStopWords() throws Exception {

        Analyzer analyzer = new StandardAnalyzer(new HashSet());
        RAMDirectory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        Document doc = new Document();
        doc.add(new Field("name", "that", Field.Store.YES, Field.Index.TOKENIZED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(dir);
        Query q = new QueryParser("name", analyzer).parse("that");
        Hits hits = searcher.search(q);
        assertEquals(1, hits.length());
    }

     public void testAbleToFindStopWords() throws Exception {

        Analyzer analyzer = new StandardUnaccentAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        Document doc = new Document();
        doc.add(new Field("name", "that", Field.Store.YES, Field.Index.TOKENIZED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(dir);
        Query q = new QueryParser("name", analyzer).parse("that");
        Hits hits = searcher.search(q);
        assertEquals(1, hits.length());

    }
}
