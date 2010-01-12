package org.musicbrainz.search.servlet;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.queryParser.QueryParser;
import org.musicbrainz.search.index.ArtistIndex;
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.servlet.mmd1.ArtistMmd1XmlWriter;
import org.musicbrainz.search.servlet.mmd2.ArtistWriter;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.util.ArrayList;


public class ArtistSearch extends SearchServer {

    private ArtistSearch() throws Exception {
        resultsWriter = new ArtistWriter();
        mmd1XmlWriter = new ArtistMmd1XmlWriter();
        defaultFields = new ArrayList<String>();
        defaultFields.add(ArtistIndexField.ARTIST.getName());
        defaultFields.add(ArtistIndexField.ALIAS.getName());
        defaultFields.add(ArtistIndexField.SORTNAME.getName());
        analyzer = new PerFieldEntityAnalyzer(ArtistIndexField.class);
    }


    public ArtistSearch(String indexDir) throws Exception {

        this();
        indexSearcher = createIndexSearcherFromFileIndex(indexDir, new ArtistIndex().getFilename());
        this.setLastServerUpdatedDate();
    }


    public ArtistSearch(IndexSearcher searcher) throws Exception {

        this();
        indexSearcher = searcher;
    }

      @Override
    protected QueryParser getParser() {
       return new ArtistQueryParser(defaultFields.toArray(new String[0]), analyzer);
    }


}
