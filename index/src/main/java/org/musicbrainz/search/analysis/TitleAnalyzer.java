/* Copyright (c) 2008 Lukas Lalinsky
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the MusicBrainz project nor the names of the
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.musicbrainz.search.analysis;

import com.ibm.icu.text.Transliterator;
import org.apache.lucene.analysis.*;
import org.apache.lucene.util.Version;
import org.musicbrainz.search.LuceneVersion;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Pattern;

/**
 * Should be used for for analysing titles such as track title,release title or recording title
 * because contains special processing for titles that isn't required for other text fields such as artist name.
 *
 * Filters StandardTokenizer with StandardFilter, ICUTransformFilter, AccentFilter, LowerCaseFilter
 * and no stop words.
 */
public class TitleAnalyzer extends Analyzer {

    private NormalizeCharMap charConvertMap;

    //We convert to the wrong form No.1 rather than the correct form No. 1 because this keeps it as single token
    //when tokenized so doesn't incorrectly match additional single numbers in the text.
    private Pattern no1Pattern = Pattern.compile("(no\\.) (\\d+)", Pattern.CASE_INSENSITIVE);
    private String no1PatternReplacement = "$1$2";

    private void setCharConvertMap() {
        charConvertMap = new NormalizeCharMap();
        charConvertMap.add("&", "and");
        //Hebrew chars converted to western cases so matches both
        charConvertMap.add("\u05f3","'");
        charConvertMap.add("\u05be","-");
        charConvertMap.add("\u05f4","\"");
    }

    public TitleAnalyzer() {
        setCharConvertMap();
    }

    public TokenStream tokenStream(String fieldName, Reader reader) {
        CharFilter mappingCharFilter = new MappingCharFilter(charConvertMap, reader);
        CharFilter no1CharFilter = new PatternReplaceCharFilter(no1Pattern, no1PatternReplacement, mappingCharFilter);
        StandardTokenizer tokenStream = new StandardTokenizer(LuceneVersion.LUCENE_VERSION, no1CharFilter);
        TokenStream result = new ICUTransformFilter(tokenStream, Transliterator.getInstance("[ー[:Script=Katakana:]]Katakana-Hiragana"));
        result = new ICUTransformFilter(result, Transliterator.getInstance("Traditional-Simplified"));
        result = new StandardFilter(result);
        result = new AccentFilter(result);
        result = new LowercaseFilter(result);
        return result;
    }

    private static final class SavedStreams {
        StandardTokenizer tokenStream;
        TokenStream filteredTokenStream;
    }

    public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException {
        SavedStreams streams = (SavedStreams) getPreviousTokenStream();
        if (streams == null) {
            streams = new SavedStreams();
            setPreviousTokenStream(streams);
            streams.tokenStream = new StandardTokenizer(LuceneVersion.LUCENE_VERSION, new PatternReplaceCharFilter(no1Pattern, no1PatternReplacement, new MappingCharFilter(charConvertMap, reader)));
            streams.filteredTokenStream = new ICUTransformFilter(streams.tokenStream, Transliterator.getInstance("[ー[:Script=Katakana:]]Katakana-Hiragana"));
            streams.filteredTokenStream = new ICUTransformFilter(streams.filteredTokenStream, Transliterator.getInstance("Traditional-Simplified"));
            streams.filteredTokenStream = new StandardFilter(streams.filteredTokenStream);
            streams.filteredTokenStream = new AccentFilter(streams.filteredTokenStream);
            streams.filteredTokenStream = new LowercaseFilter(streams.filteredTokenStream);
        } else {
            streams.tokenStream.reset(new PatternReplaceCharFilter(no1Pattern, no1PatternReplacement, new MappingCharFilter(charConvertMap, reader)));
        }
        return streams.filteredTokenStream;
    }
}