package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.musicbrainz.search.index.AnnotationIndexField;
import org.musicbrainz.search.index.CDStubIndexField;

import java.util.HashMap;
import java.util.Map;

public class CDStubDismaxSearch extends CDStubSearch {

    private DismaxSearcher dismaxSearcher;

    protected void initDismaxSearcher() {
        Map<String, DismaxAlias.AliasField> fieldBoosts = new HashMap<String, DismaxAlias.AliasField>(2);
        fieldBoosts.put(CDStubIndexField.TITLE.getName(), new DismaxAlias.AliasField(true, 1.3f));
        fieldBoosts.put(CDStubIndexField.ARTIST.getName(), new DismaxAlias.AliasField(true, 1f));
        fieldBoosts.put(CDStubIndexField.COMMENT.getName(), new DismaxAlias.AliasField(false, 0.8f));
        fieldBoosts.put(CDStubIndexField.BARCODE.getName(), new DismaxAlias.AliasField(false, 0.8f));
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
    public CDStubDismaxSearch(IndexSearcher searcher) throws Exception {
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
    public CDStubDismaxSearch(IndexSearcher searcher, String query, int offset, int limit) throws Exception {
        super(searcher, query, offset, limit);
        initDismaxSearcher();
    }

    protected Query parseQuery(String userQuery) throws ParseException {
        Query q1 = dismaxSearcher.parseQuery(userQuery, analyzer);
        Query q2 = new BoostExactMatchQuery(q1, userQuery, CDStubIndexField.TITLE.getName());
        return q2;
    }
}
