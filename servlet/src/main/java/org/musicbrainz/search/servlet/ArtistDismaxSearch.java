package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.*;
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.index.RecordingIndexField;

import java.util.HashMap;
import java.util.Map;

public class ArtistDismaxSearch extends ArtistSearch {

    private DismaxSearcher dismaxSearcher;

    protected void initDismaxSearcher() {
        Map<String, DismaxAlias.AliasField> fieldBoosts = new HashMap<String, DismaxAlias.AliasField>(3);
        fieldBoosts.put(ArtistIndexField.ARTIST_ACCENT.getName(), new DismaxAlias.AliasField(false, 1.4f));
        fieldBoosts.put(ArtistIndexField.ARTIST.getName(), new DismaxAlias.AliasField(true, 1.2f));
        fieldBoosts.put(ArtistIndexField.SORTNAME.getName(), new DismaxAlias.AliasField(true, 1.1f));
        fieldBoosts.put(ArtistIndexField.ALIAS.getName(), new DismaxAlias.AliasField(true, 0.9f));
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
    public ArtistDismaxSearch(IndexSearcher searcher) throws Exception {
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
    public ArtistDismaxSearch(IndexSearcher searcher, String query, int offset, int limit) throws Exception {
        super(searcher, query, offset, limit);
        initDismaxSearcher();
    }

    protected Query parseQuery(String userQuery) throws ParseException {
        return dismaxSearcher.parseQuery(userQuery, analyzer);
    }
}
