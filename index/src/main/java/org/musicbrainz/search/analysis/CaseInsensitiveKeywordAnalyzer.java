package org.musicbrainz.search.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.charfilter.MappingCharFilter;
import org.apache.lucene.analysis.core.KeywordTokenizer;

import java.io.IOException;
import java.io.Reader;


/**
 * Keeps input as simple token but always converts to lowercase, to allow searching user uppercase or lowercase
 * queries
 */
public class CaseInsensitiveKeywordAnalyzer extends Analyzer {

    public CaseInsensitiveKeywordAnalyzer() {

    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        Tokenizer source = new KeywordTokenizer(reader);
        TokenStream filter = new LowercaseFilter(source);
        return new TokenStreamComponents(source, filter);
    }

}

