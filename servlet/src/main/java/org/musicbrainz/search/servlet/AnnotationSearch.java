package org.musicbrainz.search.servlet;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.musicbrainz.search.index.AnnotationIndexField;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.servlet.mmd2.AnnotationWriter;


public class AnnotationSearch extends AbstractSearchServer {

  protected void setupDefaultFields() {
    defaultFields = new ArrayList<String>();
    defaultFields.add(AnnotationIndexField.TEXT.getName());
  }

  private AnnotationSearch() throws Exception {
    resultsWriter = new AnnotationWriter();
    mmd1Writer = null;
    setupDefaultFields();
    analyzer = DatabaseIndex.getAnalyzer(AnnotationIndexField.class);
  }

  public AnnotationSearch(SearcherManager searcherManager) throws Exception {
    this();
    this.searcherManager = searcherManager;
    setLastServerUpdatedDate();
    resultsWriter.setLastServerUpdatedDate(this.getServerLastUpdatedDate());
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
        results.setResourceType(ResourceType.ANNOTATION);
        return results;
    }

  @Override
  public QueryParser getParser() {
    return new QueryParser(defaultFields.get(0), analyzer);
  }

  @Override
  protected  String printExplainHeader(Document doc)
      throws IOException, ParseException {
    return doc.get(AnnotationIndexField.ID.getName()) +':'
        + doc.get(AnnotationIndexField.ENTITY.getName())
        + '\n';
  }


}
