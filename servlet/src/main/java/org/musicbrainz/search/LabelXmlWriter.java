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

public class LabelXmlWriter extends XmlWriter {

	public void write(PrintWriter out, Results results) throws IOException {
		writeHeader(out);
		out.write("<label-list count=\"" + results.totalHits + "\" offset=\"" + results.offset + "\">");
		for (Result result: results.results) {
				Document doc = result.doc;

				out.write("<label id=\"");
				Utils.escapeXml(out, doc.get("arid"));
				out.write('"');
				String artype = doc.get("artype");
				if (artype != null) {
					out.write(" type=\"");
					Utils.escapeXml(out, StringUtils.capitalize(artype));
					out.write('"');
				}
				out.write(" ext:score=\"");
				out.print((int)(result.score * 100));
				out.write("\">");

				String name = doc.get("label");
				if (name != null) {
					out.write("<name>");
					Utils.escapeXml(out, name);
					out.write("</name>");
				}

				String sortname = doc.get("sortname");
				if (sortname != null) {
					out.write("<sort-name>");
					Utils.escapeXml(out, sortname);
					out.write("</sort-name>");
				}

				String begin = doc.get("begin");
				String end = doc.get("end");
				if (begin != null || end != null) {
					out.write("<life-span");
					if (begin != null)
						out.write(" begin=\"" + begin + "\"");
					if (end != null)
						out.write(" end=\"" + end + "\"");
					out.write("/>");
				}

				String comment = doc.get("comment");
				if (comment != null) {
					out.write("<disambiguation>");
					Utils.escapeXml(out, comment);
					out.write("</disambiguation>");
				}

				out.write("</label>");
		}
		out.write("</label-list>");
		writeFooter(out);
	}

}
