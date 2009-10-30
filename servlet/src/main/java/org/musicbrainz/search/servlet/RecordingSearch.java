package org.musicbrainz.search.servlet;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.queryParser.QueryParser;
import org.musicbrainz.search.index.RecordingIndexField;
import org.musicbrainz.search.servlet.mmd1.TrackMmd1XmlWriter;
import org.musicbrainz.search.servlet.mmd2.RecordingXmlWriter;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.util.ArrayList;


public class RecordingSearch extends SearchServer {

    public RecordingSearch() throws Exception {

        xmlWriter = new RecordingXmlWriter();
        mmd1XmlWriter = new TrackMmd1XmlWriter();
        htmlWriter = new TrackHtmlWriter();
        defaultFields = new ArrayList<String>();
        defaultFields.add(RecordingIndexField.RECORDING.getName());
        analyzer = new PerFieldEntityAnalyzer(RecordingIndexField.class);
    }

    public RecordingSearch(String indexDir) throws Exception {

        this();
        indexSearcher = createIndexSearcherFromFileIndex(indexDir,"recording_index");
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