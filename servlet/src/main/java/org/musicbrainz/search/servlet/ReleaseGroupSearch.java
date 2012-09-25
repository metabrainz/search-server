package org.musicbrainz.search.servlet;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.SearcherManager;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.ReleaseGroupIndexField;
import org.musicbrainz.search.servlet.mmd1.ReleaseGroupMmd1XmlWriter;
import org.musicbrainz.search.servlet.mmd2.ReleaseGroupWriter;

public class ReleaseGroupSearch extends SearchServer {

  protected void setupDefaultFields() {
    defaultFields = new ArrayList<String>();
    defaultFields.add(ReleaseGroupIndexField.RELEASEGROUP.getName());
  }

  public ReleaseGroupSearch() throws Exception {

    resultsWriter = new ReleaseGroupWriter();
    mmd1Writer = new ReleaseGroupMmd1XmlWriter();
    setupDefaultFields();
    analyzer = DatabaseIndex.getAnalyzer(ReleaseGroupIndexField.class);
  }

  public ReleaseGroupSearch(SearcherManager searcherManager) throws Exception {
    this();
    this.searcherManager = searcherManager;
    setLastServerUpdatedDate();
    resultsWriter.setLastServerUpdatedDate(this.getServerLastUpdatedDate());
  }

  public ReleaseGroupSearch(SearcherManager searcherManager, String query, int offset, int limit) throws Exception {
    this(searcherManager);
    this.query = query;
    this.offset = offset;
    this.limit = limit;
  }

  @Override
  protected QueryParser getParser() {
    return new ReleaseGroupQueryParser(defaultFields.toArray(new String[0]), analyzer);
  }

  @Override
  protected String printExplainHeader(Document doc) throws IOException, ParseException {
    return doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID.getName()) + ':'
        + doc.get(ReleaseGroupIndexField.RELEASE.getName()) + '\n';
  }

}