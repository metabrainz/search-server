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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.NIOFSDirectory;
import org.musicbrainz.search.analysis.StandardUnaccentAnalyzer;

import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;

public abstract class SearchServer {

    private Analyzer analyzer;
    protected XmlWriter xmlWriter;
    protected HtmlWriter htmlWriter;
    protected QueryMangler queryMangler;
    protected List<String> defaultFields;
    protected IndexSearcher indexSearcher;

    final Logger log = Logger.getLogger(SearchServer.class.getName());


    protected SearchServer() {
        analyzer = new StandardUnaccentAnalyzer();
    }

    protected IndexSearcher createIndexSearcherFromFileIndex(String indexDir,String indexName) throws Exception
    {
        return new IndexSearcher(IndexReader.open(new NIOFSDirectory(new File(indexDir + '/' + indexName + '/'), null), true));

    }


    public void close() {
    }


    public QueryMangler getQueryMangler() {
        return queryMangler;
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

        if (getQueryMangler() != null) {
            query = getQueryMangler().mangleQuery(query);
        }
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
        return processResults(searcher, collector, offset);

    }

    /**
     * Get Query Parser for parsing queries for this resourcetype
     *
     * @return
     */
    private QueryParser getParser() {
        if (getSearchFields().size() > 1) {
            return new MultiFieldQueryParser(defaultFields.toArray(new String[0]), analyzer);
        } else {
            return new QueryParser(defaultFields.get(0), analyzer);

        }
    }

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
