package org.musicbrainz.search.servlet;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.queryParser.QueryParser;
import org.musicbrainz.search.index.ReleaseGroupIndexField;
import org.musicbrainz.search.index.ReleaseGroupAnalyzer;

import java.util.ArrayList;


public class ReleaseGroupSearch extends SearchServer{

    public ReleaseGroupSearch() throws Exception
    {
        xmlWriter           = new ReleaseGroupXmlWriter();
        htmlWriter          = new ReleaseGroupHtmlWriter();
        defaultFields       = new ArrayList<String>();
        defaultFields.add(ReleaseGroupIndexField.RELEASEGROUP.getName());
        analyzer = new ReleaseGroupAnalyzer();
    }

    public ReleaseGroupSearch(String indexDir) throws Exception {

        this();
        indexSearcher = createIndexSearcherFromFileIndex(indexDir,"releasegroup_index");
        this.setLastServerUpdatedDate();
    }


    public ReleaseGroupSearch(IndexSearcher searcher) throws Exception {

        this();
        indexSearcher = searcher;
    }


    @Override
    protected QueryParser getParser() {
       return new ReleaseGroupQueryParser(defaultFields.get(0), analyzer);
    }

}