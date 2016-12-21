package org.musicbrainz.search.analysis;

import com.ibm.icu.text.Transliterator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.icu.ICUTransformFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;
import org.musicbrainz.search.LuceneVersion;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;


public class Issue1006Test {

    /**
     * Tests word is preserved and identified as alpahnumeric
     *
     * @throws Exception
     */
    @Test
    public void testKatakanaHiraganaTokenizer() throws Exception {

        {
            Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION);
            tokenizer.setReader(new StringReader("ゲーム"));
            tokenizer.reset();
            CharTermAttribute term = (CharTermAttribute) tokenizer.addAttribute(CharTermAttribute.class);
            TypeAttribute type = (TypeAttribute) tokenizer.addAttribute(TypeAttribute.class);
            OffsetAttribute offset = (OffsetAttribute) tokenizer.addAttribute(OffsetAttribute.class);
            while (tokenizer.incrementToken()) {
                assertEquals("<ALPHANUM>", type.type());
                assertEquals("ゲーム", new String(term.buffer(),0,term.length()));
                assertEquals(0, offset.startOffset());
                assertEquals(3, offset.endOffset());


            }
        }

        {
            Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION);
            tokenizer.setReader(new StringReader("ゲエム"));
            tokenizer.reset();
            CharTermAttribute term = (CharTermAttribute) tokenizer.addAttribute(CharTermAttribute.class);
            TypeAttribute type = (TypeAttribute) tokenizer.addAttribute(TypeAttribute.class);
            OffsetAttribute offset = (OffsetAttribute) tokenizer.addAttribute(OffsetAttribute.class);

            while (tokenizer.incrementToken()) {


                assertEquals("<ALPHANUM>", type.type());
                assertEquals("ゲエム", new String(term.buffer(),0,term.length()));
                assertEquals(0, offset.startOffset());
                assertEquals(3, offset.endOffset());

            }
        }

        {
            Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION);
            tokenizer.setReader(new StringReader("げえむ"));
            tokenizer.reset();
            CharTermAttribute term = (CharTermAttribute) tokenizer.addAttribute(CharTermAttribute.class);
            TypeAttribute type = (TypeAttribute) tokenizer.addAttribute(TypeAttribute.class);
            OffsetAttribute offset = (OffsetAttribute) tokenizer.addAttribute(OffsetAttribute.class);

            while (tokenizer.incrementToken()) {

                assertEquals("<ALPHANUM>", type.type());
                assertEquals("げえむ", new String(term.buffer(),0,term.length()));
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
    @Test
    public void testKatakanaHiraganaEquivalenceFilter() throws Exception {

        {

            Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION);
            tokenizer.setReader(new StringReader("ゲーム"));
            tokenizer.reset();
            TokenStream result = new ICUTransformFilter(tokenizer, Transliterator.getInstance("[ー[:Script=Katakana:]]Katakana-Hiragana"));

            CharTermAttribute term = (CharTermAttribute) result.addAttribute(CharTermAttribute.class);
            TypeAttribute type = (TypeAttribute) result.addAttribute(TypeAttribute.class);
            OffsetAttribute offset = (OffsetAttribute) result.addAttribute(OffsetAttribute.class);

            while (result.incrementToken()) {

                assertEquals("<ALPHANUM>", type.type());
                assertEquals("げえむ", new String(term.buffer(),0,term.length()));
                assertEquals(0, offset.startOffset());
                assertEquals(3, offset.endOffset());
            }
        }

        {

            Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION);
            tokenizer.setReader(new StringReader("ゲエム"));
            tokenizer.reset();
            TokenStream result = new ICUTransformFilter(tokenizer, Transliterator.getInstance("[ー[:Script=Katakana:]]Katakana-Hiragana"));

            CharTermAttribute term = (CharTermAttribute) result.addAttribute(CharTermAttribute.class);
            TypeAttribute type = (TypeAttribute) result.addAttribute(TypeAttribute.class);
            OffsetAttribute offset = (OffsetAttribute) result.addAttribute(OffsetAttribute.class);

            while (result.incrementToken()) {

                assertEquals("<ALPHANUM>", type.type());
                assertEquals("げえむ", new String(term.buffer(),0,term.length()));
                assertEquals(0, offset.startOffset());
                assertEquals(3, offset.endOffset());
            }
        }

        {

            Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION);
            tokenizer.setReader(new StringReader("げえむ"));
            tokenizer.reset();
            TokenStream result = new ICUTransformFilter(tokenizer, Transliterator.getInstance("[ー[:Script=Katakana:]]Katakana-Hiragana"));

            CharTermAttribute term = (CharTermAttribute) result.addAttribute(CharTermAttribute.class);
            TypeAttribute type = (TypeAttribute) result.addAttribute(TypeAttribute.class);
            OffsetAttribute offset = (OffsetAttribute) result.addAttribute(OffsetAttribute.class);

            while (result.incrementToken()) {

                assertEquals("<ALPHANUM>", type.type());
                assertEquals("げえむ", new String(term.buffer(),0,term.length()));
                assertEquals(0, offset.startOffset());
                assertEquals(3, offset.endOffset());
            }
        }

        
    }

    /**
     * Test filter is being used for indexing and searching
     */
    @Test
    public void testKatakanaHiraganaEquivalence() throws Exception {

        Analyzer analyzer = new MusicbrainzAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        {
            Document doc = new Document();
            doc.add(new Field("name", "ゲーム", TextField.TYPE_STORED));
            writer.addDocument(doc);
        }
        {
            Document doc = new Document();
            doc.add(new Field("name", "ゲエム", TextField.TYPE_STORED));
            writer.addDocument(doc);
        }
        {
            Document doc = new Document();
            doc.add(new Field("name", "げえむ", TextField.TYPE_STORED));
            writer.addDocument(doc);
        }
        writer.close();


        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
        {
            Query q = new QueryParser("name", analyzer).parse("ゲーム");
            //System.out.println(q);
            assertEquals(3, searcher.search(q, 10).totalHits);
        }

        {
            Query q = new QueryParser("name", analyzer).parse("ゲエム");
            //System.out.println(q);
            assertEquals(3, searcher.search(q, 10).totalHits);
        }

        {
            Query q = new QueryParser("name", analyzer).parse("げえむ");
            //System.out.println(q);
            assertEquals(3, searcher.search(q, 10).totalHits);
        }

    }
}
