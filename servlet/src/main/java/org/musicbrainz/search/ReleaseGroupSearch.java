package org.musicbrainz.search;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.NIOFSDirectory;

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