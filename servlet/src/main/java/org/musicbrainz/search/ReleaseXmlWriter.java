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
import org.apache.commons.lang.StringUtils;

public class ReleaseXmlWriter extends XmlWriter {

    public void write(PrintWriter out, Results results) throws IOException {
        writeHeader(out);
        out.write("<release-list count=\"" + results.totalHits + "\" offset=\"" + results.offset + "\">");
        for (Result result : results.results) {
            Document doc = result.doc;

            out.write("<release id=\"");
            Utils.escapeXml(out, doc.get(ReleaseIndexFieldName.RELEASE_ID.getFieldname()));
            out.write('"');
//				String artype = doc.get("artype");
//				if (artype != null) {
//					out.write(" type=\"");
//					Utils.escapeXml(out, StringUtils.capitalize(artype));
//					out.write('"');
//				}
            out.write(" ext:score=\"");
            out.print((int) (result.score * 100));
            out.write("\">");

            String title = doc.get(ReleaseIndexFieldName.RELEASE.getFieldname());
            if (title != null) {
                out.write("<title>");
                Utils.escapeXml(out, title);
                out.write("</title>");
            }

            String asin = doc.get(ReleaseIndexFieldName.AMAZON_ID.getFieldname());
            if (asin != null) {
                out.write("<asin>");
                Utils.escapeXml(out, asin);
                out.write("</asin>");
            }

            String[] countries = doc.getValues(ReleaseIndexFieldName.COUNTRY.getFieldname());
            String[] dates = doc.getValues(ReleaseIndexFieldName.DATE.getFieldname());
            String[] labels = doc.getValues(ReleaseIndexFieldName.LABEL.getFieldname());
            String[] catnos = doc.getValues(ReleaseIndexFieldName.CATALOG_NO.getFieldname());
            String[] barcodes = doc.getValues(ReleaseIndexFieldName.BARCODE.getFieldname());
            if (countries.length > 0) {
                out.write("<release-event-list>");
                for (int i = 0; i < countries.length; i++) {
                    out.write("<event");
                    if (!dates[i].equals("-")) {
                        out.write(" date=\"" + dates[i] + "\"");
                    }
                    if (!countries[i].equals("-")) {
                        out.write(" country=\"" + StringUtils.upperCase(countries[i]) + "\"");
                    }
                    if (!labels[i].equals("-")) {
                        out.write(" label=\"");
                        Utils.escapeXml(out, labels[i]);
                        out.write("\"");
                    }
                    if (!catnos[i].equals("-")) {
                        out.write(" catalog-number=\"");
                        Utils.escapeXml(out, catnos[i]);
                        out.write("\"");
                    }
                    if (!barcodes[i].equals("-")) {
                        out.write(" barcode=\"");
                        Utils.escapeXml(out, barcodes[i]);
                        out.write("\"");
                    }
                    out.write("/>");
                }
                out.write("</release-event-list>");
            }

            String artistName = doc.get(ReleaseIndexFieldName.ARTIST.getFieldname());
            if (artistName != null) {
                out.write("<artist id=\"");
                Utils.escapeXml(out, doc.get(ReleaseIndexFieldName.ARTIST_ID.getFieldname()));
                out.write("\"><name>");
                Utils.escapeXml(out, artistName);
                out.write("</name></artist>");
            }

            String discids = doc.get(ReleaseIndexFieldName.NUM_DISC_IDS.getFieldname());
            if (discids != null) {
                out.write("<disc-list count=\"");
                Utils.escapeXml(out, discids);
                out.write("\"/>");
            }

            String tracks = doc.get(ReleaseIndexFieldName.NUM_TRACKS.getFieldname());
            if (tracks != null) {
                out.write("<track-list count=\"");
                Utils.escapeXml(out, tracks);
                out.write("\"/>");
            }

            out.write("</release>");
        }
        out.write("</release-list>");
        writeFooter(out);
    }

}
