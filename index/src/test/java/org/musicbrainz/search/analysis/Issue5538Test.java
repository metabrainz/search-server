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

/** Test that analyser treats No.x and No. x the same (where x can be any number) , because both forms are found
 * in the database.
 */
public class Issue5538Test extends TestCase {


    public void testNo1Handling() throws Exception {

        Analyzer analyzer = new TitleAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        Document doc = new Document();
        doc.add(new Field("name", "No. 11", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(dir,true);
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("\"no. 11\"");
            assertEquals(1, searcher.search(q,10).totalHits);
        }

        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("\"no.11\"");
            assertEquals(1, searcher.search(q,10).totalHits);
        }


    }

}