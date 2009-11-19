package org.musicbrainz.search.servlet;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.util.Version;
import org.musicbrainz.search.index.FreeDBIndexField;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.util.ArrayList;


public class FreeDBSearch extends SearchServer {

    private FreeDBSearch() throws Exception {
        mmd1XmlWriter = null;
        defaultFields = new ArrayList<String>();
        defaultFields.add(FreeDBIndexField.ARTIST.getName());
        defaultFields.add(FreeDBIndexField.TITLE.getName());
        analyzer = new PerFieldEntityAnalyzer(FreeDBIndexField.class);
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


    @Override
    protected QueryParser getParser() {
     return new MultiFieldQueryParser(Version.LUCENE_CURRENT,defaultFields.toArray(new String[0]), analyzer);
  }
}