package org.musicbrainz.search.servlet;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.NIOFSDirectory;
import org.musicbrainz.search.index.TrackIndexField;
import org.musicbrainz.search.index.ReleaseAnalyzer;
import org.musicbrainz.search.index.TrackAnalyzer;

import java.io.File;
import java.util.ArrayList;


public class TrackSearch extends SearchServer {

    public TrackSearch() throws Exception {


        xmlWriter = new TrackXmlWriter();
        htmlWriter = new TrackHtmlWriter();
        queryMangler = new TrackMangler();
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

}