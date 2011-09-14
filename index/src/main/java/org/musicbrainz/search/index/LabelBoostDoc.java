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

package org.musicbrainz.search.index;

import org.musicbrainz.search.MbDocument;

import java.util.HashSet;
import java.util.Set;

/**
 * Boost documents that do not have their common name as their label name. For example EMI Records is better known
 * simply as EMI
 *
 */
public class LabelBoostDoc {

    //Double the score of this doc if it comes up in search (note will have no effect if matches on alias field because
    //norms are disabled for aliases.
    private static float DOC_BOOST = 2.0f;

    private static Set<String> labelGuIdSet = new HashSet<String>();

    static  {
        labelGuIdSet.add("022fe361-596c-43a0-8e22-bad712bb9548");   //EMI Records
        labelGuIdSet.add("29d7c88f-5200-4418-a683-5c94ea032e38");   //Bertelsmann Music Group
    }

    public static void boost(String artistGuid, MbDocument doc) {
        if(labelGuIdSet.contains(artistGuid)) {
            doc.getLuceneDocument().setBoost(DOC_BOOST);
        }
    }

}
