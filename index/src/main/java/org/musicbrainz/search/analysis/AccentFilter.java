package org.musicbrainz.search.analysis;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Decomposes text so that diacritics are separated out from the character itself and then removed.
 * It also removes some symbolics and phonetic modifier letters
 *
 * Using this filter means that searches with and without these characters are treated the same, it also means that usually different representations
 * of the same complex character are treated the same because both derived to the same simpler character.
 *
 * Usually this will provide better results
 * however if the character removed is intrinic to what it is being searched for it may make the search worse, In these cases you should
 * use an anlayser that doesnt use this filter.
 *
 * InCombiningDiacriticalMarks: special marks that are part of "normal" ä, ö, î etc..
 * IsSk: Symbol, Modifier see http://www.fileformat.info/info/unicode/category/Sk/list.htm
 * IsLm: Letter, Modifier see http://www.fileformat.info/info/unicode/category/Lm/list.htm
 */
public final class AccentFilter extends TokenFilter
{

    public static final Pattern DIACRITICS_AND_FRIENDS
            = Pattern.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");


    private CharTermAttribute termAtt;

    public AccentFilter(TokenStream input) {
        super(input);
        termAtt = (CharTermAttribute) addAttribute(CharTermAttribute.class);
    }

    @Override
    public final boolean incrementToken() throws IOException
    {
        if (input.incrementToken()) {
            String result = stripDiacritics(new String(termAtt.buffer()).substring(0,termAtt.length()));
            char[] newBuffer = result.toCharArray();
            termAtt.copyBuffer(newBuffer, 0, newBuffer.length);
            termAtt.setLength(newBuffer.length);
            return true;
        } else {
            return false;
        }
    }

    private static String stripDiacritics(String str) {
        String normalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        String simplifiedString = DIACRITICS_AND_FRIENDS.matcher(normalizedString).replaceAll("");
        System.out.println(str+":"+normalizedString+":"+simplifiedString);
        return simplifiedString;
    }
}