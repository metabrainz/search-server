package org.musicbrainz.search.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizer;

import java.io.Reader;

/**
 * For analyzing barcodes and labelcodes treat those with leading zeroes and those without the same
 */
public class StripLeadingZeroesAnalyzer extends Analyzer {

    public StripLeadingZeroesAnalyzer() {

    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer source = new KeywordTokenizer();
        TokenStream filter = new StripLeadingZeroesFilter(source);
        return new TokenStreamComponents(source, filter);
    }
}
