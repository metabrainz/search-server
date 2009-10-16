package org.musicbrainz.search.analysis;

import org.apache.lucene.analysis.*;

import java.io.Reader;
import java.io.IOException;


/**
 * Keeps input as simple token but always converts to lowercase, to allow searching user uppercase or lowercase
 * queries
 */
public class CaseInsensitiveKeywordAnalyzer extends Analyzer {

    public CaseInsensitiveKeywordAnalyzer() {

    }

    public TokenStream tokenStream(String fieldName,
                                   final Reader reader) {
        TokenStream result = new KeywordTokenizer(reader);
        result = new LowerCaseFilter(result);
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
            streams.filteredTokenStream = new LowerCaseFilter(streams.tokenStream);
        } else  {
            streams.tokenStream.reset(reader);
        }
        return streams.filteredTokenStream;
    }

}
