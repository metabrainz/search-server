package org.musicbrainz.search.index;

import org.apache.lucene.document.Field;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.musicbrainz.search.analysis.CaseInsensitiveKeywordAnalyzer;

/**
 * Fields created in Lucene Search Index
 */
public enum ArtistIndexField implements IndexField {

    ALIAS		("alias",		Field.Store.NO,		Field.Index.ANALYZED),
    ARTIST_ID	("arid",		Field.Store.YES,	Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    ARTIST		("artist",		Field.Store.YES,	Field.Index.ANALYZED),
    SORTNAME	("sortname",	Field.Store.YES,	Field.Index.ANALYZED),
    BEGIN		("begin",		Field.Store.YES,	Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    END			("end",			Field.Store.YES,	Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    COMMENT		("comment",		Field.Store.YES,	Field.Index.ANALYZED),
    TYPE		("type",		Field.Store.YES,	Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    GENDER      ("gender",      Field.Store.YES,    Field.Index.NOT_ANALYZED, new CaseInsensitiveKeywordAnalyzer()),
    COUNTRY     ("country",     Field.Store.YES,    Field.Index.NOT_ANALYZED, new CaseInsensitiveKeywordAnalyzer()),


    ;
    private String name;
	private Field.Store store;
    private Field.Index index;
    private Analyzer analyzer;

    private ArtistIndexField(String name, Field.Store store, Field.Index index) {
        this.name = name;
        this.store = store;
        this.index = index;
    }

    private ArtistIndexField(String name, Field.Store store, Field.Index index, Analyzer analyzer) {
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