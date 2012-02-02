package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.index.ReleaseIndexField;

import java.util.HashMap;
import java.util.Map;

public class ReleaseDismaxSearch extends ReleaseSearch {

    private DismaxSearcher dismaxSearcher;

    protected void initDismaxSearcher() {
        Map<String, Float> fieldBoosts = new HashMap<String, Float>(3);
        fieldBoosts.put(ReleaseIndexField.RELEASE.getName(), 1.2f);
        fieldBoosts.put(ReleaseIndexField.BARCODE.getName(), 1.2f);
        fieldBoosts.put(ReleaseIndexField.CATALOG_NO.getName(), 1.2f);
        fieldBoosts.put(ReleaseIndexField.ARTIST.getName(),1f);
        fieldBoosts.put(ReleaseIndexField.ARTIST_NAMECREDIT.getName(), 1f);
        fieldBoosts.put(ReleaseIndexField.LABEL.getName(), 0.8f);

        DismaxQueryParser.DismaxAlias dismaxAlias = new DismaxQueryParser.DismaxAlias();
        dismaxAlias.setFields(fieldBoosts);
        dismaxAlias.setTie(0.1f);
        dismaxSearcher = new ReleaseDismaxSearcher(dismaxAlias);
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

    protected Query parseQuery(String userQuery) throws ParseException
    {
        Query q1 = dismaxSearcher.parseQuery(userQuery, analyzer);
        Query q2 = new BoostExactMatchQuery(q1, userQuery, ReleaseIndexField.RELEASE.getName());
        return q2;
    }
}
