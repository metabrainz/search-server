package org.musicbrainz.search.index;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.musicbrainz.search.analysis.StandardUnaccentAnalyzer;


public class CDStubAnalyzer extends PerFieldAnalyzerWrapper {

    public CDStubAnalyzer()
    {
        super(new StandardUnaccentAnalyzer());
        addAnalyzer(CDStubIndexField.NUM_TRACKS.getName(), new KeywordAnalyzer());
        addAnalyzer(CDStubIndexField.DISCID.getName(), new KeywordAnalyzer());

    }

}
