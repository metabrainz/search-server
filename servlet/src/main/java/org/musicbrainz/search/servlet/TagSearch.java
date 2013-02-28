package org.musicbrainz.search.servlet;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.TagIndexField;
import org.musicbrainz.search.servlet.mmd2.TagWriter;

public class TagSearch extends AbstractSearchServer {

  protected void setupDefaultFields() {
    defaultFields = new ArrayList<String>();
    defaultFields.add(TagIndexField.TAG.getName());
  }

  public TagSearch() throws Exception {

    resultsWriter = new TagWriter();
    setupDefaultFields();
    analyzer = DatabaseIndex.getAnalyzer(TagIndexField.class);
  }

  public TagSearch(SearcherManager searcherManager) throws Exception {
    this();
    this.searcherManager = searcherManager;
    setLastServerUpdatedDate();
    resultsWriter.setLastServerUpdatedDate(this.getServerLastUpdatedDate());
  }

  public TagSearch(SearcherManager searcherManager, String query, int offset, int limit) throws Exception {
    this(searcherManager);
    this.query = query;
    this.offset = offset;
    this.limit = limit;
  }

  @Override
  public QueryParser getParser() {
    return new TagQueryParser(defaultFields.get(0), analyzer);
  }

  @Override
  protected String printExplainHeader(Document doc) throws IOException, ParseException
  {
    return doc.get(TagIndexField.ID.getName()) + ':' + doc.get(TagIndexField.TAG.getName()) + '\n';
  }

    /**
     *
     * @param searcher
     * @param topDocs
     * @param offset
     * @return
     * @throws IOException
     */
    protected Results processResults(IndexSearcher searcher, TopDocs topDocs, int offset) throws IOException
    {
        Results results = super.processResults(searcher, topDocs, offset);
        results.setResourceType(ResourceType.TAG);
        return results;
    }
}