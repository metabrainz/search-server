package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

public class LabelDismaxSearch extends LabelSearch {

    private static final String  mask =
            "label:\"{0}\"^1.6 " +
        "(+sortname:\"{0}\"^1.6 -$type:\"{0}\") " +
        "(+alias:\"{0}\" -$type:\"{0}\" -sortname:\"{0}\") "  +
        "(+($type:({0})^0.8) -$type:\"{0}\" -sortname:\"{0}\" -alias:\"{0}\") "  +
        "(+(sortname:({0})^0.8) -$type:({0}) -sortname:\"{0}\" -alias:\"{0}\") " +
        "(+(alias:({0})^0.4) -$type:({0}) -sortname:({0}) -alias:\"{0}\")";

    /**
     * Standard Search
     *
     * @param searcher
     * @throws Exception
     */
    public LabelDismaxSearch(IndexSearcher searcher) throws Exception {
        super(searcher);
    }

    /**
     * User By Search All
     *
     * @param searcher
     * @param query
     * @param offset
     * @param limit
     * @throws Exception
     */
    public LabelDismaxSearch(IndexSearcher searcher, String query, int offset, int limit) throws Exception {
        super(searcher, query, offset, limit);
    }

    protected Query parseQuery(String query) throws ParseException
    {
        //Treat all as text
        query=QueryParser.escape(query);
        String phraseBoostedQuery=mask.replace("{0}", query);
        QueryParser parser = getParser();
        return parser.parse(phraseBoostedQuery);
    }
}
