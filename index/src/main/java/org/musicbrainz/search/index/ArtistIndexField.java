package org.musicbrainz.search.index;

import org.apache.lucene.document.Field;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.musicbrainz.search.analysis.CaseInsensitiveKeywordAnalyzer;
import org.musicbrainz.search.analysis.StandardUnaccentWithPosGapAnalyzer;

/**
 * Fields created in Lucene Search Index
 */
public enum ArtistIndexField implements IndexField {

    ALIAS		("alias",		Field.Store.YES,	Field.Index.ANALYZED, new StandardUnaccentWithPosGapAnalyzer()),
    ARTIST_ID	("arid",		Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    ARTIST		("artist",		Field.Store.YES,	Field.Index.ANALYZED),
    SORTNAME	("sortname",	Field.Store.YES,	Field.Index.ANALYZED),
    BEGIN		("begin",		Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    END			("end",			Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    COMMENT		("comment",		Field.Store.YES,	Field.Index.ANALYZED),
    TYPE		("type",		Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    GENDER      ("gender",      Field.Store.YES,    Field.Index.NOT_ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    COUNTRY     ("country",     Field.Store.YES,    Field.Index.NOT_ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    TAG		    ("tag",		    Field.Store.YES,	Field.Index.ANALYZED, new StandardUnaccentWithPosGapAnalyzer()),
    TAGCOUNT    ("tagcount",	Field.Store.YES,	Field.Index.NO),


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