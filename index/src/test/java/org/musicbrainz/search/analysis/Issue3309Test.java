package org.musicbrainz.search.analysis;

import junit.framework.TestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;


public class Issue3309Test extends TestCase {

    public void testMatchAcronymnsWithoutTrailingDot() throws Exception {

        Analyzer analyzer = new StandardUnaccentAnalyzer();        
        RAMDirectory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        Document doc = new Document();
        doc.add(new Field("name", "R.E.S.", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(dir,true);
        Query q = new QueryParser(Version.LUCENE_CURRENT,"name", analyzer).parse("R.E.S");
        TopDocs docs = searcher.search(q,10);
        assertEquals(1, docs.totalHits);
    }

    public void testMatchAcronymnsWithTrailingDot() throws Exception {

        Analyzer analyzer = new StandardUnaccentAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        Document doc = new Document();
        doc.add(new Field("name", "R.E.S", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(dir,true);
        Query q = new QueryParser(Version.LUCENE_CURRENT,"name", analyzer).parse("R.E.S.");
        TopDocs docs = searcher.search(q,10);
        assertEquals(1, docs.totalHits);
    }

}
