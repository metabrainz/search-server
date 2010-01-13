package org.musicbrainz.search.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Field;
import org.musicbrainz.search.analysis.CaseInsensitiveKeywordAnalyzer;
import org.musicbrainz.search.analysis.StandardUnaccentWithPosGapAnalyzer;
import org.musicbrainz.search.analysis.TitleAnalyzer;

/**
 * Fields created in Lucene Search Index
 */
public enum WorkIndexField implements IndexField {
    ARTIST_ID		    ("arid",			Field.Store.NO,	Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    ARTIST              ("artist",          Field.Store.NO,	Field.Index.ANALYZED, new StandardUnaccentWithPosGapAnalyzer()),
    ARTIST_NAME         ("artistname",		Field.Store.NO,	Field.Index.ANALYZED, new StandardUnaccentWithPosGapAnalyzer()),
    ARTIST_NAMECREDIT   ("creditname",	    Field.Store.NO,	Field.Index.ANALYZED, new StandardUnaccentWithPosGapAnalyzer()),
    ARTIST_CREDIT       ("artistcredit",    Field.Store.YES,    Field.Index.NO),
    WORK_ID		        ("wid",		Field.Store.YES,	Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    WORK			    ("work",		Field.Store.YES,	Field.Index.ANALYZED, new TitleAnalyzer()),
    ISWC		        ("iswc",		Field.Store.YES,	Field.Index.ANALYZED, new CaseInsensitiveKeywordAnalyzer()),
    TYPE		        ("type",		Field.Store.YES,	Field.Index.NOT_ANALYZED, new CaseInsensitiveKeywordAnalyzer()),
    ;

    private String name;
	private Field.Store store;
    private Field.Index index;
    private Analyzer analyzer;

    private WorkIndexField(String name, Field.Store store, Field.Index index) {
        this.name = name;
        this.store = store;
        this.index = index;
    }

     private WorkIndexField(String name, Field.Store store, Field.Index index, Analyzer analyzer) {
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