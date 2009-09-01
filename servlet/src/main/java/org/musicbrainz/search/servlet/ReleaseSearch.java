package org.musicbrainz.search.servlet;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.NIOFSDirectory;
import org.musicbrainz.search.index.ReleaseIndexField;

import java.io.File;
import java.util.ArrayList;


public class ReleaseSearch extends SearchServer {

    public ReleaseSearch() throws Exception {

        xmlWriter = new ReleaseXmlWriter();
        htmlWriter = new ReleaseHtmlWriter();
        queryMangler = new ReleaseMangler();
        defaultFields = new ArrayList<String>();
        defaultFields.add(ReleaseIndexField.RELEASE.getName());
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


}