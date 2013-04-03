package org.musicbrainz.search.servlet;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherManager;

public abstract class AbstractDismaxSearchServer implements SearchServer {

  protected DismaxSearcher dismaxSearcher;
  protected AbstractSearchServer realSearchServer;

  protected AbstractDismaxSearchServer(AbstractSearchServer mainSearchServer) {
    this.realSearchServer = mainSearchServer;
    this.dismaxSearcher = initDismaxSearcher();
  }

  abstract protected DismaxSearcher initDismaxSearcher();

  // Default parseQuery implemention
  protected Query parseQuery(String query) throws ParseException
  {
    return dismaxSearcher.parseQuery(query, realSearchServer.getAnalyzer());
  }

  @Override
  public Results search(String userQuery, int offset, int limit) throws IOException, ParseException {
    // Parse query with the dismaxSearcher, then delegate the search to the backend search server
    Query query = parseQuery(userQuery);
    return realSearchServer.search(query, offset, limit);
  }

  @Override
  public Results search(Query query, int offset, int limit) throws IOException, ParseException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String explain(String userQuery, int offset, int limit) throws IOException, ParseException {
      Query query = parseQuery(userQuery);
      return realSearchServer.explain(query, offset, limit);
  }


    @Override
  public String explain(Query query, int offset, int limit) throws IOException, ParseException {
    return realSearchServer.explain(query, offset, limit);
  }

  @Override
  public SearcherManager getSearcherManager() {
    return realSearchServer.getSearcherManager();
  }

  @Override
  public Analyzer getAnalyzer() {
    return realSearchServer.getAnalyzer();
  }

  @Override
  public String getCount() {
    return realSearchServer.getCount();
  }

  @Override
  public ResultsWriter getWriter(String version) {
    return realSearchServer.getWriter(version);
  }

  @Override
  public void close() throws IOException {
    realSearchServer.close();
  }

  @Override
  public void reloadIndex() throws CorruptIndexException, IOException {
    realSearchServer.reloadIndex();
  }

}
