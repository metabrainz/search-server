package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.ReleaseIndexField;
import org.musicbrainz.search.servlet.mmd1.ReleaseMmd1XmlWriter;
import org.musicbrainz.search.servlet.mmd2.ReleaseWriter;

import java.util.ArrayList;


public class ReleaseSearch extends SearchServer {

    public ReleaseSearch() throws Exception {
    
        resultsWriter = new ReleaseWriter();
        mmd1XmlWriter = new ReleaseMmd1XmlWriter();
        defaultFields = new ArrayList<String>();
        defaultFields.add(ReleaseIndexField.RELEASE.getName());
        analyzer = DatabaseIndex.getAnalyzer(ReleaseIndexField.class);
    }

    public ReleaseSearch(IndexSearcher searcher) throws Exception {
        this();
        indexSearcher = searcher;
    }

    public ReleaseSearch(IndexSearcher searcher, String query, int offset, int limit) throws Exception {
        this(searcher);
        this.query=query;
        this.offset=offset;
        this.limit=limit;
    }

     @Override
    protected QueryParser getParser() {
       return new ReleaseQueryParser(defaultFields.get(0), analyzer);
    }
}