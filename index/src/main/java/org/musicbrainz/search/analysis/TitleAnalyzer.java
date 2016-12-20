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
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.charfilter.MappingCharFilter;
import org.apache.lucene.analysis.charfilter.NormalizeCharMap;
import org.apache.lucene.analysis.cjk.CJKBigramFilter;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.icu.ICUTransformFilter;
import org.musicbrainz.search.LuceneVersion;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Pattern;

/**
 * Should be used for for analysing titles such as track title,release title or recording title
 * because contains special processing for titles that isn't required for other text fields such as artist name.
 *
 * Filters MusicbrainzTokenizer with MusicbrainzTokenizerFilter, ICUTransformFilter, AccentFilter, LowerCaseFilter
 * and no stop words.
 */
public class TitleAnalyzer extends Analyzer {

    private NormalizeCharMap charConvertMap;

    //We convert to the wrong form No.1 rather than the correct form No. 1 because this keeps it as single token
    //when tokenized so doesn't incorrectly match additional single numbers in the text.
    private Pattern no1Pattern = Pattern.compile("(no\\.) (\\d+)", Pattern.CASE_INSENSITIVE);
    private String no1PatternReplacement = "$1$2";

    private void setCharConvertMap() {
        NormalizeCharMap.Builder builder = new NormalizeCharMap.Builder();
        AmpersandToAndMappingHelper.addToMap(builder);
        CharEquivToCharHelper.addToMap(builder);
        HebrewCharMappingHelper.addToMap(builder);
        charConvertMap = builder.build();
    }

    public TitleAnalyzer() {
        setCharConvertMap();
    }


    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer source = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION);
        TokenStream filter = new ICUTransformFilter(source, Transliterator.getInstance("[ー[:Script=Katakana:]]Katakana-Hiragana"));
        filter = new ICUTransformFilter(filter, Transliterator.getInstance("Traditional-Simplified"));
        filter = new AccentFilter(filter);
        filter = new MusicbrainzTokenizerFilter(filter);
        filter = new CJKBigramFilter(filter);
        filter = new LowercaseFilter(filter);
        filter = new MusicbrainzWordDelimiterFilter(filter,
                WordDelimiterIterator.DEFAULT_WORD_DELIM_TABLE,
                1,
                0,
                0,
                6,
                0,
                0,
                0,
                0,
                0,
                null);
        return new TokenStreamComponents(source, filter);
    }

    @Override
    protected Reader initReader(String fieldName,
                                Reader reader)
    {
        return new PatternReplaceCharFilter(no1Pattern, no1PatternReplacement,
            new MappingCharFilter(charConvertMap, reader));
    }
}
