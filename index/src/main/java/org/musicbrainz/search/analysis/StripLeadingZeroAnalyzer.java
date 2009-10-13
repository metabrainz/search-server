package org.musicbrainz.search.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordTokenizer;
import org.apache.lucene.analysis.TokenStream;

import java.io.IOException;
import java.io.Reader;

/**
 * For analyzing barcodes treat those with leading zero and those without the same
 */
public class StripLeadingZeroAnalyzer extends Analyzer {

    public StripLeadingZeroAnalyzer() {

    }

    public TokenStream tokenStream(String fieldName,
                                   final Reader reader) {
        TokenStream result = new KeywordTokenizer(reader);
        result = new StripLeadingZeroFilter(result);
        return result;
    }

    private static final class SavedStreams {
        KeywordTokenizer tokenStream;
        TokenStream filteredTokenStream;
    }

    public TokenStream reusableTokenStream(String fieldName,
                                           final Reader reader) throws IOException {

        SavedStreams streams = (SavedStreams) getPreviousTokenStream();
        if (streams == null) {
            streams = new SavedStreams();
            setPreviousTokenStream(streams);
            streams.tokenStream = new KeywordTokenizer(reader);
            streams.filteredTokenStream = new StripLeadingZeroFilter(streams.tokenStream);
        } else {
            streams.tokenStream.reset(reader);
        }
        return streams.filteredTokenStream;
    }
}
