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

import org.apache.lucene.queryParser.ParseException;
import org.apache.velocity.app.Velocity;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.logging.Logger;

public class SearchServerServlet extends HttpServlet {

    final Logger log = Logger.getLogger(SearchServerServlet.class.getName());

    final static int DEFAULT_OFFSET = 0;
    final static int DEFAULT_LIMIT = 100;

    final static String RESPONSE_XML = "xml";
    final static String RESPONSE_HTML = "html";

    private boolean isVelocityInitialized = false;

    @Override
    public void init() {

        String indexDir = getServletConfig().getInitParameter("index_dir");
        log.info("Index dir = " + indexDir);

        // Setup Velocity and Search server
        setUpVelocity();

        try {
            Velocity.init();
            SearchServerFactory.init(indexDir);
            isVelocityInitialized = true;
        } catch (Exception e1) {
            e1.printStackTrace();
            isVelocityInitialized = false;
        }

    }

    public static void setUpVelocity() {
        Velocity.setProperty("resource.loader", "class");
        Velocity.setProperty("class.resource.loader.class", org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader.class.getName());
        Velocity.setProperty("eventhandler.referenceinsertion.class", "org.apache.velocity.app.event.implement.EscapeHtmlReference");
        //TODO: remove this DummyLogChute hack and add log4j logging
        Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, new DummyLogChute());
    }

    @Override
    public void destroy() {
        SearchServerFactory.close();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check if velocity is initialized
        if (!isVelocityInitialized) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error during Velocity/SearchServer initialization");
            return;
        }

        // Extract parameters from request
        request.setCharacterEncoding("UTF-8");

        String query = request.getParameter("query");
        if (query == null || query.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No query.");
            return;
        }

        String type = request.getParameter("type");
        if (type == null || type.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No type.");
            return;
        }
        ResourceType resourceType = ResourceType.getValue(type);
        if (resourceType == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown resource type");
            return;
        }

        Integer offset = DEFAULT_OFFSET;
        String strOffset = request.getParameter("offset");
        if (strOffset != null && !strOffset.isEmpty()) {
            offset = new Integer(strOffset);
        }

        Integer limit = DEFAULT_LIMIT;
        String strLimit = request.getParameter("limit");
        if (strLimit != null && !strLimit.isEmpty()) {
            limit = new Integer(strLimit);
        }

        String responseFormat = request.getParameter("fmt");
        if (responseFormat == null || responseFormat.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No format supplied");
            return;
        }

        // Make the search
        try {

            SearchServer searchServer = SearchServerFactory.getSearchServer(resourceType);
            Results results = searchServer.search(query, offset, limit);
            ResultsWriter writer = searchServer.getWriter(responseFormat);

            if (writer == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "No handler for resource type " + resourceType + " and format " + responseFormat);
                return;
            }
            response.setCharacterEncoding("UTF-8");
            response.setContentType(writer.getMimeType());
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8")));
            writer.writeFragment(out, results);
            out.close();
        }
        catch (ParseException pe) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to parse search:" + query);
            return;

        }


    }

}
