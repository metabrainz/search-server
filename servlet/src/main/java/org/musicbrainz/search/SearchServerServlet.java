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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.EnumMap;
import java.util.logging.Logger;

import org.apache.velocity.app.Velocity;
import org.apache.lucene.queryParser.ParseException;

public class SearchServerServlet extends HttpServlet {

    final Logger log = Logger.getLogger(SearchServerServlet.class.getName());

    final static int DEFAULT_OFFSET = 0;
    final static int DEFAULT_LIMIT = 100;

    final static String RESPONSE_XML = "xml";
    final static String RESPONSE_HTML = "html";

    private SearchServer searchServer;
    private boolean isVelocityInitialized = false;
    private EnumMap<ResourceType, ResultsWriter> xmlWriters  = new EnumMap<ResourceType, ResultsWriter>(ResourceType.class);
    private EnumMap<ResourceType, ResultsWriter> htmlWriters = new EnumMap<ResourceType, ResultsWriter>(ResourceType.class);
    private EnumMap<ResourceType, QueryMangler> queryManglers = new EnumMap<ResourceType, QueryMangler>(ResourceType.class);

    @Override
    public void init() {

        // Setup search server
        String indexDir = getServletConfig().getInitParameter("index_dir");
        log.info("Index dir = " + indexDir);
        try {
            searchServer = new SearchServer(indexDir);
        }
        catch (IOException e) {
            searchServer = null;
        }

        //Map resourcetype to XML writer, writer can be reused
        xmlWriters.put(ResourceType.ARTIST, new ArtistXmlWriter());
        xmlWriters.put(ResourceType.LABEL, new LabelXmlWriter());
        xmlWriters.put(ResourceType.RELEASE, new ReleaseXmlWriter());
        xmlWriters.put(ResourceType.RELEASE_GROUP, new ReleaseGroupXmlWriter());
        xmlWriters.put(ResourceType.TRACK, new TrackXmlWriter());

        // Setup Velocity and the HTML writers
        setUpVelocity();

        try {
            Velocity.init();
            htmlWriters.put(ResourceType.FREEDB, new FreeDBHtmlWriter());
            htmlWriters.put(ResourceType.ARTIST, new ArtistHtmlWriter());
            isVelocityInitialized = true;
        } catch (Exception e1) {
            e1.printStackTrace();
            isVelocityInitialized = false;
        }

        //Setup query manglers
        queryManglers.put(ResourceType.ARTIST, new ArtistMangler());
        queryManglers.put(ResourceType.LABEL, new LabelMangler());
        queryManglers.put(ResourceType.RELEASE, new ReleaseMangler());
        queryManglers.put(ResourceType.RELEASE_GROUP, new ReleaseGroupMangler());
        queryManglers.put(ResourceType.TRACK, new TrackMangler());


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
        if (searchServer != null) {
            searchServer.close();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check if search server is available
        if (searchServer == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "searchServer == null");
            return;
        }

        // Check if velocity is initialized
        if (!isVelocityInitialized) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error during Velocity initialization");
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
            //Make any mods we need
            QueryMangler qm = queryManglers.get(resourceType);
            if(qm!=null)
            {
                query=qm.mangleQuery(query);
            }
            Results results = searchServer.search(resourceType, query, offset, limit);

            // Select the matching writer
            ResultsWriter writer;
            if (RESPONSE_XML.equals(responseFormat)) {
                writer = xmlWriters.get(resourceType);
            } else {
                writer = htmlWriters.get(resourceType);
            }

            if (writer == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "No handler for resource type " + resourceType + " and format " + responseFormat);
                return;
            }

            // Output the response
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
