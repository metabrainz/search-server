package org.musicbrainz.search.analysis;

import junit.framework.TestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.search.LuceneVersion;

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

        Analyzer analyzer = new StandardAnalyzer(LuceneVersion.LUCENE_VERSION);
        RAMDirectory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        Document doc = new Document();
        doc.add(new Field("name", "that", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(dir,true);
        Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("that");
        TopDocs docs = searcher.search(q,null,100);
        assertEquals(0, docs.totalHits);

    }

    public void testAbleToFindStopWordsWithStandardAnalyserIfNoStopWords() throws Exception {

        Analyzer analyzer = new StandardAnalyzer(LuceneVersion.LUCENE_VERSION, new HashSet());
        RAMDirectory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        Document doc = new Document();
        doc.add(new Field("name", "that", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(dir,true);
        Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("that");
        TopDocs docs = searcher.search(q,null,100);
        assertEquals(1, docs.totalHits);
    }

     public void testAbleToFindStopWords() throws Exception {

        Analyzer analyzer = new StandardUnaccentAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        Document doc = new Document();
        doc.add(new Field("name", "that", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(dir,true);
        Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("that");
        TopDocs docs = searcher.search(q,null,100);
        assertEquals(1, docs.totalHits);

    }
}
