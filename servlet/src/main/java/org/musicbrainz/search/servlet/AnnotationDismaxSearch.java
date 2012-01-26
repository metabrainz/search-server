package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.musicbrainz.search.index.AnnotationIndexField;
import org.musicbrainz.search.index.ArtistIndexField;

import java.util.HashMap;
import java.util.Map;

public class AnnotationDismaxSearch extends AnnotationSearch {

    private DismaxSearcher dismaxSearcher;

    protected void initDismaxSearcher()
    {

        Map<String, Float> fieldBoosts = new HashMap<String, Float>(2);
        fieldBoosts.put(AnnotationIndexField.NAME.getName(),null);
        fieldBoosts.put(AnnotationIndexField.TEXT.getName(),null);
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
    public AnnotationDismaxSearch(IndexSearcher searcher) throws Exception {
        super(searcher);
        initDismaxSearcher();
    }

    protected Query parseQuery(String query) throws ParseException
    {
        return dismaxSearcher.parseQuery(query, analyzer);
    }
}
