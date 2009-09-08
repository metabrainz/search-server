package org.musicbrainz.search.analysis;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.Token;

import junit.framework.TestCase;
import com.ibm.icu.text.Transliterator;

import java.io.StringReader;


public class Issue1006Test extends TestCase {

    /** Tests word is preserved and identified as alpahnumeric
     *
     * @throws Exception
     */
    public void testKatakanaHiraganaTokenizer() throws Exception {

        {
            Tokenizer tokenizer = new StandardTokenizer(new StringReader("ゲーム"));
            Token t;
            while((t =tokenizer.next())!=null)
            {
                System.out.println(t.toString());
                assertEquals("(ゲーム,0,3,type=<ALPHANUM>)",t.toString());
            }
        }

        {
            Tokenizer tokenizer = new StandardTokenizer(new StringReader("ゲエム"));
            Token t;
            while((t =tokenizer.next())!=null)
            {
                System.out.println(t.toString());
                assertEquals("(ゲエム,0,3,type=<ALPHANUM>)",t.toString());
            }
        }

        {
            Tokenizer tokenizer = new StandardTokenizer(new StringReader("げえむ"));
            Token t;
            while((t =tokenizer.next())!=null)
            {
                System.out.println(t.toString());
                assertEquals("(げえむ,0,3,type=<ALPHANUM>)",t.toString());
            }
        }
    }

    /** Test ICU Transformer converts as expected
     *
     * @throws Exception
     */
    public void testKatakanaHiraganaEquivalenceFilter() throws Exception {

        {
            Tokenizer tokenizer = new StandardTokenizer(new StringReader("ゲーム"));
            TokenStream result = new ICUTransformFilter(tokenizer, Transliterator.getInstance("[ー[:Script=Katakana:]]Katakana-Hiragana"));
            Token t;
            while((t =result.next())!=null)
            {
                System.out.println(t.toString());
                assertEquals("(げえむ,0,3,type=<ALPHANUM>)",t.toString());
            }
        }

        {
            Tokenizer tokenizer = new StandardTokenizer(new StringReader("ゲエム"));
            TokenStream result = new ICUTransformFilter(tokenizer, Transliterator.getInstance("[ー[:Script=Katakana:]]Katakana-Hiragana"));
            Token t;
            while((t =result.next())!=null)
            {
                System.out.println(t.toString());
                assertEquals("(げえむ,0,3,type=<ALPHANUM>)",t.toString());
            }
        }

        {
            Tokenizer tokenizer = new StandardTokenizer(new StringReader("げえむ"));
            TokenStream result = new ICUTransformFilter(tokenizer, Transliterator.getInstance("[ー[:Script=Katakana:]]Katakana-Hiragana"));
            Token t;
            while((t =result.next())!=null)
            {
                System.out.println(t.toString());
                assertEquals("(げえむ,0,3,type=<ALPHANUM>)",t.toString());
            }
        }
    }

    /** Test filter is being used for indexing and searching */
    public void testKatakanaHiraganaEquivalence() throws Exception {

        Analyzer analyzer = new StandardUnaccentAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        {
            Document doc = new Document();
            doc.add(new Field("name", "ゲーム", Field.Store.YES, Field.Index.ANALYZED));
            writer.addDocument(doc);
        }
        {
            Document doc = new Document();
            doc.add(new Field("name", "ゲエム", Field.Store.YES, Field.Index.ANALYZED));
            writer.addDocument(doc);
        }
        {
            Document doc = new Document();
            doc.add(new Field("name", "げえむ", Field.Store.YES, Field.Index.ANALYZED));
            writer.addDocument(doc);
        }
        writer.close();



        IndexSearcher searcher = new IndexSearcher(dir,true);
        {
            Query q = new QueryParser("name", analyzer).parse("ゲーム");
            System.out.println(q);
            assertEquals(3, searcher.search(q,10).totalHits);
        }

        {
            Query q = new QueryParser("name", analyzer).parse("ゲエム");
            System.out.println(q);
            assertEquals(3, searcher.search(q,10).totalHits);
        }

        {
            Query q = new QueryParser("name", analyzer).parse("げえむ");
            System.out.println(q);
            assertEquals(3, searcher.search(q,10).totalHits);
        }

    }
}
