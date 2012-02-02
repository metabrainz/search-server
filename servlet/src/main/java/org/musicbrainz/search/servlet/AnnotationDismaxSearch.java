package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.musicbrainz.search.index.AnnotationIndexField;
import org.musicbrainz.search.index.LabelIndexField;

import java.util.HashMap;
import java.util.Map;

public class AnnotationDismaxSearch extends AnnotationSearch {

    private DismaxSearcher dismaxSearcher;

    protected void initDismaxSearcher() {
        Map<String, DismaxAlias.AliasField> fieldBoosts = new HashMap<String, DismaxAlias.AliasField>(2);
        fieldBoosts.put(AnnotationIndexField.NAME.getName(), new DismaxAlias.AliasField(true, 1f));
        fieldBoosts.put(AnnotationIndexField.TEXT.getName(), new DismaxAlias.AliasField(true, 1f));
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
    public AnnotationDismaxSearch(IndexSearcher searcher) throws Exception {
        super(searcher);
        initDismaxSearcher();
    }

    protected Query parseQuery(String userQuery) throws ParseException {
        Query q1 = dismaxSearcher.parseQuery(userQuery, analyzer);
        Query q2 = new BoostExactMatchQuery(q1, userQuery, AnnotationIndexField.NAME.getName());
        return q2;
    }
}
