package org.musicbrainz.search.servlet;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.queryParser.QueryParser;
import org.musicbrainz.search.index.AnnotationIndexField;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.util.ArrayList;


public class AnnotationSearch extends SearchServer {

    private AnnotationSearch() throws Exception {
        mmd1XmlWriter = null;
        defaultFields = new ArrayList<String>();
        defaultFields.add(AnnotationIndexField.TEXT.getName());
        analyzer = new PerFieldEntityAnalyzer(AnnotationIndexField.class);
    }

    public AnnotationSearch(String indexDir) throws Exception {

        this();
        indexSearcher = createIndexSearcherFromFileIndex(indexDir, "annotation_index");
        this.setLastServerUpdatedDate();
    }


    public AnnotationSearch(IndexSearcher searcher) throws Exception {

        this();
        indexSearcher = searcher;
    }


    @Override
    protected QueryParser getParser() {
     return new QueryParser(defaultFields.get(0), analyzer);
  }


}