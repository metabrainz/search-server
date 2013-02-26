package org.musicbrainz.search.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Field;
import org.musicbrainz.search.analysis.MusicbrainzAnalyzer;

/**
 * Fields created in Lucene Search Index
 */
public enum TagIndexField implements IndexField {
	
	ID		("_id",		Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    TAG		("tag",		Field.Store.YES,	Field.Index.ANALYZED, new MusicbrainzAnalyzer()),
    ;

    private String name;
	private Field.Store store;
    private Field.Index index;
    private Analyzer analyzer;

    private TagIndexField(String name, Field.Store store, Field.Index index) {
        this.name = name;
        this.store = store;
        this.index = index;
    }

     private TagIndexField(String name, Field.Store store, Field.Index index, Analyzer analyzer) {
        this(name, store, index);
        this.analyzer = analyzer;
    }

    public String getName() {
        return name;
    }

    public Field.Store getStore() {
		return store;
	}

	public Field.Index getIndex() {
		return index;
	}

    public Analyzer getAnalyzer() {
        return analyzer;
    }


}