/*
 Copyright (c) 2013 Paul Taylor
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

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import org.musicbrainz.mmd2.ReleaseEvent;

import java.util.Comparator;

public class ReleaseEventComparator implements Comparator<ReleaseEvent> {

    private static CharMatcher hyphenMatcher = CharMatcher.anyOf("-");

    /**
     * Date will be one of these formats
     *
     * YYYY
     * YYYY-MM
     * YYYY-MM-dd
     *
     * Or Empty String
     *
     * @param releaseEvent1
     * @param releaseEvent2
     * @return dates ordered earliest first
     */
    public int compare(ReleaseEvent releaseEvent1, ReleaseEvent releaseEvent2) {

        String date1 = padDate(Strings.nullToEmpty(releaseEvent1.getDate()));
        String date2 = padDate(Strings.nullToEmpty(releaseEvent2.getDate()));

        try {
            Integer date1Number = Integer.parseInt(date1);
            Integer date2Number = Integer.parseInt(date2);
            return date1Number.compareTo(date2Number);
        }
        catch(NumberFormatException nfe) {
            return 0;
        }

    }

    private String padDate(String origDate)
    {
        String date = hyphenMatcher.removeFrom(origDate);
        if(date.length()==0) {
            return "99999999";
        }
        if(date.length()==4) {
            return date + "0000";
        }
        else if(date.length()==6) {
            return date + "00";
        }
        else {
            return date;
        }
    }

}
