package org.musicbrainz.search.index;

import org.apache.lucene.document.Field;

/**
 * Fields created in Lucene Search Index
 */
public interface IndexField {

    public String getName();
    public Field.Store getStore();
	public Field.Index getIndex();

}