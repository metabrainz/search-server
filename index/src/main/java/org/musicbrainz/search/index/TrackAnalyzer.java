package org.musicbrainz.search.index;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.musicbrainz.search.analysis.StandardUnaccentAnalyzer;


public class TrackAnalyzer extends PerFieldAnalyzerWrapper {

    public TrackAnalyzer()
    {
        super(new StandardUnaccentAnalyzer());
        addAnalyzer(TrackIndexField.TRACK_ID.getName(), new KeywordAnalyzer());
        addAnalyzer(TrackIndexField.ARTIST_ID.getName(), new KeywordAnalyzer());
        addAnalyzer(TrackIndexField.RELEASE_ID.getName(), new KeywordAnalyzer());
        addAnalyzer(TrackIndexField.NUM_TRACKS.getName(), new KeywordAnalyzer());
        addAnalyzer(TrackIndexField.DURATION.getName(), new KeywordAnalyzer());
        addAnalyzer(TrackIndexField.QUANTIZED_DURATION.getName(), new KeywordAnalyzer());
        addAnalyzer(TrackIndexField.TRACKNUM.getName(), new KeywordAnalyzer());
        addAnalyzer(TrackIndexField.RELEASE_TYPE.getName(), new KeywordAnalyzer());
    }

}
