package org.musicbrainz.search.servlet;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.queryParser.QueryParser;
import org.musicbrainz.search.index.TrackIndexField;
import org.musicbrainz.search.index.TrackAnalyzer;

import java.util.ArrayList;


public class TrackSearch extends SearchServer {

    public TrackSearch() throws Exception {


        xmlWriter = new TrackXmlWriter();
        htmlWriter = new TrackHtmlWriter();
        defaultFields = new ArrayList<String>();
        defaultFields.add(TrackIndexField.TRACK.getName());
        analyzer = new TrackAnalyzer();
    }

    public TrackSearch(String indexDir) throws Exception {

        this();
        indexSearcher = createIndexSearcherFromFileIndex(indexDir,"track_index");
        this.setLastServerUpdatedDate();
    }


    public TrackSearch(IndexSearcher searcher) throws Exception {

        this();
        indexSearcher = searcher;
    }

    @Override
    protected QueryParser getParser() {
       return new TrackQueryParser(defaultFields.get(0), analyzer);
    }


}