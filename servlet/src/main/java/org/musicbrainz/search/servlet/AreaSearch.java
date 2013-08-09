package org.musicbrainz.search.servlet;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.AreaIndexField;
import org.musicbrainz.search.servlet.mmd2.AreaWriter;

import java.io.IOException;
import java.util.ArrayList;

public class AreaSearch extends AbstractSearchServer {

  protected void setupDefaultFields() {
    defaultFields = new ArrayList<String>();
    defaultFields.add(AreaIndexField.AREA.getName());
    defaultFields.add(AreaIndexField.ALIAS.getName());
    defaultFields.add(AreaIndexField.SORTNAME.getName());
  }

  public AreaSearch() throws Exception {
    resultsWriter = new AreaWriter();
    setupDefaultFields();
    analyzer = DatabaseIndex.getAnalyzer(AreaIndexField.class);
  }

  public AreaSearch(SearcherManager searcherManager) throws Exception {
    this();
    this.searcherManager = searcherManager;
    setLastServerUpdatedDate();
    resultsWriter.setLastServerUpdatedDate(this.getServerLastUpdatedDate());
  }

  @Override
  public QueryParser getParser() {
    return new AreaQueryParser(defaultFields.toArray(new String[0]), analyzer);
  }

  @Override
  protected String printExplainHeader(Document doc) throws IOException, ParseException {
    return doc.get(AreaIndexField.AREA_ID.getName()) + ':' + doc.get(AreaIndexField.AREA.getName()) + '\n';
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
        results.setResourceType(ResourceType.AREA);
        return results;
    }
}
