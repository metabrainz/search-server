package org.musicbrainz.search.servlet;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.SearcherManager;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.ReleaseGroupIndexField;
import org.musicbrainz.search.servlet.mmd1.ReleaseGroupMmd1XmlWriter;
import org.musicbrainz.search.servlet.mmd2.ReleaseGroupWriter;

public class ReleaseGroupSearch extends AbstractSearchServer {

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

  @Override
  public QueryParser getParser() {
    return new ReleaseGroupQueryParser(defaultFields.toArray(new String[0]), analyzer);
  }

  @Override
  protected String printExplainHeader(Document doc) throws IOException, ParseException {
    return doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID.getName()) + ':'
        + doc.get(ReleaseGroupIndexField.RELEASE.getName()) + '\n';
  }

}