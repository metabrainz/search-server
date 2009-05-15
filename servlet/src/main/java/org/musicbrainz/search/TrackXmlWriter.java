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

package org.musicbrainz.search;

import java.io.*;

import org.apache.lucene.document.Document;

public class TrackXmlWriter extends XmlWriter {

    public void write(PrintWriter out, Results results) throws IOException {
        writeHeader(out);
        out.write("<track-list count=\"" + results.totalHits + "\" offset=\"" + results.offset + "\">");
        for (Result result : results.results) {
            Document doc = result.doc;

            out.write("<track id=\"");
            Utils.escapeXml(out, doc.get(TrackIndexFieldName.TRACK_ID.getFieldname()));
            out.write('"');
            out.write(" ext:score=\"");
            out.print((int) (result.score * 100));
            out.write("\">");

            String title = doc.get(TrackIndexFieldName.TRACK.getFieldname());
            if (title != null) {
                out.write("<title>");
                Utils.escapeXml(out, title);
                out.write("</title>");
            }

            String duration = doc.get(TrackIndexFieldName.DURATION.getFieldname());
            if (duration != null) {
                out.write("<duration>" + duration + "</duration>");
            }

            String artistName = doc.get(TrackIndexFieldName.ARTIST.getFieldname());
            if (artistName != null) {
                out.write("<artist id=\"");
                Utils.escapeXml(out, doc.get(TrackIndexFieldName.ARTIST_ID.getFieldname()));
                out.write("\"><name>");
                Utils.escapeXml(out, artistName);
                out.write("</name></artist>");
            }

            String releaseName = doc.get(TrackIndexFieldName.RELEASE.getFieldname());
            if (releaseName != null) {
                out.write("<release id=\"");
                Utils.escapeXml(out, doc.get(TrackIndexFieldName.RELEASE_ID.getFieldname()));
                out.write("\"><title>");
                Utils.escapeXml(out, releaseName);
                out.write("</title>");
                out.write("<track-list offset=\"");
                out.write(doc.get(TrackIndexFieldName.TRACKNUM.getFieldname()));
                out.write("\"/></release>");
            }

            out.write("</track>");
        }
        out.write("</track-list>");
        writeFooter(out);
    }

}
