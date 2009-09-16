/* Copyright (c) 2009 Lukas Lalinsky
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

package org.musicbrainz.search.servlet;

import java.io.IOException;
import java.io.PrintWriter;

/** Html helper methods for the html output
 *
 */
public class Utils {

    public static void escapeXml(PrintWriter writer, String str) throws IOException {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            switch (c) {
                case '&':
                    writer.write("&amp;");
                    break;
                case '<':
                    writer.write("&lt;");
                    break;
                case '>':
                    writer.write("&gt;");
                    break;
                default:
                    writer.write(c);
                    break;
            }
        }
    }

    /**
     * Convert milliseconds to mm:ss display format
     *
     * Note we don't break into hours, 3900000 ms would display as 65:00 , and we dont put a zero
     * before single digit minutes
     *
     * @param ms
     * @return
     */
    public static String convertMsToMinutesAndSeconds(long ms) {
        int seconds = (int)((ms /1000) % 60);
        int minutes = (int)(ms / 60000) ;
        String secondsStr = (seconds<10 ? "0" : "")+ seconds;
        return new String(minutes + ":" + secondsStr );
    }

    /** Calculate tlen class to use when comparing tracks to a specific track in track.html
     *
     */
    public static String getTlenClassForDuration(long dur,long trackDur) {
        long diff = Math.abs(dur - trackDur);
        if( diff < 5000) return "tlen good";
        if( diff < 15000) return "tlen ok";
        return "tlen bad";
    }
}
