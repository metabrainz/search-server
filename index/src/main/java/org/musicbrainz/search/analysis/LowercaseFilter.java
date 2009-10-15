package org.musicbrainz.search.analysis;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

import java.io.IOException;

/**
 * LOwercase Filter
 * <p/>
 * Like Lucenes Lowercase Filter but additionally lowercases Hiragana characters which don't seem
 * to be lowercased by Character.toLowerCase() for some reason
 */
public class LowercaseFilter extends TokenFilter {

    public LowercaseFilter(TokenStream in) {
        super(in);
        termAtt = (TermAttribute) addAttribute(TermAttribute.class);
    }

    private TermAttribute termAtt;

    public final boolean incrementToken() throws IOException {
        if (input.incrementToken()) {

            final char[] buffer = termAtt.termBuffer();
            final int length = termAtt.termLength();
            for (int i = 0; i < length; i++)
                switch (buffer[i]) {
                    case 'あ':
                        buffer[i] = 'ぁ';
                        break;
                    case 'い':
                        buffer[i] = 'ぃ';
                        break;
                    case 'う':
                        buffer[i] = 'ぅ';
                        break;
                    case 'え':
                        buffer[i] = 'ぇ';
                        break;
                    case 'お':
                        buffer[i] = 'ぉ';
                        break;
                    case 'つ':
                        buffer[i] = 'っ';
                        break;
                    case 'や':
                        buffer[i] = 'ゃ';
                        break;
                    case 'ゆ':
                        buffer[i] = 'ゅ';
                        break;
                    case 'よ':
                        buffer[i] = 'ょ';
                        break;
                    case 'わ':
                        buffer[i] = 'ゎ';
                        break;
                    case 'か':
                        buffer[i] = 'ゕ';
                        break;
                    case 'け':
                        buffer[i] = 'ゖ';
                        break;

                    default:
                        buffer[i] = Character.toLowerCase(buffer[i]);
                }
            return true;
        } else
            return false;
    }
}