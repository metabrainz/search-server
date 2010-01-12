package org.musicbrainz.search.servlet;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.queryParser.QueryParser;
import org.musicbrainz.search.index.RecordingIndex;
import org.musicbrainz.search.index.RecordingIndexField;
import org.musicbrainz.search.servlet.mmd1.TrackMmd1XmlWriter;
import org.musicbrainz.search.servlet.mmd2.RecordingWriter;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.util.ArrayList;


public class RecordingSearch extends SearchServer {

    public RecordingSearch() throws Exception {

        resultsWriter = new RecordingWriter();
        mmd1XmlWriter = new TrackMmd1XmlWriter();
        defaultFields = new ArrayList<String>();
        defaultFields.add(RecordingIndexField.RECORDING.getName());
        analyzer = new PerFieldEntityAnalyzer(RecordingIndexField.class);
    }

    public RecordingSearch(String indexDir) throws Exception {

        this();
        indexSearcher = createIndexSearcherFromFileIndex(indexDir,new RecordingIndex().getFilename());
        this.setLastServerUpdatedDate();
    }


    public RecordingSearch(IndexSearcher searcher) throws Exception {

        this();
        indexSearcher = searcher;
    }

    @Override
    protected QueryParser getParser() {
       return new RecordingQueryParser(defaultFields.get(0), analyzer);
    }


}