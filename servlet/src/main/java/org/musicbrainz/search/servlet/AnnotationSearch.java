package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.util.Version;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;
import org.musicbrainz.search.index.AnnotationIndex;
import org.musicbrainz.search.index.AnnotationIndexField;
import org.musicbrainz.search.servlet.mmd2.AnnotationWriter;

import java.util.ArrayList;


public class AnnotationSearch extends SearchServer {

    private AnnotationSearch() throws Exception {
        resultsWriter = new AnnotationWriter();
        mmd1XmlWriter = null;
        defaultFields = new ArrayList<String>();
        defaultFields.add(AnnotationIndexField.TEXT.getName());
        analyzer = new PerFieldEntityAnalyzer(AnnotationIndexField.class);
    }

    public AnnotationSearch(String indexDir) throws Exception {

        this();
        indexSearcher = createIndexSearcherFromFileIndex(indexDir, new AnnotationIndex().getFilename());
        this.setLastServerUpdatedDate();
    }


    public AnnotationSearch(IndexSearcher searcher) throws Exception {

        this();
        indexSearcher = searcher;
    }


    @Override
    protected QueryParser getParser() {
     return new QueryParser(Version.LUCENE_CURRENT,defaultFields.get(0), analyzer);
  }


}