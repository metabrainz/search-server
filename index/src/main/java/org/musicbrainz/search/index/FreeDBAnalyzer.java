package org.musicbrainz.search.index;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.musicbrainz.search.analysis.StandardUnaccentAnalyzer;


public class FreeDBAnalyzer extends PerFieldAnalyzerWrapper {

    public FreeDBAnalyzer()
    {
        super(new StandardUnaccentAnalyzer());
        addAnalyzer(FreeDBIndexField.DISCID.getName(), new KeywordAnalyzer());
        addAnalyzer(FreeDBIndexField.CATEGORY.getName(), new KeywordAnalyzer());

    }

}