package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.index.ReleaseGroupIndexField;
import org.musicbrainz.search.index.WorkIndexField;

import java.util.HashMap;
import java.util.Map;

public class WorkDismaxSearch extends WorkSearch {

    private DismaxSearcher dismaxSearcher;

    protected void initDismaxSearcher()
    {
        Map<String, Float> fieldBoosts = new HashMap<String, Float>(3);
        fieldBoosts.put(WorkIndexField.WORK.getName(), 1.3f);
        fieldBoosts.put(WorkIndexField.ALIAS.getName(), 0.9f);
        DismaxQueryParser.DismaxAlias dismaxAlias = new DismaxQueryParser.DismaxAlias();
        dismaxAlias.setFields(fieldBoosts);
        dismaxAlias.setTie(0.1f);
        dismaxSearcher = new DismaxSearcher(dismaxAlias);
    }

    /**
     * Standard Search
     *
     * @param searcher
     * @throws Exception
     */
    public WorkDismaxSearch(IndexSearcher searcher) throws Exception {
        super(searcher);
        initDismaxSearcher();
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
    public WorkDismaxSearch(IndexSearcher searcher, String query, int offset, int limit) throws Exception {
        super(searcher, query, offset, limit);
        initDismaxSearcher();
    }

    protected Query parseQuery(String userQuery) throws ParseException
    {
        Query q1 = dismaxSearcher.parseQuery(userQuery, analyzer);
        Query q2 = new BoostExactMatchQuery(q1, userQuery, WorkIndexField.WORK.getName());
        return q2;
    }
}
