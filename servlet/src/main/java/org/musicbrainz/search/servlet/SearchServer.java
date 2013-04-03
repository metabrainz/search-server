package org.musicbrainz.search.servlet;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherManager;

public interface SearchServer {

  public abstract org.musicbrainz.search.servlet.ResultsWriter getWriter(String version);

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
  public abstract Results search(String query, int offset, int limit) throws IOException, ParseException;

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
  public abstract Results search(Query query, int offset, int limit) throws IOException, ParseException;

  /**
   * Explain the results This method is for debugging and to allow end users to understand why their query is not
   * returning the results they expected so they can refine their query
   * 
   * @param query
   * @param offset
   * @param limit
   * @return
   * @throws IOException
   * @throws ParseException
   */
  public abstract String explain(Query query, int offset, int limit) throws IOException, ParseException;

  public abstract String explain(String query, int offset, int limit) throws IOException, ParseException;

  public abstract SearcherManager getSearcherManager();

  public Analyzer getAnalyzer();

  public abstract String getCount();

  public abstract void close() throws IOException;

  public abstract void reloadIndex() throws CorruptIndexException, IOException;

}