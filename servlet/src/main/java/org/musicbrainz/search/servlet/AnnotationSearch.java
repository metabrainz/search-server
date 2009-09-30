package org.musicbrainz.search.servlet;

import org.apache.lucene.search.IndexSearcher;
import org.musicbrainz.search.index.AnnotationIndexField;
import org.musicbrainz.search.index.ReleaseAnalyzer;
import org.musicbrainz.search.index.AnnotationAnalyzer;

import java.util.ArrayList;


public class AnnotationSearch extends SearchServer {

    private AnnotationSearch() throws Exception {
        xmlWriter = null;
        htmlWriter = new AnnotationHtmlWriter();
        queryMangler = null;
        defaultFields = new ArrayList<String>();
        defaultFields.add(AnnotationIndexField.TEXT.getName());
        analyzer = new AnnotationAnalyzer();
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


}