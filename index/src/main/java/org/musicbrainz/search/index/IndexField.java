package org.musicbrainz.search.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;

/**
 * Fields created in Lucene Search Index
 */
public interface IndexField {

    public String getName();
    public Analyzer getAnalyzer();
    public FieldType getFieldType();


}