package org.musicbrainz.search.servlet;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherManager;
import org.musicbrainz.search.index.CDStubIndexField;

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
  public CDStubDismaxSearch(SearcherManager searcherManager) throws Exception {
    super(searcherManager);
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
  public CDStubDismaxSearch(SearcherManager searcherManager, String query, int offset, int limit) throws Exception {
    super(searcherManager, query, offset, limit);
        initDismaxSearcher();
    }

    protected Query parseQuery(String userQuery) throws ParseException {
        return dismaxSearcher.parseQuery(userQuery, analyzer);
    }
}
