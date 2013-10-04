package org.musicbrainz.search.servlet;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.musicbrainz.search.index.PlaceIndexField;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.servlet.mmd2.PlaceWriter;

import java.io.IOException;
import java.util.ArrayList;

public class PlaceSearch extends AbstractSearchServer {

  protected void setupDefaultFields() {
    defaultFields = new ArrayList<String>();
    defaultFields.add(PlaceIndexField.PLACE.getName());
    defaultFields.add(PlaceIndexField.ALIAS.getName());
    defaultFields.add(PlaceIndexField.AREA.getName());
    defaultFields.add(PlaceIndexField.ADDRESS.getName());
  }

  public PlaceSearch() throws Exception {
    resultsWriter = new PlaceWriter();
    setupDefaultFields();
    analyzer = DatabaseIndex.getAnalyzer(PlaceIndexField.class);
  }

  public PlaceSearch(SearcherManager searcherManager) throws Exception {
    this();
    this.searcherManager = searcherManager;
    setLastServerUpdatedDate();
    resultsWriter.setLastServerUpdatedDate(this.getServerLastUpdatedDate());
  }

  @Override
  public QueryParser getParser() {
    return new PlaceQueryParser(defaultFields.toArray(new String[0]), analyzer);
  }

  @Override
  protected String printExplainHeader(Document doc) throws IOException, ParseException {
    return doc.get(PlaceIndexField.PLACE_ID.getName()) + ':' + doc.get(PlaceIndexField.PLACE.getName()) + '\n';
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
        results.setResourceType(ResourceType.PLACE);
        return results;
    }
}
