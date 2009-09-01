package org.musicbrainz.search.servlet;

import org.apache.lucene.search.IndexSearcher;
import org.musicbrainz.search.index.AnnotationIndexField;

import java.util.ArrayList;


public class AnnotationSearch extends SearchServer {

    private AnnotationSearch() throws Exception {
        xmlWriter = null;
        htmlWriter = new AnnotationHtmlWriter();
        queryMangler = null;
        defaultFields = new ArrayList<String>();
        defaultFields.add(AnnotationIndexField.NAME.getName());
        defaultFields.add(AnnotationIndexField.TYPE.getName());
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