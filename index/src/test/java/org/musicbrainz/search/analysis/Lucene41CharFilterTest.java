package org.musicbrainz.search.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.charfilter.MappingCharFilter;
import org.apache.lucene.analysis.charfilter.NormalizeCharMap;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.musicbrainz.search.LuceneVersion;

import java.io.Reader;

import static org.junit.Assert.assertEquals;

public class Lucene41CharFilterTest
{
    class SimpleAnalyzer extends Analyzer {

        protected NormalizeCharMap charConvertMap;

        protected void setCharConvertMap() {

            NormalizeCharMap.Builder builder = new NormalizeCharMap.Builder();
            builder.add("&","and");
            charConvertMap = builder.build();
        }

        public SimpleAnalyzer() {
            setCharConvertMap();
        }

        @Override
        protected TokenStreamComponents createComponents(String fieldName) {
            Tokenizer source = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION);
            TokenStream filter = new LowerCaseFilter(source);
            return new TokenStreamComponents(source, filter);
        }

        @Override
        protected Reader initReader(String fieldName,
                                    Reader reader)
        {
            return new MappingCharFilter(charConvertMap, reader);
        }
    }

    @Test
    public void testAmpersandSearching() throws Exception {

        Analyzer analyzer = new SimpleAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        {
            Document doc = new Document();
            doc.add(new Field("name", "Platinum & Gold", TextField.TYPE_STORED));
            writer.addDocument(doc);
        }
        writer.close();

        IndexReader ir = DirectoryReader.open(dir);
        Fields fields = MultiFields.getFields(ir);
        Terms terms = fields.terms("name");
        TermsEnum termsEnum = terms.iterator();
        BytesRef text;
        while((text = termsEnum.next()) != null) {
            System.out.println("--term=" + text.utf8ToString()+"--");
        }
        ir.close();

        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
        {
            Query q = new QueryParser("name", analyzer).parse("\"Platinum and Gold\"");
            System.out.println(q);
            TopDocs td = searcher.search(q, 10);
            System.out.println("Size"+td.scoreDocs.length);
            assertEquals(1, searcher.search(q, 10).totalHits);
        }

        searcher = new IndexSearcher(DirectoryReader.open(dir));
        {
            Query q = new QueryParser("name", analyzer).parse("\"Platinum & Gold\"");
            TopDocs td = searcher.search(q, 10);
            System.out.println("Size"+td.scoreDocs.length);
            assertEquals(1, searcher.search(q, 10).totalHits);
        }
    }
}
