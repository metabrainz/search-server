package org.musicbrainz.search.servlet;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.SearcherManager;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.FreeDBIndexField;
import org.musicbrainz.search.servlet.mmd2.FreeDBWriter;


public class FreeDBSearch extends AbstractSearchServer {

  protected void setupDefaultFields() {
    defaultFields = new ArrayList<String>();
    defaultFields.add(FreeDBIndexField.ARTIST.getName());
    defaultFields.add(FreeDBIndexField.TITLE.getName());
  }

  private FreeDBSearch() throws Exception {
    resultsWriter = new FreeDBWriter();
    mmd1Writer = null;
    setupDefaultFields();
    analyzer = DatabaseIndex.getAnalyzer(FreeDBIndexField.class);
  }

  public FreeDBSearch(SearcherManager searcherManager) throws Exception {
    this();
    this.searcherManager = searcherManager;
  }


  @Override
  public QueryParser getParser() {
    return new MultiFieldQueryParser(LuceneVersion.LUCENE_VERSION, defaultFields.toArray(new String[0]), analyzer);
  }

  @Override
  protected  String printExplainHeader(Document doc)
      throws IOException, ParseException {
    return doc.get(FreeDBIndexField.ARTIST.getName()) +':'
        + doc.get(FreeDBIndexField.TITLE.getName())
        + '\n';
  }

}