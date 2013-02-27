package org.musicbrainz.search.analysis;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;


public class StripLeadingZeroesFilter extends TokenFilter {

    /**
     * Construct filtering <i>in</i>.
     */
    public StripLeadingZeroesFilter(TokenStream in) {
        super(in);
        termAtt = addAttribute(CharTermAttribute.class);
    }

    private CharTermAttribute termAtt;

    /**
     *
     * <p>Removes zeroes if at start of token
     */
    public final boolean incrementToken() throws java.io.IOException {
        if (!input.incrementToken()) {
            return false;
        }

        char[] buffer = termAtt.buffer();
        final int bufferLength = termAtt.length();

        int countZeroes;
        for (countZeroes = 0; countZeroes < bufferLength; countZeroes++) {
            if(buffer[countZeroes]!='0') {
                break;
            }
        }
        if(countZeroes>0) {
            for (int i = countZeroes; i < bufferLength; i++) {
                char c = buffer[i];
                buffer[i - countZeroes] = c;
            }
            termAtt.setLength(bufferLength - countZeroes);
            return true;
        }
        return true;

    }

}
