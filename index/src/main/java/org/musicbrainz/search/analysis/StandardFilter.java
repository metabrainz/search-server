package org.musicbrainz.search.analysis;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;


/**
 * Normalizes tokens extracted with {@link org.apache.lucene.analysis.standard.StandardTokenizer}.
 * <p/>
 * This is based on StandardFilter in that tokens identified as Acronyms have there dots removed but unlike StandardFilter
 * apostrophes are always removed and there is no special rule for words ending in 's .
 * <p/>
 * Whereas StandardFilter usually leaves apostrophes unless word ends with 's whereby the 's is removed.
 */

public class StandardFilter extends TokenFilter {
    /**
     * Construct filtering <i>in</i>.
     */
    public StandardFilter(TokenStream in) {
        super(in);
        termAtt = (CharTermAttribute) addAttribute(CharTermAttribute.class);
        typeAtt = (TypeAttribute) addAttribute(TypeAttribute.class);
    }

    private static final String APOSTROPHE_TYPE = StandardTokenizer.TOKEN_TYPES[StandardTokenizer.APOSTROPHE];
    private static final String ACRONYM_TYPE = StandardTokenizer.TOKEN_TYPES[StandardTokenizer.ACRONYM];

    // this filters uses attribute type
    private TypeAttribute       typeAtt;
    private CharTermAttribute   termAtt;

    /**
     * Returns the next token in the stream, or null at EOS.
     * <p>Removes <tt>'</tt> from the words.
     * <p>Removes dots from acronyms.
     */
    public final boolean incrementToken() throws java.io.IOException {
        if (!input.incrementToken()) {
            return false;
        }

        char[] buffer = termAtt.buffer();
        final int bufferLength = termAtt.length();
        final String type = typeAtt.type();

        if (type == APOSTROPHE_TYPE) {      // remove apostrophe
            int upto = 0;
            for (int i = 0; i < bufferLength; i++) {
                char c = buffer[i];
                if (c != '\'') {
                    buffer[upto++] = c;
                }
            }
            termAtt.setLength(upto);
        } else if (type == ACRONYM_TYPE) {      // remove dots
            int upto = 0;
            for (int i = 0; i < bufferLength; i++) {
                char c = buffer[i];
                if (c != '.') {
                    buffer[upto++] = c;
                }
            }
            termAtt.setLength(upto);
        }

        return true;
    }
}
