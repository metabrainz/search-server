package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.index.CDStubIndexField;
import org.musicbrainz.search.index.ReleaseIndexField;

import java.util.HashMap;
import java.util.Map;

public class ReleaseDismaxSearch extends ReleaseSearch {

    private DismaxSearcher dismaxSearcher;

    protected void initDismaxSearcher() {
        Map<String, DismaxAlias.AliasField> fieldBoosts = new HashMap<String, DismaxAlias.AliasField>(6);
        fieldBoosts.put(ReleaseIndexField.RELEASE_ACCENT.getName(), new DismaxAlias.AliasField(false, 1.4f));
        fieldBoosts.put(ReleaseIndexField.RELEASE.getName(), new DismaxAlias.AliasField(true, 1.2f));
        fieldBoosts.put(ReleaseIndexField.BARCODE.getName(), new DismaxAlias.AliasField(false, 1.2f));
        fieldBoosts.put(ReleaseIndexField.CATALOG_NO.getName(), new DismaxAlias.AliasField(false, 1.2f));
        fieldBoosts.put(ReleaseIndexField.ARTIST.getName(), new DismaxAlias.AliasField(true, 1f));
        fieldBoosts.put(ReleaseIndexField.ARTIST_NAMECREDIT.getName(), new DismaxAlias.AliasField(true, 1f));
        fieldBoosts.put(ReleaseIndexField.LABEL.getName(), new DismaxAlias.AliasField(true, 0.8f));

        DismaxAlias dismaxAlias = new DismaxAlias();
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

    protected Query parseQuery(String userQuery) throws ParseException {
        return dismaxSearcher.parseQuery(userQuery, analyzer);
    }
}
