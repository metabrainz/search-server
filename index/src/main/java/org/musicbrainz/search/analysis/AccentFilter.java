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

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

import java.io.IOException;

/**
 * A filter that replaces accented characters by their unaccented equivalents.
 */
public class AccentFilter extends TokenFilter {

    private char[] output = new char[256];
    private int outputPos;

    private TermAttribute termAttr;

    public AccentFilter(TokenStream input) {
        super(input);
        termAttr = (TermAttribute) addAttribute(TermAttribute.class);
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (!input.incrementToken())
            return false;

        final char[] buffer = termAttr.termBuffer();
        final int length    = termAttr.termLength();
        if (removeAccents(buffer, length))  {
            termAttr.setTermBuffer(output, 0, outputPos);
        }
        return true;
    }

    protected final boolean removeAccents(char[] input, int length) {
        final int maxSizeNeeded = 2 * length;
        int size = output.length;
        while (size < maxSizeNeeded)
            size *= 2;

        int inputPos = 0;
        outputPos = 0;

        for (int i = 0; i < length; i++) {
            int c = (int) input[i];

            int block = UnaccentIndexes.indexes[c >> UnaccentData.BLOCK_SHIFT];
            int position = c & UnaccentData.BLOCK_MASK;

            short[] positions = UnaccentPositions.positions[block];
            int unacPosition = positions[position];
            int unacLength = positions[position + 1] - unacPosition;

            if (unacLength > 0) {
                // allocate a new char array, if necessary
                if (size != output.length)
                    output = new char[size];
                // copy front of the input
                if (inputPos < i) {
                    System.arraycopy(input, inputPos, output, outputPos, i - inputPos);
                    outputPos += i - inputPos;
                }
                // copy unaccented data
                System.arraycopy(UnaccentData.data[block], unacPosition,
                        output, outputPos, unacLength);
                outputPos += unacLength;
                inputPos = i + 1;
            }
        }

        // no conversion needed...
        if (inputPos == 0)
            return false;

        // copy rest of the input
        int copyLength = length - inputPos;
        if (copyLength > 0) {
            System.arraycopy(input, inputPos, output, outputPos, copyLength);
            outputPos += copyLength;
        }

        return true;
    }

}
