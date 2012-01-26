package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.musicbrainz.search.index.ReleaseGroupIndexField;

import java.util.HashMap;
import java.util.Map;

public class ReleaseGroupDismaxSearch extends ReleaseGroupSearch {

    private DismaxSearcher dismaxSearcher;

    protected void initDismaxSearcher() {
        Map<String, Float> fieldBoosts = new HashMap<String, Float>(4);
        fieldBoosts.put(ReleaseGroupIndexField.RELEASEGROUP.getName(), 1.4f);
        fieldBoosts.put(ReleaseGroupIndexField.RELEASE.getName(), 1.2f);
        fieldBoosts.put(ReleaseGroupIndexField.ARTIST.getName(),null);
        fieldBoosts.put(ReleaseGroupIndexField.ARTIST_NAMECREDIT.getName(), null);
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
    public ReleaseGroupDismaxSearch(IndexSearcher searcher) throws Exception {
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
    public ReleaseGroupDismaxSearch(IndexSearcher searcher, String query, int offset, int limit) throws Exception {
        super(searcher, query, offset, limit);
        initDismaxSearcher();
    }

    protected Query parseQuery(String query) throws ParseException
    {
        return dismaxSearcher.parseQuery(query, analyzer);
    }


}
