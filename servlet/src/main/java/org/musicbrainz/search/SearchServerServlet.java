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
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.EnumMap;

public class SearchServerServlet extends HttpServlet {

    final Logger log = Logger.getLogger(SearchServerServlet.class.getName());

    final static int DEFAULT_OFFSET = 0;
    final static int DEFAULT_MATCHES_LIMIT = 25;
    final static int MAX_MATCHES_LIMIT = 100;


    final static String RESPONSE_XML = "xml";
    final static String RESPONSE_HTML = "html";

    final static String CHARSET = "UTF-8";

    private boolean isServletInitialized = false;

    private String initMessage = null;

    @Override
    public void init() {

        String indexDir = getServletConfig().getInitParameter("index_dir");
        log.info("Index dir = " + indexDir);

        // Setup Velocity and Search server
        setUpVelocity();

        try {
            Velocity.init();
            SearchServerFactory.init(indexDir);
            isServletInitialized = true;
        } catch (Exception e1) {
            initMessage = e1.getMessage();
            e1.printStackTrace(System.out);
            isServletInitialized = false;
        }

    }

    public static void setUpVelocity() {
        Velocity.setProperty("resource.loader", "class");
        Velocity.setProperty("class.resource.loader.class", org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader.class.getName());
        Velocity.setProperty("eventhandler.referenceinsertion.class", "org.apache.velocity.app.event.implement.EscapeHtmlReference");
        Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, new VelocityLogChute());
    }

    @Override
    public void destroy() {
        SearchServerFactory.close();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check if servlet is initialized ok
        if (!isServletInitialized) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorMessage.SERVLET_INIT_FAILED.getMsg(initMessage));
            return;
        }

        // Extract parameters from request
        request.setCharacterEncoding(CHARSET);
        String query = request.getParameter(RequestParameter.QUERY.getName());
        if (query == null || query.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorMessage.NO_QUERY_PARAMETER.getMsg());
            return;
        }

        String type = request.getParameter(RequestParameter.TYPE.getName());
        if (type == null || type.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorMessage.NO_TYPE_PARAMETER.getMsg());
            return;
        }
        ResourceType resourceType = ResourceType.getValue(type);
        if (resourceType == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorMessage.UNKNOWN_RESOURCE_TYPE.getMsg(type));
            return;
        }

        String responseFormat = request.getParameter(RequestParameter.FORMAT.getName());
        if (responseFormat == null || responseFormat.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorMessage.NO_FORMAT_PARAMETER.getMsg());
            return;
        }

        Integer offset = DEFAULT_OFFSET;
        String strOffset = request.getParameter(RequestParameter.OFFSET.getName());
        if (strOffset != null && !strOffset.isEmpty()) {
            offset = new Integer(strOffset);
        }

        Integer limit = DEFAULT_MATCHES_LIMIT;
        String strLimit = request.getParameter(RequestParameter.LIMIT.getName());
        String strMax = request.getParameter(RequestParameter.MAX.getName());
        //used by webservice
        if (strLimit != null && !strLimit.isEmpty()) {
            limit = new Integer(strLimit);
            if (limit > MAX_MATCHES_LIMIT) {
                limit = MAX_MATCHES_LIMIT;
            }
        }
        //used by web search (although entered as limit on website then converted to max !)
        //TODO perhaps could be simplified
        else if (strMax != null && !strMax.isEmpty()) {
            limit = new Integer(strMax);
            if (limit > MAX_MATCHES_LIMIT) {
                limit = MAX_MATCHES_LIMIT;
            }
        }

        EnumMap<RequestParameter, String> extraInfoMap = new EnumMap<RequestParameter, String>(RequestParameter.class);
        switch (resourceType) {
            case RELEASE: {
                String tport = request.getParameter(RequestParameter.TAGGER_PORT.getName());
                String rel = request.getParameter(RequestParameter.RELATIONSHIPS.getName());
                if (tport != null) {
                    extraInfoMap.put(RequestParameter.TAGGER_PORT, tport);
                    String duration = request.getParameter(RequestParameter.DURATION.getName());
                    if (duration != null) {
                        extraInfoMap.put(RequestParameter.DURATION, duration);
                    }
                } else if (rel != null) {
                    extraInfoMap.put(RequestParameter.RELATIONSHIPS, rel);
                }
            }
            break;

            case TRACK: {
                String tport = request.getParameter(RequestParameter.TAGGER_PORT.getName());
                String rel = request.getParameter(RequestParameter.RELATIONSHIPS.getName());
                String oldLink = request.getParameter(RequestParameter.OLD_STYLE_LINK.getName());

                if (tport != null) {
                    extraInfoMap.put(RequestParameter.TAGGER_PORT, tport);
                    String duration = request.getParameter(RequestParameter.DURATION.getName());
                    if (duration != null) {
                        extraInfoMap.put(RequestParameter.DURATION, duration);
                    }
                } else if (oldLink != null) {
                    extraInfoMap.put(RequestParameter.OLD_STYLE_LINK, oldLink);
                } else if (rel != null) {
                    extraInfoMap.put(RequestParameter.RELATIONSHIPS, rel);
                }
            }
            break;

            case ARTIST: {
                String rel = request.getParameter(RequestParameter.RELATIONSHIPS.getName());
                if (rel != null) {
                    extraInfoMap.put(RequestParameter.RELATIONSHIPS, rel);
                }
            }
            break;
        }

        // Make the search
        try {

            SearchServer searchServer = SearchServerFactory.getSearchServer(resourceType);
            Results results = searchServer.search(query, offset, limit);
            ResultsWriter writer = searchServer.getWriter(responseFormat);

            if (writer == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorMessage.NO_HANDLER_FOR_TYPE_AND_FORMAT.getMsg(resourceType, responseFormat));
                return;
            }
            response.setCharacterEncoding(CHARSET);
            response.setContentType(writer.getMimeType());
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), CHARSET)));
            writer.write(out, results, extraInfoMap);
            out.close();
        }
        catch (ParseException pe) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorMessage.UNABLE_TO_PARSE_SEARCH.getMsg(query));
            return;

        }


    }

}
