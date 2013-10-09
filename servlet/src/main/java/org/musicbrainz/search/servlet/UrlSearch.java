package org.musicbrainz.search.servlet;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.UrlIndexField;
import org.musicbrainz.search.servlet.mmd2.UrlWriter;

import java.io.IOException;
import java.util.ArrayList;

public class UrlSearch extends AbstractSearchServer {

  protected void setupDefaultFields() {
    defaultFields = new ArrayList<String>();
    defaultFields.add(UrlIndexField.URL.getName());
  }

  public UrlSearch() throws Exception {

    resultsWriter = new UrlWriter();
    setupDefaultFields();
    analyzer = DatabaseIndex.getAnalyzer(UrlIndexField.class);
  }

  public UrlSearch(SearcherManager searcherManager) throws Exception {
    this();
    this.searcherManager = searcherManager;
    setLastServerUpdatedDate();
    resultsWriter.setLastServerUpdatedDate(this.getServerLastUpdatedDate());
  }

  public UrlSearch(SearcherManager searcherManager, String query, int offset, int limit) throws Exception {
    this(searcherManager);
    this.query = query;
    this.offset = offset;
    this.limit = limit;
  }

  @Override
  public QueryParser getParser() {
    return new UrlQueryParser(defaultFields.get(0), analyzer);
  }

  @Override
  protected String printExplainHeader(Document doc) throws IOException, ParseException
  {
    return doc.get(UrlIndexField.ID.getName()) + ':' + doc.get(UrlIndexField.URL.getName()) + '\n';
  }

    /**
     *
     * @param searcher
     * @param topDocs
     * @param offset
     * @return
     * @throws java.io.IOException
     */
    protected Results processResults(IndexSearcher searcher, TopDocs topDocs, int offset) throws IOException
    {
        Results results = super.processResults(searcher, topDocs, offset);
        results.setResourceType(ResourceType.URL);
        return results;
    }
}