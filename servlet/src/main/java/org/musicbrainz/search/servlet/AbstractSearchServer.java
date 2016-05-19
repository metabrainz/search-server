/* Copyright (c) 2012 The MusicBrainz Search Server Authors
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopDocsCollector;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.TimeLimitingCollector;
import org.apache.lucene.search.TimeLimitingCollector.TimeExceededException;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.index.MetaIndexField;
import org.musicbrainz.search.servlet.mmd1.Mmd1XmlWriter;
import org.musicbrainz.search.servlet.mmd2.ResultsWriter;

public abstract class AbstractSearchServer implements SearchServer {

  protected String query;
  protected int offset;
  protected int limit;

  protected Analyzer analyzer;
  protected ResultsWriter resultsWriter;
  protected Mmd1XmlWriter mmd1Writer;
  protected List<String> defaultFields;
  protected SearcherManager searcherManager;
  protected Date serverLastUpdatedDate;
  protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm zz", Locale.US);
  protected AtomicInteger searchCount = new AtomicInteger();

  protected AbstractSearchServer() {
  }

  /**
   * Set the last updated date by getting the value from the index, then for efficiency convert to a format suitable for
   * use in output html
   * 
   * @throws IOException
   */
  protected void setLastServerUpdatedDate() throws IOException {

    if (searcherManager == null) {
      return;
    }

    // Is not a disaster if missing so just log and carry on
    IndexSearcher searcher = searcherManager.acquire();
    try {
      Term term = new Term(MetaIndexField.META.getName(), MetaIndexField.META_VALUE);
      TermQuery query = new TermQuery(term);
      TopDocs hits = searcher.search(query, 10);

      if (hits.scoreDocs.length == 0) {
        System.out.println("No matches in the index for the meta document.");
        return;
      } else if (hits.scoreDocs.length > 1) {
        System.out.println("More than one meta document was found in the index.");
        return;
      }

      int docId = hits.scoreDocs[0].doc;
      MbDocument doc = new MbDocument(searcher.doc(docId));


      String lastUpdated = doc.get(MetaIndexField.LAST_UPDATED);
      serverLastUpdatedDate = new Date(NumericUtils.prefixCodedToLong(new BytesRef(lastUpdated)));
      dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    } catch (Exception e) {
      System.out.println(e);
    } finally {
      searcherManager.release(searcher);
    }
  }

  public Date getServerLastUpdatedDate() {
    return serverLastUpdatedDate;
  }

  @Override
  public void reloadIndex() throws CorruptIndexException, IOException {
    if (searcherManager != null) {
      // Try to refresh
      searcherManager.maybeRefresh();
      // Update last update date
      this.setLastServerUpdatedDate();
      resultsWriter.setLastServerUpdatedDate(this.getServerLastUpdatedDate());
    }
  }

  @Override
  public void close() throws IOException {
  }

  public org.musicbrainz.search.servlet.mmd2.ResultsWriter getMmd2Writer() {
    return resultsWriter;
  }

  public Mmd1XmlWriter getMmd1Writer() {
    return mmd1Writer;
  }

  public List<String> getSearchFields() {
    return defaultFields;
  }

  /* (non-Javadoc)
   * @see org.musicbrainz.search.servlet.ISearchServer#getWriter(java.lang.String)
   */
  @Override
  public org.musicbrainz.search.servlet.ResultsWriter getWriter(String version) {
    if (SearchServerServlet.WS_VERSION_1.equals(version)) {
      return getMmd1Writer();
    } else {
      return getMmd2Writer();
    }
  }

  /**
   * Parse and search lucene query, returning between results from offset up to limit
   * 
   * @param query
   * @param offset
   * @param limit
   * @return
   * @throws IOException
   * @throws ParseException if the query was invalid
   */
  @Override
  public Results search(String query, int offset, int limit) throws IOException, ParseException {
    return this.search(parseQuery(query), offset, limit);
  }

  /**
   * Parse and search lucene query, returning between results from offset up to limit
   * 
   * @param query
   * @param offset
   * @param limit
   * @return
   * @throws IOException
   * @throws ParseException if the query was invalid
   */
  @Override
  public Results search(Query query, int offset, int limit) throws IOException, ParseException, TimeExceededException {

    IndexSearcher searcher = searcherManager.acquire();
    try {
      TopDocsCollector<?> collector = TopScoreDocCollector.create(offset + limit, true);
      TimeLimitingCollector tCollector = new TimeLimitingCollector(collector, TimeLimitingCollector.getGlobalCounter(), 3000);
      searcher.search(query, tCollector);
      searchCount.incrementAndGet();
      TopDocs topDocs = collector.topDocs();
      if (topDocs == null)
      {
          return new Results();
      }
      return processResults(searcher, topDocs, offset);
    } finally {
      searcherManager.release(searcher);
    }
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
   * @return count of searches done on this index since servlet started
   */
  @Override
  public String getCount() {
    return searchCount.toString();
  }

  /**
   * Get Query Parser for parsing queries for this resourceType , QueryParser is not thread safe so always get a new
   * instance;
   * 
   * @return
   */
  public abstract QueryParser getParser();

  /**
   * Process results of search
   * 
   * @param searcher
   * @param topDocs
   * @param offset
   * @return
   * @throws IOException
   */
  protected Results processResults(IndexSearcher searcher, TopDocs topDocs, int offset) throws IOException {
    Results results = new Results();
    results.setOffset(offset);
    results.setTotalHits(topDocs.totalHits);
    ScoreDoc docs[] = topDocs.scoreDocs;
    results.setMaxScore(topDocs.getMaxScore());
    for (int i = offset; i < docs.length; i++) {
      Result result     = new Result();
      result.setScore(docs[i].score);
      result.setDoc(new MbDocument(searcher.doc(docs[i].doc)));
      results.results.add(result);
    }
    return results;
  }

  public String explain(String userQuery, int offset, int limit) throws IOException, ParseException {
    Query parsedQuery = parseQuery(userQuery);
    return explain(parsedQuery, offset, limit);
  }

  /* (non-Javadoc)
   * @see org.musicbrainz.search.servlet.ISearchServer#explain(java.lang.String, int, int)
   */
  @Override
  public String explain(Query query, int offset, int limit) throws IOException, ParseException {
    StringBuffer sb = new StringBuffer("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n");
    sb.append("<html lang=\"en\">\n<head>\n");
    sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n");
    sb.append("</head>\n<body>");
    IndexSearcher searcher = searcherManager.acquire();
    try {
      TopDocs topdocs = searcher.search(query, offset + limit);
      ScoreDoc docs[] = topdocs.scoreDocs;
      float maxScore = topdocs.getMaxScore();
      sb.append("<p>Query:" + query.toString() + "</p>\n");
      for (int i = 0; i < docs.length; i++) {
        explainAndDisplayResult(i, sb, searcher, query, docs[i], maxScore);
      }
      searchCount.incrementAndGet();
    } finally {
      searcherManager.release(searcher);
    }
    sb.append("</body>\n</html>");
    return sb.toString();
  }

  /**
   * Output the Explain for the document
   * 
   * @param sb
   * @param searcher
   * @param query
   * @param scoreDoc
   * @throws IOException
   * @throws ParseException
   */
  protected void explainAndDisplayResult(int i, StringBuffer sb, IndexSearcher searcher, Query query,
      ScoreDoc scoreDoc, float maxScore) throws IOException, ParseException {
    sb.append("<p>" + i + ":Score:" + (scoreDoc.score / maxScore) * 100 + "</p>\n");
    sb.append(printExplainHeader(searcher.doc(scoreDoc.doc)));
    sb.append(searcher.explain(query, scoreDoc.doc).toHtml());

  }

  /**
   * Print details about the matching document, override to give resource type specific information
   * 
   * @param doc
   * @return
   * @throws IOException
   * @throws ParseException
   */
  protected abstract String printExplainHeader(Document doc) throws IOException, ParseException;

  @Override
  public SearcherManager getSearcherManager() {
    return searcherManager;
  }

  @Override
  public Analyzer getAnalyzer() {
    return analyzer;
  }

}
