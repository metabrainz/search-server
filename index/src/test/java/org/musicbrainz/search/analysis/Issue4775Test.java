package org.musicbrainz.search.analysis;

import junit.framework.TestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.search.LuceneVersion;


public class Issue4775Test extends TestCase {

    public void testApostropheHandling() throws Exception {

        Analyzer analyzer = new StandardUnaccentAnalyzer();
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

        IndexSearcher searcher = new IndexSearcher(dir,true);
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

    public void testDashHandling() throws Exception {

        Analyzer analyzer = new StandardUnaccentAnalyzer();
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

        IndexSearcher searcher = new IndexSearcher(dir,true);
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
    public void testAcronymHandling() throws Exception {
        Analyzer analyzer = new StandardUnaccentAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        Document doc = new Document();
        doc.add(new Field("name", "R.E.S.", Field.Store.YES, Field.Index.ANALYZED));

        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(dir,true);
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
