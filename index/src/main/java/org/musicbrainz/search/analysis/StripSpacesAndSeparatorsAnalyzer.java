package org.musicbrainz.search.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.charfilter.MappingCharFilter;
import org.apache.lucene.analysis.charfilter.NormalizeCharMap;
import org.apache.lucene.analysis.core.KeywordTokenizer;

import java.io.IOException;
import java.io.Reader;

/**
 * For analyzing catalogno so can compare values containing spaces with values that do not
 * Removes any spaces and common seperators such as hyphens and lowercases the remaining text
 */
public class StripSpacesAndSeparatorsAnalyzer extends Analyzer {

    protected NormalizeCharMap charConvertMap;

    protected void setCharConvertMap() {

        NormalizeCharMap.Builder builder = new NormalizeCharMap.Builder();
        builder.add(" ","");
        builder.add("-","");
        builder.add("_","");
        builder.add(":","");
        charConvertMap = builder.build();
    }

    public StripSpacesAndSeparatorsAnalyzer() {
        setCharConvertMap();
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        Tokenizer source = new KeywordTokenizer(reader);
        TokenStream filter = new LowercaseFilter(source);
        return new TokenStreamComponents(source, filter);
    }

    @Override
    protected Reader initReader(String fieldName,
                                Reader reader)
    {
        return new MappingCharFilter(charConvertMap, reader);
    }
}
