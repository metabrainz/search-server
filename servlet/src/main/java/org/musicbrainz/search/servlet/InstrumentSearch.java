package org.musicbrainz.search.servlet;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.InstrumentIndexField;
import org.musicbrainz.search.servlet.mmd2.InstrumentWriter;

import java.io.IOException;
import java.util.ArrayList;

public class InstrumentSearch extends AbstractSearchServer {

  protected void setupDefaultFields() {
    defaultFields = new ArrayList<String>();
    defaultFields.add(InstrumentIndexField.INSTRUMENT.getName());
    defaultFields.add(InstrumentIndexField.ALIAS.getName());
  }

  public InstrumentSearch() throws Exception {
    resultsWriter = new InstrumentWriter();
    setupDefaultFields();
    analyzer = DatabaseIndex.getAnalyzer(InstrumentIndexField.class);
  }

  public InstrumentSearch(SearcherManager searcherManager) throws Exception {
    this();
    this.searcherManager = searcherManager;
    setLastServerUpdatedDate();
    resultsWriter.setLastServerUpdatedDate(this.getServerLastUpdatedDate());
  }

  @Override
  public QueryParser getParser() {
    return new InstrumentQueryParser(defaultFields.toArray(new String[0]), analyzer);
  }

  @Override
  protected String printExplainHeader(Document doc) throws IOException, ParseException {
    return doc.get(InstrumentIndexField.INSTRUMENT_ID.getName()) + ':' + doc.get(InstrumentIndexField.INSTRUMENT.getName()) + '\n';
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
        results.setResourceType(ResourceType.INSTRUMENT);
        return results;
    }
}
