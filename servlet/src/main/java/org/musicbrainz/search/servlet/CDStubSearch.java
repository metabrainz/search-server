package org.musicbrainz.search.servlet;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.musicbrainz.search.index.CDStubIndexField;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.servlet.mmd2.CDStubWriter;


public class CDStubSearch extends AbstractSearchServer {

  protected void setupDefaultFields() {
    defaultFields = new ArrayList<String>();
    defaultFields.add(CDStubIndexField.ARTIST.getName());
    defaultFields.add(CDStubIndexField.TITLE.getName());
  }

  public CDStubSearch(SearcherManager searcherManager) throws Exception {
    resultsWriter = new CDStubWriter();
    mmd1Writer = null;
    setupDefaultFields();
    analyzer = DatabaseIndex.getAnalyzer(CDStubIndexField.class);
    this.searcherManager = searcherManager;
    setLastServerUpdatedDate();
    resultsWriter.setLastServerUpdatedDate(this.getServerLastUpdatedDate());
  }

  @Override
  public QueryParser getParser() {
    return new MultiFieldQueryParser(defaultFields.toArray(new String[0]), analyzer);
  }

  @Override
  protected  String printExplainHeader(Document doc)
      throws IOException, ParseException {
    return doc.get(CDStubIndexField.ID.getName()) +':'
        + doc.get(CDStubIndexField.ARTIST.getName()) + ':'
        + doc.get(CDStubIndexField.TITLE.getName())
        + '\n';
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
        results.setResourceType(ResourceType.CDSTUB);
        return results;
    }
}
