package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.TagIndexField;
import org.musicbrainz.search.servlet.mmd2.TagWriter;

import java.util.ArrayList;


public class TagSearch extends SearchServer {

    public TagSearch() throws Exception {
    
        resultsWriter = new TagWriter();
        defaultFields = new ArrayList<String>();
        defaultFields.add(TagIndexField.TAG.getName());
        analyzer = DatabaseIndex.getAnalyzer(TagIndexField.class);
    }

    public TagSearch(IndexSearcher searcher) throws Exception {
        this();
        indexSearcher = searcher;
    }

    public TagSearch(IndexSearcher searcher, String query, int offset, int limit) throws Exception {
        this(searcher);
        this.query=query;
        this.offset=offset;
        this.limit=limit;
    }

     @Override
    protected QueryParser getParser() {
       return new TagQueryParser(defaultFields.get(0), analyzer);
    }
}