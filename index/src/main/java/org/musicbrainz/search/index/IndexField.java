package org.musicbrainz.search.index;

import org.apache.lucene.document.Field;
import org.apache.lucene.analysis.Analyzer;

/**
 * Fields created in Lucene Search Index
 */
public interface IndexField {

    public String getName();
    public Field.Store getStore();
	public Field.Index getIndex();
    public Analyzer getAnalyzer();

}