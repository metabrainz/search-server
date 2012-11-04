package org.musicbrainz.search.servlet;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.SearcherManager;
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.servlet.mmd1.ArtistMmd1XmlWriter;
import org.musicbrainz.search.servlet.mmd2.ArtistWriter;


public class ArtistSearch extends AbstractSearchServer {

  protected void setupDefaultFields() {
    defaultFields = new ArrayList<String>();
    defaultFields.add(ArtistIndexField.ARTIST.getName());
    defaultFields.add(ArtistIndexField.ALIAS.getName());
    defaultFields.add(ArtistIndexField.SORTNAME.getName());
  }


  private ArtistSearch() throws Exception {
    resultsWriter = new ArtistWriter();
    mmd1Writer = new ArtistMmd1XmlWriter();
    setupDefaultFields();
    analyzer = DatabaseIndex.getAnalyzer(ArtistIndexField.class);
  }

  /**
   * Standard Search
   *
   * @param searcher
   * @throws Exception
   */
  public ArtistSearch(SearcherManager searcherManager) throws Exception {
    this();
    this.searcherManager = searcherManager;
    setLastServerUpdatedDate();
    resultsWriter.setLastServerUpdatedDate(this.getServerLastUpdatedDate());

  }

  @Override
  public QueryParser getParser() {
    return new ArtistQueryParser(defaultFields.toArray(new String[0]), analyzer);
  }

  @Override
  protected  String printExplainHeader(Document doc)
      throws IOException, ParseException {
    return doc.get(ArtistIndexField.ARTIST_ID.getName()) +':'
        + doc.get(ArtistIndexField.ARTIST.getName())
        + '\n';
  }

}
