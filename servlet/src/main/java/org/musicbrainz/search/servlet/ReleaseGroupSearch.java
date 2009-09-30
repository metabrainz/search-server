package org.musicbrainz.search.servlet;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.NIOFSDirectory;
import org.musicbrainz.search.index.ReleaseGroupIndexField;
import org.musicbrainz.search.index.ReleaseAnalyzer;
import org.musicbrainz.search.index.ReleaseGroupAnalyzer;

import java.io.File;
import java.util.ArrayList;


public class ReleaseGroupSearch extends SearchServer{

    public ReleaseGroupSearch() throws Exception
    {
        xmlWriter           = new ReleaseGroupXmlWriter();
        htmlWriter          = new ReleaseGroupHtmlWriter();
        queryMangler        = new ReleaseGroupMangler();
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


}