package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.musicbrainz.search.analysis.MusicbrainzSimilarity;
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.servlet.mmd1.ArtistMmd1XmlWriter;
import org.musicbrainz.search.servlet.mmd2.ArtistWriter;

import javax.naming.LimitExceededException;
import java.util.ArrayList;


public class ArtistSearch extends SearchServer {
  private ArtistSearch() throws Exception {
        resultsWriter = new ArtistWriter();
        mmd1XmlWriter = new ArtistMmd1XmlWriter();
        defaultFields = new ArrayList<String>();
        defaultFields.add(ArtistIndexField.ARTIST.getName());
        defaultFields.add(ArtistIndexField.ALIAS.getName());
        defaultFields.add(ArtistIndexField.SORTNAME.getName());
        analyzer = DatabaseIndex.getAnalyzer(ArtistIndexField.class);
    }

    public ArtistSearch(IndexSearcher searcher) throws Exception {
        this();
        indexSearcher = searcher;
        if (indexSearcher != null) {
        	indexSearcher.setSimilarity(new MusicbrainzSimilarity());
        }
    }

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
