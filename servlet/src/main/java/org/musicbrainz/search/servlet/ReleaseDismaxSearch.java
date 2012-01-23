package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.musicbrainz.search.index.ReleaseIndexField;

import java.util.ArrayList;

public class ReleaseDismaxSearch extends ReleaseSearch {


    protected void setupDefaultFields() {
        defaultFields       = new ArrayList<String>();
        defaultFields.add(ReleaseIndexField.RELEASE.getName());
        defaultFields.add(ReleaseIndexField.ARTIST.getName());
        defaultFields.add(ReleaseIndexField.ARTIST_NAMECREDIT.getName());
        defaultFields.add(ReleaseIndexField.LABEL.getName());
    }


    /**
     * Standard Search
     *
     * @param searcher
     * @throws Exception
     */
    public ReleaseDismaxSearch(IndexSearcher searcher) throws Exception {
        super(searcher);
    }

    protected Query parseQuery(String query) throws ParseException
    {
        //Treat all as text
        query=QueryParser.escape(query);
        QueryParser parser = getParser();
        return parser.parse(query);
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
    public ReleaseDismaxSearch(IndexSearcher searcher, String query, int offset, int limit) throws Exception {
        super(searcher, query, offset, limit);
    }
}
