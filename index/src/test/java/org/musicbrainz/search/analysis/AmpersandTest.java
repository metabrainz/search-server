package org.musicbrainz.search.analysis;

import org.apache.lucene.analysis.Analyzer;
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

import static org.junit.Assert.assertEquals;

public class AmpersandTest  {


    /**
     * Test filter is being used for indexing and searching , and can search either way
     */
    @Test
    public void testAmpersandSearching() throws Exception {

        Analyzer analyzer = new MusicbrainzAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        {
            Document doc = new Document();
            doc.add(new Field("name", "Platinum & Gold", Field.Store.YES, Field.Index.ANALYZED));
            writer.addDocument(doc);
        }
        writer.close();


        IndexSearcher searcher = new IndexSearcher(IndexReader.open(dir));
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("\"Platinum & Gold\"");
            System.out.println(q);
            assertEquals(1, searcher.search(q, 10).totalHits);
        }

        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("\"Platinum and Gold\"");
            System.out.println(q);
            assertEquals(1, searcher.search(q, 10).totalHits);
        }
    }
}
