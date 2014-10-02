/* Copyright (c) 2010 Paul Taylor
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

import org.apache.lucene.analysis.charfilter.NormalizeCharMap;

/**
 * Map unusual characters to simpler characters when we want both to be treated the same
 *
 * This applied on input BEFORE tokenization
 * Does not deal with Punctuation this may be removed by MusicBrainzTokenizer or MusicbrainzTokenizerFilter
 * Does not deal with diacritics these are dealt with by AccentFilter AFTER tokenization
 *
 */
public class CharEquivToCharHelper
{

    public static void addToMap(NormalizeCharMap.Builder charConvertMap)
    {
        //Apostrophes
        charConvertMap.add("’", "'");

        //Various Hyphens, from http://www.fileformat.info/info/unicode/category/Pd/list.htm
        charConvertMap.add("‐", "-");
        charConvertMap.add("–", "-");
        charConvertMap.add("‒", "-");
        charConvertMap.add("—", "-");
        charConvertMap.add("−", "-");
        charConvertMap.add("_", "-");

        //These are from Phonetic Extensions http://www.fileformat.info/info/unicode/block/phonetic_extensions/list.htm
        charConvertMap.add("ᴀ", "A");
        charConvertMap.add("ᴁ", "ae");
        charConvertMap.add("ᴂ", "ae");
        charConvertMap.add("ᴃ", "B");
        charConvertMap.add("ᴄ", "C");
        charConvertMap.add("ᴅ", "D");
        charConvertMap.add("ᴆ", "E");
        charConvertMap.add("ᴇ", "E");
        charConvertMap.add("ᴌ", "L");
        charConvertMap.add("ᴍ", "M");
        charConvertMap.add("ᴏ", "O");
        charConvertMap.add("ᴒ", "O");
        charConvertMap.add("ᴓ", "o");
        charConvertMap.add("ᴛ", "T");
        charConvertMap.add("ᴜ", "U");
        charConvertMap.add("ᴠ", "V");
        charConvertMap.add("ᴡ", "W");
        charConvertMap.add("ᴢ", "Z");
        charConvertMap.add("ᴬ", "A");
        charConvertMap.add("ᴭ", "AE");
        charConvertMap.add("ᴮ", "B");
        charConvertMap.add("ᴰ", "D");
        charConvertMap.add("ᴱ", "E");
        charConvertMap.add("ᴳ", "G");
        charConvertMap.add("ᴴ", "H");
        charConvertMap.add("ᴵ", "I");
        charConvertMap.add("ᴶ", "J");
        charConvertMap.add("ᴷ", "K");
        charConvertMap.add("ᴸ", "L");
        charConvertMap.add("ᴹ", "M");
        charConvertMap.add("ᴺ", "N");
        charConvertMap.add("ᴼ", "O");
        charConvertMap.add("ᴾ", "P");
        charConvertMap.add("ᴿ", "R");
        charConvertMap.add("ᵀ", "T");
        charConvertMap.add("ᵁ", "U");
        charConvertMap.add("ᵂ", "W");
        charConvertMap.add("ᵃ", "a");
        charConvertMap.add("ᵇ", "b");
        charConvertMap.add("ᵈ", "d");
        charConvertMap.add("ᵉ", "e");
        charConvertMap.add("ᵍ", "g");
        charConvertMap.add("ᵏ", "k");
        charConvertMap.add("ᵐ", "m");
        charConvertMap.add("ᵒ", "o");
        charConvertMap.add("ᵖ", "p");
        charConvertMap.add("ᵗ", "t");
        charConvertMap.add("ᵘ", "u");
        charConvertMap.add("ᵛ", "v");
        charConvertMap.add("ᵢ", "i");
        charConvertMap.add("ᵣ", "r");
        charConvertMap.add("ᵤ", "u");
        charConvertMap.add("ᵥ", "v");
        charConvertMap.add("ᵬ", "b");
        charConvertMap.add("ᵭ", "d");
        charConvertMap.add("ᵮ", "f");
        charConvertMap.add("ᵯ", "m");
        charConvertMap.add("ᵰ", "n");
        charConvertMap.add("ᵱ", "p");
        charConvertMap.add("ᵲ", "r");
        charConvertMap.add("ᵳ", "r");
        charConvertMap.add("ᵴ", "s");
        charConvertMap.add("ᵵ", "t");
        charConvertMap.add("ᵶ", "z");
        charConvertMap.add("ᵻ", "I");
        charConvertMap.add("ᵽ", "p");
        charConvertMap.add("ᵾ", "u");
        charConvertMap.add("ᵧ", "γ");
        charConvertMap.add("ᵨ", "ϱ");
        charConvertMap.add("ᵦ", "β");
        charConvertMap.add("ᵩ", "ϕ");
        charConvertMap.add("ᵪ", "χ");
        charConvertMap.add("ᵟ", "δ");
        charConvertMap.add("ᵡ", "χ");
        charConvertMap.add("ᴊ","j");
        charConvertMap.add("ı","i");

    }
}