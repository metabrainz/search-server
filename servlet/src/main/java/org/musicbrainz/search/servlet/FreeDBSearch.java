package org.musicbrainz.search.servlet;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.NIOFSDirectory;
import org.musicbrainz.search.index.FreeDBIndexField;
import org.musicbrainz.search.index.ReleaseAnalyzer;
import org.musicbrainz.search.index.FreeDBAnalyzer;

import java.io.File;
import java.util.ArrayList;


public class FreeDBSearch extends SearchServer {

    private FreeDBSearch() throws Exception {
        xmlWriter = null;
        htmlWriter = new FreeDBHtmlWriter();
        queryMangler = null;
        defaultFields = new ArrayList<String>();
        defaultFields.add(FreeDBIndexField.ARTIST.getName());
        defaultFields.add(FreeDBIndexField.TITLE.getName());
        analyzer = new FreeDBAnalyzer();
    }

    public FreeDBSearch(String indexDir) throws Exception {

        this();
        indexSearcher = createIndexSearcherFromFileIndex(indexDir,"freedb_index");
        this.setLastServerUpdatedDate();
    }


    public FreeDBSearch(IndexSearcher searcher) throws Exception {

        this();
        indexSearcher = searcher;
    }


}