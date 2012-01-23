package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

public class AnnotationDismaxSearch extends AnnotationSearch {

    /**
     * Standard Search
     *
     * @param searcher
     * @throws Exception
     */
    public AnnotationDismaxSearch(IndexSearcher searcher) throws Exception {
        super(searcher);
    }

    protected Query parseQuery(String query) throws ParseException
    {
        //Treat all as text
        query=QueryParser.escape(query);
        QueryParser parser = getParser();
        return parser.parse(query);
    }
}
