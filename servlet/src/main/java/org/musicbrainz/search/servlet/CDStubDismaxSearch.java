package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.musicbrainz.search.index.CDStubIndexField;

import java.util.HashMap;
import java.util.Map;

public class CDStubDismaxSearch extends CDStubSearch {

    private DismaxSearcher dismaxSearcher;

    protected void initDismaxSearcher() {
        Map<String, Float> fieldBoosts = new HashMap<String, Float>(4);
        fieldBoosts.put(CDStubIndexField.TITLE.getName(),1.3f);
        fieldBoosts.put(CDStubIndexField.ARTIST.getName(), null);
        fieldBoosts.put(CDStubIndexField.COMMENT.getName(), 0.8f);
        fieldBoosts.put(CDStubIndexField.BARCODE.getName(), 0.8f);
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

    protected Query parseQuery(String query) throws ParseException
    {
        return dismaxSearcher.parseQuery(query, analyzer);
    }
}
