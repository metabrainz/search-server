package org.musicbrainz.search;

import org.apache.lucene.search.IndexSearcher;

import java.util.ArrayList;


public class CDStubSearch extends SearchServer {

    private CDStubSearch() throws Exception {
        xmlWriter = null;
        htmlWriter = new CDStubHtmlWriter();
        queryMangler = null;
        defaultFields = new ArrayList<String>();
        defaultFields.add(CDStubIndexField.ARTIST.getName());
        defaultFields.add(CDStubIndexField.TITLE.getName());

    }

    public CDStubSearch(String indexDir) throws Exception {

        this();
        indexSearcher = createIndexSearcherFromFileIndex(indexDir, "cdstub_index");
    }


    public CDStubSearch(IndexSearcher searcher) throws Exception {

        this();
        indexSearcher = searcher;
    }


}
