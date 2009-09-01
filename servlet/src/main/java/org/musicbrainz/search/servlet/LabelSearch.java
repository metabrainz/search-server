package org.musicbrainz.search.servlet;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.NIOFSDirectory;
import org.musicbrainz.search.index.LabelIndexField;

import java.io.File;
import java.util.ArrayList;


public class LabelSearch extends SearchServer {

    public LabelSearch() throws Exception {

        xmlWriter = new LabelXmlWriter();
        htmlWriter = new LabelHtmlWriter();
        queryMangler = new LabelMangler();
        defaultFields = new ArrayList<String>();
        defaultFields.add(LabelIndexField.LABEL.getName());
        defaultFields.add(LabelIndexField.ALIAS.getName());
        defaultFields.add(LabelIndexField.SORTNAME.getName());

    }

    public LabelSearch(String indexDir) throws Exception {

        this();
        indexSearcher = createIndexSearcherFromFileIndex(indexDir,"label_index");
        this.setLastServerUpdatedDate();
    }


    public LabelSearch(IndexSearcher searcher) throws Exception {

        this();
        indexSearcher = searcher;
    }


}
