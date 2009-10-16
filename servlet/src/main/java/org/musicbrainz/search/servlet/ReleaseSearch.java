package org.musicbrainz.search.servlet;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.queryParser.QueryParser;
import org.musicbrainz.search.index.ReleaseIndexField;
import org.musicbrainz.search.index.ReleaseAnalyzer;
import org.musicbrainz.search.servlet.mmd1.ReleaseMmd1XmlWriter;

import java.util.ArrayList;


public class ReleaseSearch extends SearchServer {

    public ReleaseSearch() throws Exception {

        mmd1XmlWriter = new ReleaseMmd1XmlWriter();
        htmlWriter = new ReleaseHtmlWriter();
        defaultFields = new ArrayList<String>();
        defaultFields.add(ReleaseIndexField.RELEASE.getName());
        analyzer = new ReleaseAnalyzer();
    }

    public ReleaseSearch(String indexDir) throws Exception {

        this();
        indexSearcher = createIndexSearcherFromFileIndex(indexDir,"release_index");
        this.setLastServerUpdatedDate();
    }


    public ReleaseSearch(IndexSearcher searcher) throws Exception {

        this();
        indexSearcher = searcher;
    }

     @Override
    protected QueryParser getParser() {
       return new ReleaseQueryParser(defaultFields.get(0), analyzer);
    }
}