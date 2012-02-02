package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.musicbrainz.search.index.ReleaseGroupIndexField;
import org.musicbrainz.search.index.WorkIndexField;

import java.util.HashMap;
import java.util.Map;

public class ReleaseGroupDismaxSearch extends ReleaseGroupSearch {

    private DismaxSearcher dismaxSearcher;

    protected void initDismaxSearcher() {

        Map<String, DismaxAlias.AliasField> fieldBoosts = new HashMap<String, DismaxAlias.AliasField>(4);
        fieldBoosts.put(ReleaseGroupIndexField.RELEASEGROUP.getName(), new DismaxAlias.AliasField(true, 1.4f));
        fieldBoosts.put(ReleaseGroupIndexField.RELEASE.getName(), new DismaxAlias.AliasField(true, 1.2f));
        fieldBoosts.put(ReleaseGroupIndexField.ARTIST.getName(), new DismaxAlias.AliasField(true, 1f));
        fieldBoosts.put(ReleaseGroupIndexField.ARTIST_NAMECREDIT.getName(), new DismaxAlias.AliasField(true, 1f));
        DismaxAlias dismaxAlias = new DismaxAlias();
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

    protected Query parseQuery(String userQuery) throws ParseException {
        Query q1 = dismaxSearcher.parseQuery(userQuery, analyzer);
        Query q2 = new BoostExactMatchQuery(q1, userQuery, ReleaseGroupIndexField.RELEASEGROUP.getName());
        return q2;
    }
}
