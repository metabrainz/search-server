package org.musicbrainz.search.servlet;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherManager;
import org.musicbrainz.search.index.AnnotationIndexField;

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
  public AnnotationDismaxSearch(SearcherManager searcherManager) throws Exception {
    super(searcherManager);
        initDismaxSearcher();
    }

    protected Query parseQuery(String userQuery) throws ParseException {
        return dismaxSearcher.parseQuery(userQuery, analyzer);
    }
}
