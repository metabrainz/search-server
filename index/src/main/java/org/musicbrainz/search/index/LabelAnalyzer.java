package org.musicbrainz.search.index;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.musicbrainz.search.analysis.StandardUnaccentAnalyzer;
import org.musicbrainz.search.analysis.StripLeadingZeroAnalyzer;


public class LabelAnalyzer extends PerFieldAnalyzerWrapper {

    public LabelAnalyzer()
    {
        super(new StandardUnaccentAnalyzer());
        addAnalyzer(LabelIndexField.LABEL_ID.getName(),new KeywordAnalyzer());
        addAnalyzer(LabelIndexField.LABEL_GID.getName(),new KeywordAnalyzer());
        addAnalyzer(LabelIndexField.TYPE.getName(),new KeywordAnalyzer());
        addAnalyzer(LabelIndexField.CODE.getName(),new StripLeadingZeroAnalyzer());
    }

}
