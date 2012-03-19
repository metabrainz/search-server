package org.musicbrainz.search.servlet;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.musicbrainz.search.analysis.RecordingSimilarity;
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.RecordingIndexField;
import org.musicbrainz.search.servlet.mmd1.TrackMmd1XmlWriter;
import org.musicbrainz.search.servlet.mmd2.RecordingWriter;

import java.io.IOException;
import java.util.ArrayList;


public class RecordingSearch extends SearchServer {

    protected void setupDefaultFields() {
        defaultFields = new ArrayList<String>();
        defaultFields.add(RecordingIndexField.RECORDING.getName());
    }

    public RecordingSearch() throws Exception {
    
        resultsWriter = new RecordingWriter();
        mmd1XmlWriter = new TrackMmd1XmlWriter();
        setupDefaultFields();
        analyzer = DatabaseIndex.getAnalyzer(RecordingIndexField.class);
    }

    public RecordingSearch(IndexSearcher searcher) throws Exception {
        this();
        indexSearcher = searcher;
        if (indexSearcher != null) {
            indexSearcher.setSimilarity(new RecordingSimilarity());
        }
    }

    public RecordingSearch(IndexSearcher searcher, String query, int offset, int limit) throws Exception {
        this(searcher);
        this.query=query;
        this.offset=offset;
        this.limit=limit;
    }

    @Override
    protected QueryParser getParser() {
       return new RecordingQueryParser(defaultFields.toArray(new String[0]), analyzer);
    }

    @Override
    protected  String printExplainHeader(Document doc)
            throws IOException, ParseException {
        return doc.get(RecordingIndexField.RECORDING_ID.getName()) +':'
                + doc.get(RecordingIndexField.RECORDING_OUTPUT.getName())
                + '\n';
    }


}