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
import java.util.logging.Logger;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

public class SearchServerServlet extends HttpServlet {

    final Logger log = Logger.getLogger(SearchServerServlet.class.getName());

	private SearchServer searchServer;
	private Map<String, Map<String, ResultsWriter>> writers;

	@Override
    public void init() {
		String indexDir = getServletConfig().getInitParameter("index_dir");
		log.info("Index dir = " + indexDir);
		try {
			searchServer = new SearchServer(indexDir);
		}
		catch (IOException e) {
			searchServer = null;
		}
	}

	@Override
	public void destroy() {
		if (searchServer != null)
			searchServer.close();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		if (searchServer == null) {
			response.sendError(500, "searchServer == null");
			return;
		}

		request.setCharacterEncoding("UTF-8");

		String query = request.getParameter("query");
		if (query == null || query.isEmpty()) {
			response.sendError(400, "No query.");
			return;
		}

		String type = request.getParameter("type");
		if (query == null || query.isEmpty()) {
			response.sendError(400, "No type.");
			return;
		}

		int offset = 0;
		int limit = 10;
		Results results = searchServer.search(type, query, offset, limit);

		ResultsWriter writer = null;
		if (type.equals("artist")) {
			writer = new ArtistXmlWriter();
		}
		else if (type.equals("label")) {
			writer = new LabelXmlWriter();
		}
		else if (type.equals("track")) {
			writer = new TrackXmlWriter();
		}
		else if (type.equals("release")) {
			writer = new ReleaseXmlWriter();
		}

		response.setCharacterEncoding("UTF-8");
		response.setContentType(writer.getMimeType());

		PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8")));
		writer.write(out, results);
		out.close();
	}

}
