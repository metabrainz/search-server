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
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.charfilter.MappingCharFilter;
import org.apache.lucene.analysis.charfilter.NormalizeCharMap;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.musicbrainz.search.LuceneVersion;

import java.io.Reader;

/**
 * Experimental uses the new Lucene 4.0 Tokenizer
 *
 * Filters MusicbrainzTokenizer with MusicbrainzTokenizerFilter, ICUTransformFilter, AccentFilter, LowerCaseFilter
 * and no stop words.
 */
public class NewMusicbrainzAnalyzer extends Analyzer {

    protected NormalizeCharMap charConvertMap;

    protected void setCharConvertMap() {

        NormalizeCharMap.Builder builder = new NormalizeCharMap.Builder();
        AmpersandToAndMappingHelper.addToMap(builder);
        CharEquivToCharHelper.addToMap(builder);
        HebrewCharMappingHelper.addToMap(builder);
        charConvertMap = builder.build();

    }

    public NewMusicbrainzAnalyzer() {
        //setCharConvertMap();
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer source = new StandardTokenizer();
        //TokenStream filter = new ICUTransformFilter(source, Transliterator.getInstance("[ー[:Script=Katakana:]]Katakana-Hiragana"));
        //filter = new ICUTransformFilter(filter, Transliterator.getInstance("Traditional-Simplified"));
        TokenStream filter = new StandardFilter(source);
        //filter = new AccentFilter(filter);
        filter = new LowercaseFilter(filter);
        /*filter = new MusicbrainzWordDelimiterFilter(filter,
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
        */
        return new TokenStreamComponents(source, filter);
    }

    /*
    @Override
    protected Reader initReader(String fieldName,
                                Reader reader)
    {
        return new MappingCharFilter(charConvertMap, reader);
    }
    */

}
