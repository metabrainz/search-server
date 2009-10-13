package org.musicbrainz.search.servlet;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.musicbrainz.search.index.CDStubIndexField;
import org.musicbrainz.search.index.CDStubAnalyzer;

import java.util.ArrayList;


public class CDStubSearch extends SearchServer {

    private CDStubSearch() throws Exception {
        xmlWriter = null;
        htmlWriter = new CDStubHtmlWriter();
        defaultFields = new ArrayList<String>();
        defaultFields.add(CDStubIndexField.ARTIST.getName());
        defaultFields.add(CDStubIndexField.TITLE.getName());
        analyzer = new CDStubAnalyzer();
    }

    public CDStubSearch(String indexDir) throws Exception {

        this();
        indexSearcher = createIndexSearcherFromFileIndex(indexDir, "cdstub_index");
        this.setLastServerUpdatedDate();
    }


    public CDStubSearch(IndexSearcher searcher) throws Exception {

        this();
        indexSearcher = searcher;
    }

     @Override
    protected QueryParser getParser() {
     return new MultiFieldQueryParser(defaultFields.toArray(new String[0]), analyzer);
  }

}
