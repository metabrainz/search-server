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

import org.musicbrainz.search.index.Index;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

public abstract class ResultsWriter {

    protected Date serverLastUpdatedDate;

    public abstract String getMimeType();

    public void write(PrintWriter out, Results results) throws IOException {
        write(out, results, SearchServerServlet.RESPONSE_XML, false) ;
    }

    public void write(PrintWriter out, Results results, String outputFormat) throws IOException {
        write(out, results, outputFormat, false) ;
    }

    public abstract void write(PrintWriter out, Results results, String outputFormat, boolean isPretty) throws IOException ;

    /**
     *
     * @param value
     * @return true if the value is not null and is a real value rather than set to unknown
     */
    protected boolean isNotUnknown(String value) {
        return ((value != null) && !(value.equalsIgnoreCase(Index.UNKNOWN)));
    }

    /**
     *
     * @param value
     * @return true if the value is not null and is a real value rather than set to no value
     */
    protected boolean isNotNoValue(String value) {
        return ((value != null) && !(value.equalsIgnoreCase(Index.NO_VALUE)));
    }

    public void setLastServerUpdatedDate(Date date)
    {
        this.serverLastUpdatedDate=date;
    }
}
