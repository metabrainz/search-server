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

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.musicbrainz.search.servlet.mmd2.ResultsWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.EnumMap;
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
    private EnumMap<ResourceType, SearchServer> searchers = new EnumMap<ResourceType, SearchServer>(ResourceType.class);
    
    private String initMessage = null;
    private static final String MUSICBRAINZ_SEARCH_WEBPAGE = "http://www.musicbrainz.org/search.html";

    @Override
    public void init() {
        init(false);
    }

    public void init(boolean useMMapDirectory)   {
    	
        String indexDir = getServletConfig().getInitParameter("index_dir");
        log.info("Index dir = " + indexDir);
        if(useMMapDirectory)  {
            log.info("Index Type = MMapped Mode");
        } else {
            log.info("Index Type = NFIO Mode");
        }
        log.info("Max Heap = "+ ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax());

        // Initialize all search servers
        for (ResourceType resourceType : ResourceType.values()) {
    		
    		File indexFileDir = new File(indexDir + System.getProperty("file.separator") + resourceType.getIndexName() + "_index");
        	
    		SearchServer searchServer = null;
			try {
				
				Directory directory = useMMapDirectory ? new MMapDirectory(indexFileDir) : new NIOFSDirectory(indexFileDir);
				IndexSearcher indexSearcher = new IndexSearcher(directory);
				searchServer = resourceType.getSearchServerClass().getConstructor(IndexSearcher.class).newInstance(indexSearcher);
				
			} catch (CorruptIndexException e) {
				log.warning("Could not load " + resourceType.getIndexName() + " index, index is corrupted: " + e.getMessage());
			} catch (IOException e) {
				log.warning("Could not load " + resourceType.getIndexName() + " index: " + e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
			}
    		searchers.put(resourceType, searchServer);
    		
    		if (searchServer == null) continue; 		
    		searchServer.setLastServerUpdatedDate();
    		
		}
        
        isServletInitialized = true;
        
    }

    @Override
    public void destroy() {
    	
    	// Close all search servers
    	for (SearchServer searchServer : searchers.values()) {
    		if (searchServer == null) continue;
            try {
				searchServer.close();
			} catch (IOException e) {
				log.severe("Caught exception during closing of index searcher: " + e.getMessage());
			}
        }
    	searchers.clear();
    }
    
    protected void reloadIndexes() {
    	
    	for (SearchServer searchServer : searchers.values()) {
    		if (searchServer == null) continue;
			try {
				searchServer.reloadIndex();				
			} catch (IOException e) {
				log.severe("Caught exception during reopening of index: " + e.getMessage());
			}
        }
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check if servlet is initialized ok
        if (!isServletInitialized) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorMessage.SERVLET_INIT_FAILED.getMsg(initMessage));
            return;
        }
        // Ensure encoding set to UTF8
        request.setCharacterEncoding(CHARSET);

        // Force initialization of search server, if already open this forces an *expensive* reopen of the indexes
        // reload should be preferred unless you want to use switch between mmap and niofs
        // If specify mmap mode then MMappedDirectory used, should only be used on 64bit JVM or on small indexes
        String init = request.getParameter(RequestParameter.INIT.getName());
        if (init != null) {
            init(init.equals("mmap"));
            return;
        }
        
        // Reopen the indexes in an efficient way
        String reloadIndexes = request.getParameter(RequestParameter.RELOAD_INDEXES.getName());
        if (reloadIndexes != null) {
        	reloadIndexes();
            return;
        }

        // If we receive Count Parameter then we just return a count immediately, the options are the same as for the type
        // parameter
        String count = request.getParameter(RequestParameter.COUNT.getName());
        if(count != null) {
            ResourceType resourceType = ResourceType.getValue(count);
            if(resourceType == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorMessage.UNKNOWN_COUNT_TYPE.getMsg(count));
                return;
            }

            SearchServer searchServerCount = searchers.get(resourceType);
            response.setCharacterEncoding(CHARSET);
            response.setContentType("text/plain; charset=UTF-8; charset=UTF-8");
            response.getOutputStream().println(searchServerCount.getCount());
            response.getOutputStream().close();
            return;
        }

        // If they have entered nothing, redirect to them the Musicbrainz Search Page
        if(request.getParameterMap().size() == 0)
        {
            response.sendRedirect(MUSICBRAINZ_SEARCH_WEBPAGE);
            return;
        }

        // Resource Type always required
        String type = request.getParameter(RequestParameter.TYPE.getName());
        if (type == null || type.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorMessage.NO_TYPE_PARAMETER.getMsg());
            return;
        }

        // V1 Compatability
        if(type.equals("track"))
        {
            type = ResourceType.RECORDING.getName();
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


        // Default to xml if not provided
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
        // Used by webservice
        if (strLimit != null && !strLimit.isEmpty()) {
            limit = new Integer(strLimit);
            if (limit > MAX_MATCHES_LIMIT) {
                limit = MAX_MATCHES_LIMIT;
            }
        }
        // Used by web search (although entered as limit on website then converted to max !)
        // TODO perhaps could be simplified
        else if (strMax != null && !strMax.isEmpty()) {
            limit = new Integer(strMax);
            if (limit > MAX_MATCHES_LIMIT) {
                limit = MAX_MATCHES_LIMIT;
            }
        }


        // Make the search
        try {
            SearchServer searchServer = searchers.get(resourceType);
            if (searchServer == null) {
            	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorMessage.INDEX_NOT_AVAILABLE_FOR_TYPE.getMsg(resourceType));
                return;
            }
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
