package org.musicbrainz.search.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.charfilter.MappingCharFilter;
import org.apache.lucene.analysis.charfilter.NormalizeCharMap;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.Test;
import org.musicbrainz.search.LuceneVersion;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AmpersandTest  {

    @Test
    public void testAmpersandTokenization() throws Exception {

        NormalizeCharMap.Builder builder = new NormalizeCharMap.Builder();
        AmpersandToAndMappingHelper.addToMap(builder);
        CharEquivToCharHelper.addToMap(builder);
        HebrewCharMappingHelper.addToMap(builder);
        NormalizeCharMap charConvertMap = builder.build();
        Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION,
                new MappingCharFilter(charConvertMap, new StringReader("platinum & gold")));

        assertTrue(tokenizer.incrementToken());
        CharTermAttribute term = tokenizer.addAttribute(CharTermAttribute.class);
        TypeAttribute type = tokenizer.addAttribute(TypeAttribute.class);
        OffsetAttribute offset = tokenizer.addAttribute(OffsetAttribute.class);
        assertEquals("<ALPHANUM>", type.type());
        assertEquals("platinum", new String(term.buffer(),0,term.length()));
        assertEquals(0, offset.startOffset());
        assertEquals(8, offset.endOffset());
        assertTrue(tokenizer.incrementToken());
        assertEquals("<ALPHANUM>", type.type());
        assertEquals("and", new String(term.buffer(),0,term.length()));
        assertEquals(9, offset.startOffset());
        assertEquals(10, offset.endOffset());
        assertTrue(tokenizer.incrementToken());

    }

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
            doc.add(new Field("name", "platinum & gold", Field.Store.YES, Field.Index.ANALYZED));
            writer.addDocument(doc);
        }
        writer.close();

        IndexReader ir = DirectoryReader.open(dir);
        Fields fields = MultiFields.getFields(ir);
        Terms terms = fields.terms("name");
        TermsEnum termsEnum = terms.iterator(null);
        BytesRef text;
        while((text = termsEnum.next()) != null) {
            System.out.println("--term=" + text.utf8ToString()+"--");
        }
        ir.close();

        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("\"platinum & gold\"");
            System.out.println(q);
            TopDocs td = searcher.search(q, 10);
            System.out.println("Size"+td.scoreDocs.length);
            assertEquals(1, searcher.search(q, 10).totalHits);
        }

        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse("\"Platinum and Gold\"");
            System.out.println(q);
            assertEquals(1, searcher.search(q, 10).totalHits);
        }
    }
}
