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

import org.apache.lucene.queryParser.ParseException;
import org.musicbrainz.search.servlet.mmd2.ResultsWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.logging.Logger;

public class SearchServerServlet extends HttpServlet {

    final Logger log = Logger.getLogger(SearchServerServlet.class.getName());

    final static int DEFAULT_OFFSET = 0;
    final static int DEFAULT_MATCHES_LIMIT = 25;
    final static int MAX_MATCHES_LIMIT = 100;


    public final static String RESPONSE_XML    = "xml";
    public final static String RESPONSE_JSON   = "json";

    final static String WS_VERSION_1 = "1";
    final static String WS_VERSION_2 = "2";

    final static String CHARSET = "UTF-8";

    private boolean isServletInitialized = false;

    private String initMessage = null;

    @Override
    public void init() {

        String indexDir = getServletConfig().getInitParameter("index_dir");
        log.info("Index dir = " + indexDir);
        log.info("Max Heap = "+ ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax());

        try {
            SearchServerFactory.init(indexDir);
            isServletInitialized = true;
        } catch (Exception e1) {
            initMessage = e1.getMessage();
            e1.printStackTrace(System.out);
            isServletInitialized = false;
        }

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
        //Ensure encoding set to UTF8
        request.setCharacterEncoding(CHARSET);

        //If we receive Count Parameter then we just return a count immediately, the options are the same as for the type
        //parameter
        String count = request.getParameter(RequestParameter.COUNT.getName());
        if(count!=null) {
            ResourceType resourceType = ResourceType.getValue(count);
            if(resourceType==null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorMessage.UNKNOWN_COUNT_TYPE.getMsg(count));
                return;
            }

            SearchServer searchServerCount = SearchServerFactory.getSearchServer(resourceType);
            response.setCharacterEncoding(CHARSET);
            response.setContentType("text/plain; charset=UTF-8; charset=UTF-8");
            response.getOutputStream().println(searchServerCount.getCount());
            response.getOutputStream().close();
            return;
        }

        //Resource Type always required
        String type = request.getParameter(RequestParameter.TYPE.getName());
        if (type == null || type.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorMessage.NO_TYPE_PARAMETER.getMsg());
            return;
        }

        //V1 Compatability
        if(type.equals("track"))
        {
            type=ResourceType.RECORDING.getName();
        }
        //TEMPORARY FIX mb_server uses release_group instead of release-group
        else if (type.equals("release_group"))
        {
            type=ResourceType.RELEASE_GROUP.getName();
        }
        ResourceType resourceType = ResourceType.getValue(type);
        if (resourceType == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorMessage.UNKNOWN_RESOURCE_TYPE.getMsg(type));
            return;
        }

        String query = request.getParameter(RequestParameter.QUERY.getName());
        if (query == null || query.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorMessage.NO_QUERY_PARAMETER.getMsg());
            return;
        }



        //Default to xml if not provided
        String responseFormat = request.getParameter(RequestParameter.FORMAT.getName());
        if (responseFormat == null || responseFormat.isEmpty()) {
            responseFormat = RESPONSE_XML;
        }

        String responseVersion = request.getParameter(RequestParameter.VERSION.getName());
        if (responseVersion == null || responseVersion.isEmpty()) {
            responseVersion = WS_VERSION_2;
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


        // Make the search
        try {

            SearchServer searchServer = SearchServerFactory.getSearchServer(resourceType);
            Results results = searchServer.search(query, offset, limit);
            org.musicbrainz.search.servlet.ResultsWriter writer = searchServer.getWriter(responseFormat, responseVersion);

            if (writer == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorMessage.NO_HANDLER_FOR_TYPE_AND_FORMAT.getMsg(resourceType, responseFormat));
                return;
            }
            response.setCharacterEncoding(CHARSET);
            if(responseFormat.equals(RESPONSE_XML)) {
                response.setContentType(writer.getMimeType());
            }
            else {
                response.setContentType(((ResultsWriter)writer).getJsonMimeType());
            }

            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), CHARSET)));
            writer.write(out, results,responseFormat);
            out.close();

        }
        catch (ParseException pe) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorMessage.UNABLE_TO_PARSE_SEARCH.getMsg(query));
            return;

        }


    }

}
