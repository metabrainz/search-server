/*
 Copyright (c) 2012 Paul Taylor
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
import org.apache.lucene.search.DefaultSimilarity;
import org.musicbrainz.search.index.ReleaseGroupIndexField;

/**
 * Calculates a score for a match, overridden to deal with problems with releasegroup linked to many releases
 */
public class ReleaseGroupSimilarity extends DefaultSimilarity {

    /**
     * Calculates a value which is inversely proportional to the number of terms in the field. When multiple
     * releases are added to a release group it is seen as one field, so release groups  with many releases can be
     * disadvantaged against.
     *
     * But we don't want to just disable norms for release field as the number of terms in a release name
     * should effect scoring
     *
     * @return score component
     */
    public float computeNorm(String field, FieldInvertState state) {
        if (field.equals(ReleaseGroupIndexField.RELEASE)) {
            if(state.getLength()>=6) {
                //Same result as normal calc if field had six terms, based on the view that most common release title
                //is 5 terms
                return state.getBoost() * 0.408f;
            }
            else {
                return super.computeNorm(field,state);
            }
        }
        else
        {
            return super.computeNorm(field,state);
        }
    }

    /**
     * Calculates a value based on how many times the search term was found in the field.
     *
     * Because we have only short fields for most terms the frequency should usually be 1, occasionally 2.
     *
     * However if a release group contains many releases all withe same name, then release groups linked to
     * multiple releases would have an unfair advantage.
     *
     * The default computeNorm method would cancel out this advantage,  but we don't calculate the norms for the
     * releaseName field because that could give a disadvantage
     *
     * Note: would prefer to do this just for the release fields, but the field name is not passed as a parameter.
     * @param freq
     * @return score component
     */
    @Override
    public float tf(float freq) {
        if (freq >= 2.0f) {
            return 1.41f; //Same result as if contains smae term twice

        } else {
            return super.tf(freq);
        }
    }
}
