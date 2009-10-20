package org.musicbrainz.search.index;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.musicbrainz.search.analysis.StandardUnaccentAnalyzer;
import org.musicbrainz.search.analysis.CaseInsensitiveKeywordAnalyzer;


public class ReleaseGroupAnalyzer extends PerFieldAnalyzerWrapper {

    public ReleaseGroupAnalyzer()
    {
        super(new StandardUnaccentAnalyzer());
        addAnalyzer(ReleaseGroupIndexField.RELEASEGROUP_ID.getName(), new KeywordAnalyzer());
        addAnalyzer(ReleaseGroupIndexField.ARTIST_ID.getName(), new KeywordAnalyzer());
        addAnalyzer(ReleaseGroupIndexField.TYPE.getName(),new KeywordAnalyzer());
    }

}