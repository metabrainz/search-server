package org.musicbrainz.search.servlet;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.EventIndexField;
import org.musicbrainz.search.servlet.mmd2.EventWriter;

import java.io.IOException;
import java.util.ArrayList;

public class EventSearch extends AbstractSearchServer {

  protected void setupDefaultFields() {
    defaultFields = new ArrayList<String>();
    defaultFields.add(EventIndexField.EVENT.getName());
    defaultFields.add(EventIndexField.ALIAS.getName());
  }

  public EventSearch() throws Exception {
    resultsWriter = new EventWriter();
    setupDefaultFields();
    analyzer = DatabaseIndex.getAnalyzer(EventIndexField.class);
  }

  public EventSearch(SearcherManager searcherManager) throws Exception {
    this();
    this.searcherManager = searcherManager;
    setLastServerUpdatedDate();
    resultsWriter.setLastServerUpdatedDate(this.getServerLastUpdatedDate());
  }



    @Override
    public QueryParser getParser() {
        return new QueryParser(LuceneVersion.LUCENE_VERSION, defaultFields.get(0), analyzer);
    }


  @Override
  protected String printExplainHeader(Document doc) throws IOException, ParseException {
    return doc.get(EventIndexField.EVENT_ID.getName()) + ':' + doc.get(EventIndexField.EVENT.getName()) + '\n';
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
        results.setResourceType(ResourceType.EVENT);
        return results;
    }
}
