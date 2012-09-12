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
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.musicbrainz.search.servlet.mmd2.AllWriter;
import org.musicbrainz.search.servlet.mmd2.ResultsWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SearchServerServlet extends HttpServlet {

    final Logger log = Logger.getLogger(SearchServerServlet.class.getName());

    final static int DEFAULT_OFFSET = 0;
    final static int DEFAULT_MATCHES_LIMIT = 25;
    final static int MAX_MATCHES_LIMIT = 100;

    public final static String RESPONSE_XML         = "xml";
    public final static String RESPONSE_JSON        = "json";
    public final static String RESPONSE_JSON_NEW    = "jsonnew";

    final static String WS_VERSION_1 = "1";
    final static String WS_VERSION_2 = "2";

    final static String CHARSET = "UTF-8";

    final static String TYPE_ALL = "all";
    final static String TYPE_TRACK = "track";

    private boolean isServletInitialized = false;

    //Enabled as long indexes for all resources are available
    private boolean isSearchAllEnabled = true;

    //When doing search over multiple indexes use this executorservice to run in parallel
    private ExecutorService es = Executors.newCachedThreadPool();

    private EnumMap<ResourceType, SearchServer> searchers       = new EnumMap<ResourceType, SearchServer>(ResourceType.class);
    private EnumMap<ResourceType, SearchServer> dismaxSearchers = new EnumMap<ResourceType, SearchServer>(ResourceType.class);


    private String initMessage = null;
    private static String searchWebPage = "";
    private static boolean isRateLimiterEnabled = false;
    private static boolean isAdminRemoteEnabled = false;

    @Override
    public void init() {
        init(true);
    }

    /**
     * If you have indexes that are available this reads from the new indexes and closes the existing readers
     *
     * @param useMMapDirectory
     */
    public void init(boolean useMMapDirectory) {

        searchWebPage=getServletConfig().getInitParameter("search_webpage");

        String isAdminRemote=getServletConfig().getInitParameter("remoteadmin_enabled");
        isAdminRemoteEnabled = Boolean.parseBoolean(isAdminRemote);


        String rateLimiterEnabled = getServletConfig().getInitParameter("ratelimitserver_enabled");
        initRateLimiter(rateLimiterEnabled);

        String indexDir = getServletConfig().getInitParameter("index_dir");
        if (useMMapDirectory) {
            log.info("Start:Loading Indexes from " + indexDir + ",Type:mmap," + "MaxHeap:" + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax());
        } else {
            log.info("Start:loading Indexes from " + indexDir + ",Type:nfio," + "MaxHeap:" + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax());
        }

        // Initialize all search servers
        for (ResourceType resourceType : ResourceType.values()) {

            File indexFileDir = new File(indexDir + System.getProperty("file.separator") + resourceType.getIndexName() + "_index");

            SearchServer searchServer = null;
            SearchServer dismaxSearchServer = null;

            try {
                Directory directory = useMMapDirectory ? new MMapDirectory(indexFileDir) : new NIOFSDirectory(indexFileDir);
                IndexSearcher indexSearcher = new IndexSearcher(IndexReader.open(directory));
                searchServer = resourceType.getSearchServerClass().getConstructor(IndexSearcher.class).newInstance(indexSearcher);
                dismaxSearchServer = resourceType.getDismaxSearchServerClass().getConstructor(IndexSearcher.class).newInstance(indexSearcher);

            } catch (CorruptIndexException e) {
                log.warning("Could not load " + resourceType.getIndexName() + " index, index is corrupted: " + e.getMessage());
                if(resourceType.isUsedBySearchAll()) {
                    isSearchAllEnabled=false;
                }
            } catch (IOException e) {
                log.warning("Could not load " + resourceType.getIndexName() + " index: " + e.getMessage());
                if(resourceType.isUsedBySearchAll()) {
                    isSearchAllEnabled=false;
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "Could not load " + resourceType.getIndexName() + " index: " + e.getMessage(), e);
                if(resourceType.isUsedBySearchAll()) {
                    isSearchAllEnabled=false;
                }
            }

            //Close old search server, incRef/decRef counting will ensure not closed until no longer in use.
            SearchServer oldSearchServer = searchers.get(resourceType);
            if (oldSearchServer != null) {
                try {
                    oldSearchServer.close();
                } catch (IOException e) {
                    log.severe("Caught exception during closing of index searcher within Init: "
                            + resourceType.getIndexName() + ":" + e.getMessage());
                }
            }

            //Add in new search server and set last updated date
            searchers.put(resourceType, searchServer);
            dismaxSearchers.put(resourceType, dismaxSearchServer);

            if (searchServer != null) {
                searchServer.setLastServerUpdatedDate();
            }
        }
        log.info("End:loaded Indexes from " + indexDir + ",Type:nfio," + "MaxHeap:" + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax());
        isServletInitialized = true;

    }

    @Override
    public void destroy() {

        log.info("Start:Destroy Indexes");

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
        log.info("End:Destroy Indexes");

    }

    /**
     * Init Rate Limiter
     */
    private void initRateLimiter(String rateLimiterEnabled) {
        String rateLimiterHost = getServletConfig().getInitParameter("ratelimitserver_host");
        String rateLimiterPort = getServletConfig().getInitParameter("ratelimitserver_port");
        log.info("RateLimiter:" + rateLimiterEnabled + ":RateLimiterHost:" + rateLimiterHost + ":Port:" + rateLimiterPort);
        isRateLimiterEnabled = Boolean.parseBoolean(rateLimiterEnabled);
        if (isRateLimiterEnabled) {
            RateLimiterChecker.init(rateLimiterHost, rateLimiterPort);
        }
    }

    /**
     * If Index has just been updated (Documents added or removed from existing index) you can use this
     * method to read the latest documents from the index.
     */
    protected void reloadIndexes() {

        log.info("Start:Reloading Indexes");
        for (SearchServer searchServer : searchers.values()) {
            if (searchServer == null) continue;
            try {
                searchServer.reloadIndex();
            } catch (IOException e) {
                log.severe("Caught exception during reopening of index: " + e.getMessage());
            }
        }
        log.info("End:Reloading Indexes");

    }


    /**
     * Ensures that admin requests are coming direct from local machine
     *
     * @param request
     * @return
     */
    private boolean isRequestFromLocalHost(HttpServletRequest request) {

        if (isAdminRemoteEnabled ||
            (request.getRemoteAddr().equals("127.0.0.1")) ||
            (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1"))
            ) {
            log.info("isRequestFromLocalHost:VALID:" + request.getRemoteHost() + "/" + request.getRemoteAddr());
            return true;
        }
        log.info("isRequestFromLocalHost:INVALID:" + request.getRemoteHost() + "/" + request.getRemoteAddr());
        return false;
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

        // Force initialization of search server should be called when index have been replaced by new indexes
        String init = request.getParameter(RequestParameter.INIT.getName());
        if (init != null) {
            log.info("Checking init request");
            if (isRequestFromLocalHost(request)) {
                init(init.equals("mmap"));
                response.setCharacterEncoding(CHARSET);
                response.setContentType("text/plain; charset=UTF-8; charset=UTF-8");
                response.getWriter().println("Indexes Loaded:");
                response.getWriter().close();
                return;
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

        }


        // Enabled/Disable Rate Limiter
        String rate = request.getParameter(RequestParameter.RATE.getName());
        if (rate != null) {
            log.info("Checking rate request");
            if (isRequestFromLocalHost(request)) {
                initRateLimiter(rate);
                response.setCharacterEncoding(CHARSET);
                response.setContentType("text/plain; charset=UTF-8; charset=UTF-8");
                response.getWriter().println("Rate Limiter:" + rate);
                response.getWriter().close();
                return;
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        // Reopen the indexes in an efficient way when existing indexes have been updated (not replaced)
        String reloadIndexes = request.getParameter(RequestParameter.RELOAD_INDEXES.getName());
        if (reloadIndexes != null) {
            log.info("Checking reloadindex request");
            if (isRequestFromLocalHost(request)) {
                reloadIndexes();
                response.setCharacterEncoding(CHARSET);
                response.setContentType("text/plain; charset=UTF-8; charset=UTF-8");
                response.getWriter().println("Indexes Reloaded");
                response.getWriter().close();
                return;
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        // If we receive Count Parameter then we just return a count immediately, the options are the same as for the type
        // parameter
        String count = request.getParameter(RequestParameter.COUNT.getName());
        if (count != null) {
            log.info("Checking count request");
            ResourceType resourceType = ResourceType.getValue(count);
            if (resourceType == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorMessage.UNKNOWN_COUNT_TYPE.getMsg(count));
                return;
            }

            SearchServer searchServerCount = searchers.get(resourceType);
            response.setCharacterEncoding(CHARSET);
            response.setContentType("text/plain; charset=UTF-8; charset=UTF-8");
            response.getWriter().println(searchServerCount.getCount());
            response.getWriter().close();
            return;
        }

        // If they have entered nothing, redirect to them the Musicbrainz Search Page
        if (request.getParameterMap().size() == 0) {
            response.sendRedirect(searchWebPage);
            return;
        }

        //Must be a type parameter and must be type ALL or map to a valid resource type
        String type = request.getParameter(RequestParameter.TYPE.getName());
        if(type==null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorMessage.UNKNOWN_RESOURCE_TYPE.getMsg("none"));
        }

        // V1 Compatibility
        if (type.equals(TYPE_TRACK)) {
            type = ResourceType.RECORDING.getName();
        }

        ResourceType resourceType = null;
        if (!type.equalsIgnoreCase(TYPE_ALL)) {
            resourceType = ResourceType.getValue(type);
            if (resourceType == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorMessage.UNKNOWN_RESOURCE_TYPE.getMsg(type));
                return;
            }
        }
        else if(!isSearchAllEnabled) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorMessage.INDEX_NOT_AVAILABLE_FOR_TYPE.getMsg(TYPE_ALL));
        }

        if (isRateLimiterEnabled) {
            RateLimiterChecker.RateLimiterResponse rateLimiterResponse = RateLimiterChecker.checkRateLimiter(request);
            if (!rateLimiterResponse.isValid()) {
                if (rateLimiterResponse.getHeaderMsg() != null) {
                    response.setHeader(RateLimiterChecker.HEADER_RATE_LIMITED, rateLimiterResponse.getHeaderMsg());
                }
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, rateLimiterResponse.getMsg());
                return;
            }
        }

        String query = request.getParameter(RequestParameter.QUERY.getName());
        if (query == null || query.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorMessage.NO_QUERY_PARAMETER.getMsg());
            return;
        }


        //Response Format, first defined by fmt parameter, if not set defined by accept header, if not set default
        //to Xml. Note if accept header set to json this will set format to  RESPONSE_JSON_NEW not RESPONSE_JSON (the
        //old internal format)
        String responseFormat = request.getParameter(RequestParameter.FORMAT.getName());
        if (responseFormat == null || responseFormat.isEmpty()) {

            Enumeration<String> headers = request.getHeaders("Accept");
            while(headers.hasMoreElements())
            {
                String nextHeader = headers.nextElement();
                if(nextHeader.equals("application/json"))
                {
                    responseFormat = RESPONSE_JSON_NEW;
                    break;
                }
            }
            // Default to xml if not provided
            if(responseFormat == null)
            {
                responseFormat = RESPONSE_XML;
            }
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

        boolean isExplain = false;
        String strIsExplain = request.getParameter(RequestParameter.EXPLAIN.getName());
        if (strIsExplain!= null && strIsExplain.equals("true")) {
            isExplain=true;
        }

        boolean isPretty = false;
        String strIsPretty = request.getParameter(RequestParameter.PRETTY.getName());
        if (strIsPretty!= null && strIsPretty.equals("true")) {
            isPretty=true;
        }


        boolean isDismax = false;
        String strIsDismax = request.getParameter(RequestParameter.DISMAX.getName());
        if (strIsDismax!= null && strIsDismax.equals("true")) {
            isDismax=true;
        }

        try {
            if (resourceType != null) {
                //log.log(Level.SEVERE,"Query sent"+query);
                doSearch(response, resourceType, query, isDismax, isExplain, isPretty, offset, limit, responseFormat, responseVersion);
            } else {
                doAllSearch(response, query, isDismax, offset, limit, responseFormat, isPretty);
            }
        } catch (ParseException pe) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorMessage.UNABLE_TO_PARSE_SEARCH.getMsg(query));
            return;
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }
    }

    /**
     * Normal Search over one index
     *
     *
     * @param response
     * @param resourceType
     * @param query
     * @param isDismax
     * @param isPretty
     * @param offset
     * @param limit
     * @param responseFormat
     * @param responseVersion     @throws ParseException
     * @throws IOException
     */
    private void doSearch(HttpServletResponse response,
                          ResourceType resourceType,
                          String query,
                          boolean isDismax,
                          boolean isExplain,
                          boolean isPretty,
                          Integer offset,
                          Integer limit,
                          String responseFormat,
                          String responseVersion)
            throws ParseException, IOException {

        SearchServer searchServer;
        if(isDismax) {
            searchServer=dismaxSearchers.get(resourceType);
        }
        else {
            searchServer=searchers.get(resourceType);
        }

        if (searchServer == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorMessage.INDEX_NOT_AVAILABLE_FOR_TYPE.getMsg(resourceType));
            return;
        }
        
        if(isExplain) {
            String explainationOutput = searchServer.explain(query, offset, limit);
            response.setCharacterEncoding(CHARSET);
            response.setContentType("text/html");
            response.getWriter().println(explainationOutput);
            response.getWriter().close();
            return;
        }
                
        Results results = searchServer.search(query, offset, limit);
        org.musicbrainz.search.servlet.ResultsWriter writer = searchServer.getWriter(responseVersion);

        if (writer == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorMessage.NO_HANDLER_FOR_TYPE_AND_FORMAT.getMsg(resourceType, responseFormat));
            return;
        }
        response.setCharacterEncoding(CHARSET);
        if (responseFormat.equals(RESPONSE_XML)) {
            response.setContentType(writer.getMimeType());
        } else {
            response.setContentType(((ResultsWriter) writer).getJsonMimeType());
        }

        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), CHARSET)));
        writer.write(out, results, responseFormat, isPretty);
        out.close();
    }

    /**
     * Search over multiple different indexes and return merged result
     *
     *
     * @param response
     * @param query
     * @param isDismax
     * @param offset
     * @param limit
     * @param responseFormat    @throws ParseException
     * @param isPretty
     * @throws IOException
     */
    private void doAllSearch(HttpServletResponse response,
                             String query,
                             boolean isDismax, Integer offset,
                             Integer limit,
                             String responseFormat, boolean isPretty)
            throws Exception {

        SearchServer artistSearch = searchers.get(ResourceType.ARTIST);
        SearchServer releaseSearch = searchers.get(ResourceType.RELEASE);
        SearchServer releaseGroupSearch = searchers.get(ResourceType.RELEASE_GROUP);
        SearchServer labelSearch = searchers.get(ResourceType.LABEL);
        SearchServer recordingSearch = searchers.get(ResourceType.RECORDING);
        SearchServer workSearch = searchers.get(ResourceType.WORK);

        Collection<SearchServer> searches = new ArrayList<SearchServer>();
        if(isDismax) {
            searches.add(new ArtistDismaxSearch(artistSearch.getIndexSearcher(), query, offset, limit));
            searches.add(new ReleaseDismaxSearch(releaseSearch.getIndexSearcher(), query, offset, limit));
            searches.add(new ReleaseGroupDismaxSearch(releaseGroupSearch.getIndexSearcher(), query, offset, limit));
            searches.add(new LabelDismaxSearch(labelSearch.getIndexSearcher(), query, offset, limit));
            searches.add(new RecordingDismaxSearch(recordingSearch.getIndexSearcher(), query, offset, limit));
            searches.add(new WorkDismaxSearch(workSearch.getIndexSearcher(), query, offset, limit));
        }
        else {
            searches.add(new ArtistSearch(artistSearch.getIndexSearcher(), query, offset, limit));
            searches.add(new ReleaseSearch(releaseSearch.getIndexSearcher(), query, offset, limit));
            searches.add(new ReleaseGroupSearch(releaseGroupSearch.getIndexSearcher(), query, offset, limit));
            searches.add(new LabelSearch(labelSearch.getIndexSearcher(), query, offset, limit));
            searches.add(new RecordingSearch(recordingSearch.getIndexSearcher(), query, offset, limit));
            searches.add(new WorkSearch(workSearch.getIndexSearcher(), query, offset, limit));
        }

        //Run each search in parallel then merge results
        java.util.List<java.util.concurrent.Future<Results>> results = es.invokeAll(searches);
        Results allResults = new Results();
        //Results are returned in same order as they were submitted
        Results artistResults = results.get(0).get();
        Results releaseResults = results.get(1).get();
        Results releaseGroupResults = results.get(2).get();
        Results labelResults = results.get(3).get();
        Results recordingResults = results.get(4).get();
        Results workResults = results.get(5).get();


        AllWriter writer = new AllWriter(artistResults, releaseResults, releaseGroupResults, labelResults, recordingResults, workResults);
        response.setCharacterEncoding(CHARSET);

        if (responseFormat.equals(RESPONSE_XML)) {
            response.setContentType(writer.getMimeType());
        } else {
            response.setContentType(writer.getJsonMimeType());
        }

        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), CHARSET)));
        writer.write(out, allResults, responseFormat, isPretty);
        out.close();
    }
}
