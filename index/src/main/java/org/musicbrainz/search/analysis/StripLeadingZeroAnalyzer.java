package org.musicbrainz.search.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizer;

import java.io.IOException;
import java.io.Reader;

/**
 * For analyzing barcodes and labelcodes treat those with leading zero and those without the same
 */
//TODO currently strips first zero, is there any requirement to strip multiple zeroes.
public class StripLeadingZeroAnalyzer extends Analyzer {

    public StripLeadingZeroAnalyzer() {

    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        Tokenizer source = new KeywordTokenizer(reader);
        TokenStream filter = new StripLeadingZeroFilter(source);
        return new TokenStreamComponents(source, filter);
    }
}
