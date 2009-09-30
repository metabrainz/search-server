package org.musicbrainz.search.index;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.musicbrainz.search.analysis.StandardUnaccentAnalyzer;


public class AnnotationAnalyzer extends PerFieldAnalyzerWrapper {

    public AnnotationAnalyzer() {
        super(new StandardUnaccentAnalyzer());
        addAnalyzer(AnnotationIndexField.MBID.getName(), new KeywordAnalyzer());
        addAnalyzer(AnnotationIndexField.TYPE.getName(), new KeywordAnalyzer());

    }

}
