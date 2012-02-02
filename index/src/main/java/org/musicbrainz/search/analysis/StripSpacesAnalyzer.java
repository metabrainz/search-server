package org.musicbrainz.search.analysis;

import org.apache.lucene.analysis.*;

import java.io.IOException;
import java.io.Reader;

/**
 * For analyzing catalogno so can compare values containing spaces with values that do not
 * Removes any spaces and lowercases the remaining text
 */
public class StripSpacesAnalyzer extends Analyzer {

    protected NormalizeCharMap charConvertMap;

    protected void setCharConvertMap() {
        charConvertMap = new NormalizeCharMap();
        charConvertMap.add(" ","");

    }

    public StripSpacesAnalyzer() {
        setCharConvertMap();
    }

    public final TokenStream tokenStream(String fieldName,
                                   final Reader reader) {
        CharFilter mappingCharFilter = new MappingCharFilter(charConvertMap,reader);
        TokenStream result = new KeywordTokenizer(mappingCharFilter);
        result = new LowercaseFilter(result);
        return result;
    }

    private static final class SavedStreams {
        KeywordTokenizer tokenStream;
        TokenStream filteredTokenStream;
    }

    public final TokenStream reusableTokenStream(String fieldName,
                                           final Reader reader) throws IOException {

        SavedStreams streams = (SavedStreams) getPreviousTokenStream();
        if (streams == null) {
            streams = new SavedStreams();
            setPreviousTokenStream(streams);
            streams.tokenStream = new KeywordTokenizer(new MappingCharFilter(charConvertMap, reader));
            streams.filteredTokenStream = new LowercaseFilter(streams.tokenStream);
        }
        else {
            streams.tokenStream.reset(new MappingCharFilter(charConvertMap,reader));
        }
        return streams.filteredTokenStream;
    }
}
