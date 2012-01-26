package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.musicbrainz.search.index.RecordingIndexField;

import java.util.HashMap;
import java.util.Map;

public class RecordingDismaxSearch extends RecordingSearch {

    private DismaxSearcher dismaxSearcher;

    protected void initDismaxSearcher() {
        Map<String, Float> fieldBoosts = new HashMap<String, Float>(3);
        fieldBoosts.put(RecordingIndexField.RECORDING.getName(), 1.2f);
        fieldBoosts.put(RecordingIndexField.RELEASE.getName(),null);
        fieldBoosts.put(RecordingIndexField.ARTIST_NAMECREDIT.getName(), 0.8f);
        fieldBoosts.put(RecordingIndexField.ARTIST.getName(), 0.8f);
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
    public RecordingDismaxSearch(IndexSearcher searcher) throws Exception {
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
    public RecordingDismaxSearch(IndexSearcher searcher, String query, int offset, int limit) throws Exception {
        super(searcher, query, offset, limit);
        initDismaxSearcher();
    }

    protected Query parseQuery(String query) throws ParseException
    {
        return dismaxSearcher.parseQuery(query, analyzer);
    }


}
