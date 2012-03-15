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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.NumericUtils;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.index.MetaIndexField;
import org.musicbrainz.search.servlet.mmd1.Mmd1XmlWriter;
import org.musicbrainz.search.servlet.mmd2.ResultsWriter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SearchServer implements Callable<Results> {

    protected String query;
    protected int    offset;
    protected int    limit;

    protected Analyzer analyzer;
    protected ResultsWriter resultsWriter;
    protected Mmd1XmlWriter mmd1XmlWriter;
    protected List<String> defaultFields;
    protected IndexSearcher indexSearcher;
    protected Date          serverLastUpdatedDate;
    protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm zz",Locale.US);
    protected AtomicInteger    searchCount = new AtomicInteger();


    protected SearchServer() {}
    
    /**
     * Set the last updated date by getting the value from the index
     *
     * Set the last updated date by getting the value from the index (where it is always stored as the last document), then
     * for efficiency convert to a format suitable for use in output html
     *
     * @throws IOException
     */
    protected void setLastServerUpdatedDate() {

    	if (indexSearcher == null) return;
    	
        // Is not a disaster if missing so just log and carry on
        try
        {
        	Term term = new Term(MetaIndexField.META.getName(), MetaIndexField.META_VALUE);
    		TermQuery query = new TermQuery(term);
    		TopDocs hits = indexSearcher.search(query, 10);

        	if (hits.scoreDocs.length == 0) {
        		System.out.println("No matches in the index for the meta document.");
        		return;
        	} else if (hits.scoreDocs.length > 1) {
        		System.out.println("More than one meta document was found in the index.");
        		return;
        	} 

        	int docId = hits.scoreDocs[0].doc;
        	MbDocument doc = new MbDocument(indexSearcher.doc(docId));
        	serverLastUpdatedDate = new Date(NumericUtils.prefixCodedToLong(doc.get(MetaIndexField.LAST_UPDATED)));
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        catch(Exception e) {
            System.out.println(e);
        }

    }

    public void reloadIndex() throws CorruptIndexException, IOException {
    	
    	if (indexSearcher != null) {
    		IndexReader oldReader = indexSearcher.getIndexReader();
			IndexReader newReader = IndexReader.openIfChanged(oldReader);
            if (newReader != null)  {
				Similarity similarity = indexSearcher.getSimilarity();
				indexSearcher = new IndexSearcher(newReader);
				indexSearcher.setSimilarity(similarity);
				this.setLastServerUpdatedDate();
                oldReader.close();
			}
    	}
    }

    public void close() throws IOException {
    	if (indexSearcher != null) {
    		indexSearcher.getIndexReader().close();
    	}
    }

    public org.musicbrainz.search.servlet.mmd2.ResultsWriter getXmlWriter() {
        return resultsWriter;
    }

    public Mmd1XmlWriter getXmlV1Writer() {
        return mmd1XmlWriter;
    }

    public List<String> getSearchFields() {
        return defaultFields;
    }

    protected IndexSearcher getIndexSearcher() {
        return indexSearcher;
    }


    public org.musicbrainz.search.servlet.ResultsWriter getWriter(String fmt, String version) {
        if(SearchServerServlet.WS_VERSION_1.equals(version)) {
            return getXmlV1Writer();
        }
        else {
            return getXmlWriter();
        }
    }

    /**
     * Use this for All Searches run on an Executor
     *
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public Results call() throws IOException, ParseException
    {
        return search(query, offset, limit);
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

        IndexSearcher searcher=null;
        try {
            searcher = getIndexSearcher();
            incRef(searcher);
            TopDocs topdocs = searcher.search(parseQuery(query), offset + limit);
            searchCount.incrementAndGet();
            return processResults(searcher, topdocs, offset);
        }
        finally {
            decRef(searcher);
        }
    }

    /**
     *
     * @param searcher
     */
    private synchronized void incRef(IndexSearcher searcher) {
        searcher.getIndexReader().incRef();
    }

    /**
     *
     * @param searcher
     * @throws IOException
     */
    private synchronized void decRef(IndexSearcher searcher) throws IOException {
        searcher.getIndexReader().decRef();
    }

    /**
     * Parse the query
     *
     * @param query
     * @return
     * @throws ParseException
     */
    protected Query parseQuery(String query) throws ParseException
    {
        QueryParser parser = getParser();
        return parser.parse(query);
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
     * @param topDocs
     * @param offset
     * @return
     * @throws IOException
     */
    private Results processResults(IndexSearcher searcher, TopDocs topDocs, int offset) throws IOException {
        Results results = new Results();
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
