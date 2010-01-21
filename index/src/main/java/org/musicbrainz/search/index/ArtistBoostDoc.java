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
 * Boost documents that do not have their common name as their artist name. This is a common problem for classical
 * composers who are often just known by their last name but have their full name stored in the artist field meaning
 * that a search for them by just their last name often favours other artist who have the name lastname but none/less
 * firstnames. 
 */
public class ArtistBoostDoc {

    //Double the score of this doc if it comes up in search (note will have no effect if matches on alias field because
    //norms are disabled for aliases.
    private static float ARTIST_DOC_BOOST = 2.0f;

    private static Set<String> artistGuIdSet = new HashSet<String>();

    static  {

        artistGuIdSet.add("24f1766e-9635-4d58-a4d4-9413f9f98a4c");   //Bach
        artistGuIdSet.add("1f9df192-a621-4f54-8850-2c5373b7eac9");   //Beethoven
        artistGuIdSet.add("b972f589-fb0e-474e-b64a-803b0364fa75");   //Mozart
        artistGuIdSet.add("ad79836d-9849-44df-8789-180bbc823f3c");   //Vivaldi
        artistGuIdSet.add("27870d47-bb98-42d1-bf2b-c7e972e6befc");   //Handel
        artistGuIdSet.add("8255db36-4902-4cf6-8612-0f2b4288bc9a");   //Johann Strauss II
        artistGuIdSet.add("eefd7c1e-abcf-4ccc-ba60-0fd435c9061f");   //Richard Wagner
        artistGuIdSet.add("4e60a56a-514a-4a19-a3cc-49927c96b3cb");   //Sir Edward Elgar
        artistGuIdSet.add("c130b0fb-5dce-449d-9f40-1437f889f7fe");   //Joseph Haydn
        artistGuIdSet.add("f91e3a88-24ee-4563-8963-fab73d2765ed");   //Franz Schubert
        artistGuIdSet.add("c70d12a2-24fe-4f83-a6e6-57d84f8efb51");   //Johannes Brahms
        artistGuIdSet.add("f1bedf1f-4445-4651-9c35-f4a3f3860a13");   //Guiseppe Verdi
    }

    public static void boost(String artistGuid, MbDocument doc) {
        if(artistGuIdSet.contains(artistGuid)) {
            doc.getLuceneDocument().setBoost(ARTIST_DOC_BOOST);
        }
    }

}
