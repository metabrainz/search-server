package org.musicbrainz.search.servlet;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.SearcherManager;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.LabelIndexField;
import org.musicbrainz.search.servlet.mmd1.LabelMmd1XmlWriter;
import org.musicbrainz.search.servlet.mmd2.LabelWriter;

public class LabelSearch extends AbstractSearchServer {

  protected void setupDefaultFields() {
    defaultFields = new ArrayList<String>();
    defaultFields.add(LabelIndexField.LABEL.getName());
    defaultFields.add(LabelIndexField.ALIAS.getName());
    defaultFields.add(LabelIndexField.SORTNAME.getName());
  }

  public LabelSearch() throws Exception {
    resultsWriter = new LabelWriter();
    mmd1Writer = new LabelMmd1XmlWriter();
    setupDefaultFields();
    analyzer = DatabaseIndex.getAnalyzer(LabelIndexField.class);
  }

  public LabelSearch(SearcherManager searcherManager) throws Exception {
    this();
    this.searcherManager = searcherManager;
    setLastServerUpdatedDate();
    resultsWriter.setLastServerUpdatedDate(this.getServerLastUpdatedDate());
  }

  @Override
  public QueryParser getParser() {
    return new LabelQueryParser(defaultFields.toArray(new String[0]), analyzer);
  }

  @Override
  protected String printExplainHeader(Document doc) throws IOException, ParseException {
    return doc.get(LabelIndexField.LABEL_ID.getName()) + ':' + doc.get(LabelIndexField.LABEL.getName()) + '\n';
  }

}
