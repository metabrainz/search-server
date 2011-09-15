package org.musicbrainz.search.analysis;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;


public class StripLeadingZeroFilter extends TokenFilter {
    /**
     * Construct filtering <i>in</i>.
     */
    public StripLeadingZeroFilter(TokenStream in) {
        super(in);
        termAtt = (CharTermAttribute) addAttribute(CharTermAttribute.class);
    }

    private CharTermAttribute termAtt;

    /**
     *
     * <p>Removes zeroes if first char in token
     */
    public final boolean incrementToken() throws java.io.IOException {
        if (!input.incrementToken()) {
            return false;
        }

        char[] buffer = termAtt.buffer();
        final int bufferLength = termAtt.length();

        if (buffer[0] == '0') {
            for (int i = 1; i < bufferLength; i++) {
                char c = buffer[i];
                buffer[i - 1] = c;
            }
            termAtt.setLength(bufferLength - 1);
            return true;
        } else {
            return true;
        }
    }

}
