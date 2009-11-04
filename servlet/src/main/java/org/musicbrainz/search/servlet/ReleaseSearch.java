package org.musicbrainz.search.servlet;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.queryParser.QueryParser;
import org.musicbrainz.search.index.ReleaseIndexField;
import org.musicbrainz.search.servlet.mmd1.ReleaseMmd1XmlWriter;
import org.musicbrainz.search.servlet.mmd2.ReleaseWriter;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.util.ArrayList;


public class ReleaseSearch extends SearchServer {

    public ReleaseSearch() throws Exception {

        resultsWriter = new ReleaseWriter();
        mmd1XmlWriter = new ReleaseMmd1XmlWriter();
        defaultFields = new ArrayList<String>();
        defaultFields.add(ReleaseIndexField.RELEASE.getName());
        analyzer = new PerFieldEntityAnalyzer(ReleaseIndexField.class);
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