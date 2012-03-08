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
import org.musicbrainz.search.index.RecordingIndex;
import org.musicbrainz.search.index.RecordingIndexField;

/**
 * Calculates a score for a match, overridden to deal with problems with recordings linked to many releases
 */
public class RecordingSimilarity extends DefaultSimilarity {

    /**
     * Calculates a value which is inversely proportional to the number of terms in the field. When multiple
     * releases are added to a recording it is seen as one field, so recordings with many releases can be
     * disadvantaged against.
     *
     * But we don't want to just disable norms for release field as the number of terms in a release name
     * should effect scoring
     *
     * @return score component
     */
    public float computeNorm(String field, FieldInvertState state) {
        if (field.equals(RecordingIndexField.RELEASE)) {

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
     * However all fields that comes from releases could have a much larger tf, i.e if matching the status field and
     * the recording link to 20 releases all with status 'official' then the tf will be 4.47 (sqrt (20)) so to protect
     * against this if the frequency is more than 2 we just treat as 2.
     *
     * The default computeNorm method would cancel out this advantage,  but we don't calculate the norms for these fields
     * because otherwise gets unfair disadvantage when contain multiple terms that are not term being searched, i.e
     * qdur field matching ten releases with different qdurs.
     *
     * Note: would prefer to do this just for the release fields, but the field name is not passed as a parameter but side
     * effect should be minimal because not many valid fields contain term repeated more than twice.
     *
     * @param freq
     * @return score component
     */
    @Override
    public float tf(float freq) {
        if (freq >= 2.0f) {
            return 1.41f; //Same result as if contains same term twice

        } else {
            return super.tf(freq);
        }
    }
}
