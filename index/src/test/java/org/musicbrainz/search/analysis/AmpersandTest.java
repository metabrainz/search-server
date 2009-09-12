package org.musicbrainz.search.analysis;

import junit.framework.TestCase;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.queryParser.QueryParser;

import java.io.StringReader;


public class AmpersandTest extends TestCase {

    /** Show tokenizer on its own removes ampersands */
    public void testAmpersandTokenizing() throws Exception {
        {
            Tokenizer tokenizer = new StandardTokenizer(new StringReader("Platinum & Gold"));
            Token t;
            int count=0;
            while((t =tokenizer.next())!=null)
            {
                //System.out.println(t.toString());
                count++;
            }
            assertEquals(2,count);
        }
    }

     /** Test filter is being used for indexing and searching , and can search either way*/
    public void testAmpersandSearching() throws Exception {

        Analyzer analyzer = new StandardUnaccentAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        {
            Document doc = new Document();
            doc.add(new Field("name","Platinum & Gold", Field.Store.YES, Field.Index.ANALYZED));
            writer.addDocument(doc);
        }
        writer.close();


        IndexSearcher searcher = new IndexSearcher(dir,true);
        {
            Query q = new QueryParser("name", analyzer).parse("\"Platinum & Gold\"");
            System.out.println(q);
            assertEquals(1, searcher.search(q,10).totalHits);
        }

        {
            Query q = new QueryParser("name", analyzer).parse("\"Platinum and Gold\"");
            System.out.println(q);
            assertEquals(1, searcher.search(q,10).totalHits);
        }
    }
}
