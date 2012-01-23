package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.musicbrainz.search.analysis.MusicbrainzSimilarity;
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.servlet.mmd1.ArtistMmd1XmlWriter;
import org.musicbrainz.search.servlet.mmd2.ArtistWriter;

import java.util.ArrayList;


public class ArtistSearch extends SearchServer {

    protected void setupDefaultFields() {
        defaultFields = new ArrayList<String>();
        defaultFields.add(ArtistIndexField.ARTIST.getName());
        defaultFields.add(ArtistIndexField.ALIAS.getName());
        defaultFields.add(ArtistIndexField.SORTNAME.getName());
    }


    private ArtistSearch() throws Exception {
        resultsWriter = new ArtistWriter();
        mmd1XmlWriter = new ArtistMmd1XmlWriter();
        setupDefaultFields();
        analyzer = DatabaseIndex.getAnalyzer(ArtistIndexField.class);
    }

    /**
     * Standard Search
     *
     * @param searcher
     * @throws Exception
     */
    public ArtistSearch(IndexSearcher searcher) throws Exception {
        this();
        indexSearcher = searcher;
        if (indexSearcher != null) {
        	indexSearcher.setSimilarity(new MusicbrainzSimilarity());
        }
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
    public ArtistSearch(IndexSearcher searcher, String query, int offset, int limit) throws Exception {
        this(searcher);
        this.query=query;
        this.offset=offset;
        this.limit=limit;
    }

      @Override
    protected QueryParser getParser() {
       return new ArtistQueryParser(defaultFields.toArray(new String[0]), analyzer);
    }


}
