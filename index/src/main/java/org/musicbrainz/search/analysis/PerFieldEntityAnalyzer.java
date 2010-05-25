package org.musicbrainz.search.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.musicbrainz.search.index.IndexField;

import java.util.EnumSet;

public class PerFieldEntityAnalyzer extends PerFieldAnalyzerWrapper {

    public PerFieldEntityAnalyzer(Class indexFieldClass) {
        super(new StandardUnaccentAnalyzer());

        for(Object o : EnumSet.allOf(indexFieldClass)) {
            IndexField indexField = (IndexField) o;
            Analyzer analyzer = indexField.getAnalyzer();
            if (analyzer != null) {
                this.addAnalyzer(indexField.getName(), analyzer);
            }
        }
    }

}
