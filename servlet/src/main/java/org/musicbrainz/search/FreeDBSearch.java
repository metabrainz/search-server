package org.musicbrainz.search;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.NIOFSDirectory;

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