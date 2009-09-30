package org.musicbrainz.search.index;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.musicbrainz.search.analysis.StandardUnaccentAnalyzer;


public class ArtistAnalyzer extends PerFieldAnalyzerWrapper {

    public ArtistAnalyzer() {
        super(new StandardUnaccentAnalyzer());
        addAnalyzer(ArtistIndexField.ARTIST_ID.getName(), new KeywordAnalyzer());
        addAnalyzer(ArtistIndexField.BEGIN.getName(), new KeywordAnalyzer());
        addAnalyzer(ArtistIndexField.END.getName(), new KeywordAnalyzer());
        addAnalyzer(ArtistIndexField.TYPE.getName(), new KeywordAnalyzer());

    }

}
