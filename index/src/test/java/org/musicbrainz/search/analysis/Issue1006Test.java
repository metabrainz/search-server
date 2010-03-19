package org.musicbrainz.search.analysis;

import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
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

import junit.framework.TestCase;
import com.ibm.icu.text.Transliterator;
import org.apache.lucene.util.Version;
import org.musicbrainz.search.LuceneVersion;

import java.io.StringReader;


public class Issue1006Test extends TestCase {

    /**
     * Tests word is preserved and identified as alpahnumeric
     *
     * @throws Exception
     */
    public void testKatakanaHiraganaTokenizer() throws Exception {

        {
            Tokenizer tokenizer = new StandardTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("ゲーム"));
            TermAttribute term = (TermAttribute) tokenizer.addAttribute(TermAttribute.class);
            TypeAttribute type = (TypeAttribute) tokenizer.addAttribute(TypeAttribute.class);
            OffsetAttribute offset = (OffsetAttribute) tokenizer.addAttribute(OffsetAttribute.class);
            while (tokenizer.incrementToken()) {
                assertEquals("<ALPHANUM>", type.type());
                assertEquals("ゲーム", term.term());
                assertEquals(0, offset.startOffset());
                assertEquals(3, offset.endOffset());


            }
        }

        {
            Tokenizer tokenizer = new StandardTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("ゲエム"));
            TermAttribute term = (TermAttribute) tokenizer.addAttribute(TermAttribute.class);
            TypeAttribute type = (TypeAttribute) tokenizer.addAttribute(TypeAttribute.class);
            OffsetAttribute offset = (OffsetAttribute) tokenizer.addAttribute(OffsetAttribute.class);

            while (tokenizer.incrementToken()) {


                assertEquals("<ALPHANUM>", type.type());
                assertEquals("ゲエム", term.term());
                assertEquals(0, offset.startOffset());
                assertEquals(3, offset.endOffset());

            }
        }

        {
            Tokenizer tokenizer = new StandardTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("げえむ"));
            TermAttribute term = (TermAttribute) tokenizer.addAttribute(TermAttribute.class);
            TypeAttribute type = (TypeAttribute) tokenizer.addAttribute(TypeAttribute.class);
            OffsetAttribute offset = (OffsetAttribute) tokenizer.addAttribute(OffsetAttribute.class);

            while (tokenizer.incrementToken()) {

                assertEquals("<ALPHANUM>", type.type());
                assertEquals("げえむ", term.term());
                assertEquals(0, offset.startOffset());
                assertEquals(3, offset.endOffset());
            }
        }
    }

    /**
     * Test ICU Transformer converts as expected
     *
     * @throws Exception
     */
    public void testKatakanaHiraganaEquivalenceFilter() throws Exception {

        {

            Tokenizer tokenizer = new StandardTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("ゲーム"));
            TokenStream result = new ICUTransformFilter(tokenizer, Transliterator.getInstance("[ー[:Script=Katakana:]]Katakana-Hiragana"));

            TermAttribute term = (TermAttribute) result.addAttribute(TermAttribute.class);
            TypeAttribute type = (TypeAttribute) result.addAttribute(TypeAttribute.class);
            OffsetAttribute offset = (OffsetAttribute) result.addAttribute(OffsetAttribute.class);

            while (result.incrementToken()) {

                assertEquals("<ALPHANUM>", type.type());
                assertEquals("げえむ", term.term());
                assertEquals(0, offset.startOffset());
                assertEquals(3, offset.endOffset());
            }
        }

        {

            Tokenizer tokenizer = new StandardTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("ゲエム"));
            TokenStream result = new ICUTransformFilter(tokenizer, Transliterator.getInstance("[ー[:Script=Katakana:]]Katakana-Hiragana"));

            TermAttribute term = (TermAttribute) result.addAttribute(TermAttribute.class);
            TypeAttribute type = (TypeAttribute) result.addAttribute(TypeAttribute.class);
            OffsetAttribute offset = (OffsetAttribute) result.addAttribute(OffsetAttribute.class);

            while (result.incrementToken()) {

                assertEquals("<ALPHANUM>", type.type());
                assertEquals("げえむ", term.term());
                assertEquals(0, offset.startOffset());
                assertEquals(3, offset.endOffset());
            }
        }

        {

            Tokenizer tokenizer = new StandardTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("げえむ"));
            TokenStream result = new ICUTransformFilter(tokenizer, Transliterator.getInstance("[ー[:Script=Katakana:]]Katakana-Hiragana"));

            TermAttribute term = (TermAttribute) result.addAttribute(TermAttribute.class);
            TypeAttribute type = (TypeAttribute) result.addAttribute(TypeAttribute.class);
            OffsetAttribute offset = (OffsetAttribute) result.addAttribute(OffsetAttribute.class);

            while (result.incrementToken()) {

                assertEquals("<ALPHANUM>", type.type());
                assertEquals("げえむ", term.term());
                assertEquals(0, offset.startOffset());
                assertEquals(3, offset.endOffset());
            }
        }

        
    }

    /**
     * Test filter is being used for indexing and searching
     */
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


        IndexSearcher searcher = new IndexSearcher(dir, true);
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("ゲーム");
            //System.out.println(q);
            assertEquals(3, searcher.search(q, 10).totalHits);
        }

        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("ゲエム");
            //System.out.println(q);
            assertEquals(3, searcher.search(q, 10).totalHits);
        }

        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("げえむ");
            //System.out.println(q);
            assertEquals(3, searcher.search(q, 10).totalHits);
        }

    }
}
