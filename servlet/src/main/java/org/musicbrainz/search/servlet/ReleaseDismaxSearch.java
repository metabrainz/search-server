package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.musicbrainz.search.index.ReleaseIndexField;

import java.util.HashMap;
import java.util.Map;

public class ReleaseDismaxSearch extends ReleaseSearch {

    private DismaxSearcher dismaxSearcher;

    protected void initDismaxSearcher() {
        Map<String, Float> fieldBoosts = new HashMap<String, Float>(3);
        fieldBoosts.put(ReleaseIndexField.RELEASE.getName(), 1.2f);
        fieldBoosts.put(ReleaseIndexField.ARTIST.getName(),null);
        fieldBoosts.put(ReleaseIndexField.ARTIST_NAMECREDIT.getName(), null);
        fieldBoosts.put(ReleaseIndexField.LABEL.getName(), 0.8f);
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
    public ReleaseDismaxSearch(IndexSearcher searcher) throws Exception {
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
    public ReleaseDismaxSearch(IndexSearcher searcher, String query, int offset, int limit) throws Exception {
        super(searcher, query, offset, limit);
        initDismaxSearcher();
    }

    protected Query parseQuery(String query) throws ParseException {
        return dismaxSearcher.parseQuery(query, analyzer);
    }
}
