package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.musicbrainz.search.index.ReleaseGroupIndexField;

import java.util.ArrayList;

public class ReleaseGroupDismaxSearch extends ReleaseGroupSearch {

    protected void setupDefaultFields() {
        defaultFields       = new ArrayList<String>();
        defaultFields.add(ReleaseGroupIndexField.RELEASEGROUP.getName());
        defaultFields.add(ReleaseGroupIndexField.RELEASE.getName());
        defaultFields.add(ReleaseGroupIndexField.ARTIST.getName());
        defaultFields.add(ReleaseGroupIndexField.ARTIST_NAMECREDIT.getName());
    }

    /**
     * Standard Search
     *
     * @param searcher
     * @throws Exception
     */
    public ReleaseGroupDismaxSearch(IndexSearcher searcher) throws Exception {
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
    public ReleaseGroupDismaxSearch(IndexSearcher searcher, String query, int offset, int limit) throws Exception {
        super(searcher, query, offset, limit);
    }
}
