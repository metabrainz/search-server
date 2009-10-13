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

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.NumericUtils;
import org.musicbrainz.search.index.MetaIndexField;

import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;

public abstract class SearchServer {

    protected PerFieldAnalyzerWrapper analyzer;
    protected XmlWriter xmlWriter;
    protected HtmlWriter htmlWriter;
    protected List<String> defaultFields;
    protected IndexSearcher indexSearcher;
    protected Date          serverLastUpdatedDate;
    protected String        htmlLastUpdated;
    protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm zz",Locale.US);
    protected AtomicInteger    searchCount = new AtomicInteger();

    final Logger log = Logger.getLogger(SearchServer.class.getName());


    protected SearchServer() {

    }

    protected IndexSearcher createIndexSearcherFromFileIndex(String indexDir,String indexName) throws Exception
    {
        return new IndexSearcher(IndexReader.open(new NIOFSDirectory(new File(indexDir + '/' + indexName + '/'), null), true));

    }

    /**
     * Set the last updated date by getting the value from the index
     *
     * Set the last updated date by getting the value from the index (where it is always stored as the last document), then
     * for efficiency convert to a format suitable for use in output html
     *
     * @throws IOException
     */
    protected void setLastServerUpdatedDate() {

        //Is not a disaster if missing so just log and carry on
        try
        {
            serverLastUpdatedDate = new Date(NumericUtils.prefixCodedToLong(indexSearcher.getIndexReader().document(indexSearcher.getIndexReader().maxDoc()-1)
                    .getField(MetaIndexField.META.getName()).stringValue()));
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            htmlWriter.setLastUpdated(dateFormat.format(serverLastUpdatedDate));
        }
        catch(Exception e) {
            System.out.println(e);
        }

    }


    public void close() {
    }


    public XmlWriter getXmlWriter() {
        return xmlWriter;
    }

    public HtmlWriter getHtmlWriter() {
        return htmlWriter;
    }

    public List<String> getSearchFields() {
        return defaultFields;
    }

    protected IndexSearcher getIndexSearcher() {
        return indexSearcher;
    }


    public ResultsWriter getWriter(String fmt) {
        if (SearchServerServlet.RESPONSE_XML.equals(fmt)) {
            return getXmlWriter();
        } else {
            return getHtmlWriter();
        }
    }

    /**
     * Process query from Mbserver before sending to lucene searcher, returning between results from offset upto limit
     *
     * @param query
     * @param offset
     * @param limit
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public Results search(String query, int offset, int limit) throws IOException, ParseException {

        return searchLucene(query, offset, limit);
    }

    /**
     * Parse and search lucene query, returning between results from offset upto limit
     *
     * @param query
     * @param offset
     * @param limit
     * @return
     * @throws IOException
     * @throws ParseException if the query was invalid
     */
    public Results searchLucene(String query, int offset, int limit) throws IOException, ParseException {
        IndexSearcher searcher = getIndexSearcher();
        QueryParser parser = getParser();
        TopScoreDocCollector collector = TopScoreDocCollector.create(offset + limit, true);
        searcher.search(parser.parse(query), collector);
        searchCount.incrementAndGet();
        return processResults(searcher, collector, offset);
    }

    /**
     *
     * @return count of searches done on this index since servlet started
     */
    public String getCount()
    {
        return searchCount.toString();
    }

    /**
     * Get Query Parser for parsing queries for this resourcetype , QueryParser  is not thread safe so always
     * get a new instance;
     *
     * @return
     */
    protected abstract QueryParser getParser();

    /**
     * Process results of search
     *
     * @param searcher
     * @param collector
     * @param offset
     * @return
     * @throws IOException
     */
    private Results processResults(IndexSearcher searcher, TopScoreDocCollector collector, int offset) throws IOException {
        Results results = new Results();
        TopDocs topDocs = collector.topDocs();
        results.offset = offset;
        results.totalHits = topDocs.totalHits;
        ScoreDoc docs[] = topDocs.scoreDocs;
        float maxScore = topDocs.getMaxScore();
        for (int i = offset; i < docs.length; i++) {
            Result result = new Result();
            result.score = docs[i].score / maxScore;
            result.doc = new MbDocument(searcher.doc(docs[i].doc));
            results.results.add(result);
        }
        return results;
    }
}
