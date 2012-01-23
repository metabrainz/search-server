package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.musicbrainz.search.index.RecordingIndexField;

import java.util.ArrayList;

public class RecordingDismaxSearch extends RecordingSearch {

    protected void setupDefaultFields() {
        defaultFields       = new ArrayList<String>();
        defaultFields.add(RecordingIndexField.RECORDING.getName());
        defaultFields.add(RecordingIndexField.RELEASE.getName());
        defaultFields.add(RecordingIndexField.ARTIST.getName());
        defaultFields.add(RecordingIndexField.ARTIST_NAMECREDIT.getName());
    }

    /**
     * Standard Search
     *
     * @param searcher
     * @throws Exception
     */
    public RecordingDismaxSearch(IndexSearcher searcher) throws Exception {
        super(searcher);
    }

    protected Query parseQuery(String query) throws ParseException
    {
        //Treat all as text
        query=QueryParser.escape(query);
        QueryParser parser = getParser();
        return parser.parse(query);
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
    }
}
