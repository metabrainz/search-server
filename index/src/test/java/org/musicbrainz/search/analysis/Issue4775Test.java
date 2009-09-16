package org.musicbrainz.search.analysis;

import junit.framework.TestCase;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Hits;
import org.apache.lucene.queryParser.QueryParser;


public class Issue4775Test extends TestCase {

    public void testApostropheHandling() throws Exception {

        Analyzer analyzer = new StandardUnaccentAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        Document doc = new Document();
        doc.add(new Field("name", "O'reilly", Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("name", "There's", Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("name", "its", Field.Store.YES, Field.Index.ANALYZED));
                
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(dir,true);
        {
            Query q = new QueryParser("name", analyzer).parse("Oreilly");
            assertEquals(1, searcher.search(q,10).totalHits);
        }

        {
            Query q = new QueryParser("name", analyzer).parse("Theres");
            assertEquals(1, searcher.search(q,10).totalHits);
        }

        {
            Query q = new QueryParser("name", analyzer).parse("there's");
            assertEquals(1, searcher.search(q,10).totalHits);
        }

        {
            Query q = new QueryParser("name", analyzer).parse("There");
            assertEquals(0, searcher.search(q,10).totalHits);
        }

        {
            Query q = new QueryParser("name", analyzer).parse("it's'");
            assertEquals(1, searcher.search(q,10).totalHits);
        }
    }

    /** Check same as standard filter
     *
     * @throws Exception
     */
    public void testAcronymHandling() throws Exception {
        Analyzer analyzer = new StandardUnaccentAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        Document doc = new Document();
        doc.add(new Field("name", "R.E.S.", Field.Store.YES, Field.Index.ANALYZED));

        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(dir,true);
        {
            Query q = new QueryParser("name", analyzer).parse("res");
            assertEquals(1, searcher.search(q,10).totalHits);
        }

        {
            Query q = new QueryParser("name", analyzer).parse("R.E.S.");
            assertEquals(1, searcher.search(q,10).totalHits);
        }

        {
            Query q = new QueryParser("name", analyzer).parse("R.E.S");
            assertEquals(1, searcher.search(q,10).totalHits);
        }

    }

}
