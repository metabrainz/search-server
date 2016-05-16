/*
 Copyright (c) 2010 Paul Taylor
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.
  2. Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
     documentation and/or other materials provided with the distribution.
  3. Neither the name of the MusicBrainz project nor the names of the
     contributors may be used to endorse or promote products derived from
     this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.musicbrainz.search.analysis;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.search.similarities.DefaultSimilarity;

/**
 * Calculates a score for a match, overridden to deal with problems with alias fields in artist and label indexes
 */
//TODO in Lucene 4.1 we can now use PerFieldSimailrityWrapper so that we only oerform this on fields that need it, with
//current code tf() is performed on every field because we are not passed fieldname
public class MusicbrainzSimilarity extends DefaultSimilarity
{
   /**
     * Calculates a value which is inversely proportional to the number of terms in the field. When multiple
     * aliases are added to an artist (or label) it is seen as one field, so artists with many aliases can be
     * disadvantaged against when the matching alias is radically different to other aliases.
     *
     * @param state
     * @return
     */
    @Override
    public float lengthNorm(FieldInvertState state) {

        if (state.getName().equals("alias"))
        {
            if(state.getLength()>=3) {
                return state.getBoost() * 0.578f; //Same result as normal calc if field had three terms the most common scenario
            }
            else
            {
                return super.lengthNorm(state);
            }
        }
        else
        {
            return super.lengthNorm(state);
        }
    }

    /**
     * This method calculates a value based on how many times the search term was found in the field. Because
     * we have only short fields the only real case (apart from rare exceptions like Duran Duran Duran) whereby
     * the term term is found more than twice would be when
     * a search term matches multiples aliases, to remove the bias this gives towards artists/labels with
     * many aliases we limit the value to what would be returned for a two term match.
     *
     * Note: would prefer to do this just for alias field, but the field is not passed as a parameter.
     * @param freq
     * @return score component
     */
    @Override
    public float tf(float freq) {
        if (freq > 2.0f) {
            return 1.41f; //Same result as if matched term twice

        } else {
            return super.tf(freq);
        }
    }
}
