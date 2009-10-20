package org.musicbrainz.search.index;

import org.apache.lucene.document.Field;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.musicbrainz.search.analysis.CaseInsensitiveKeywordAnalyzer;
import org.musicbrainz.search.analysis.StripLeadingZeroAnalyzer;

/**
 * Fields created in Lucene Search Index
 */
public enum ReleaseIndexField implements IndexField {

    ARTIST_ID		("arid",		Field.Store.YES,	Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    ARTIST			("artist",		Field.Store.YES,	Field.Index.ANALYZED),
    ARTIST_SORTNAME	("sortname",	Field.Store.YES,	Field.Index.ANALYZED),
    RELEASE_ID		("reid",		Field.Store.YES,	Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    RELEASE			("release",		Field.Store.YES,	Field.Index.ANALYZED),
    NUM_TRACKS		("tracks",		Field.Store.YES,	Field.Index.ANALYZED),
    BARCODE			("barcode",		Field.Store.YES,	Field.Index.ANALYZED, new StripLeadingZeroAnalyzer()),
    CATALOG_NO		("catno",		Field.Store.YES,	Field.Index.ANALYZED, new CaseInsensitiveKeywordAnalyzer()),
    LABEL			("label",		Field.Store.YES,	Field.Index.ANALYZED),
    DATE			("date",		Field.Store.YES,	Field.Index.NOT_ANALYZED),
    COUNTRY			("country",		Field.Store.YES,	Field.Index.NOT_ANALYZED, new CaseInsensitiveKeywordAnalyzer()),
    NUM_DISC_IDS	("discids",		Field.Store.YES,	Field.Index.ANALYZED),
    AMAZON_ID		("asin",		Field.Store.YES,	Field.Index.ANALYZED, new CaseInsensitiveKeywordAnalyzer()),
    SCRIPT			("script",		Field.Store.YES,	Field.Index.ANALYZED, new CaseInsensitiveKeywordAnalyzer()),
    LANGUAGE		("lang",	    Field.Store.YES,	Field.Index.NOT_ANALYZED, new CaseInsensitiveKeywordAnalyzer()),
    TYPE		    ("type",		Field.Store.YES,	Field.Index.NOT_ANALYZED, new CaseInsensitiveKeywordAnalyzer()),
    STATUS		    ("status",		Field.Store.YES,	Field.Index.ANALYZED, new CaseInsensitiveKeywordAnalyzer()),
    ARTIST_COMMENT  ("comment",		Field.Store.YES,    Field.Index.ANALYZED),
    FORMAT  		("format",		Field.Store.YES,	Field.Index.ANALYZED),
    ARTIST_V1   	("artistv1",	Field.Store.YES,	Field.Index.ANALYZED),
    ;

    private String name;
	private Field.Store store;
    private Field.Index index;
    private Analyzer analyzer;
        
    private ReleaseIndexField(String name, Field.Store store, Field.Index index) {
        this.name = name;
        this.store = store;
        this.index = index;
    }

     private ReleaseIndexField(String name, Field.Store store, Field.Index index, Analyzer analyzer) {
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