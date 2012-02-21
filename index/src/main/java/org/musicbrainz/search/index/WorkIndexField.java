package org.musicbrainz.search.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Field;
import org.musicbrainz.search.analysis.CaseInsensitiveKeywordAnalyzer;
import org.musicbrainz.search.analysis.MusicbrainzKeepAccentsAnalyzer;
import org.musicbrainz.search.analysis.MusicbrainzWithPosGapAnalyzer;
import org.musicbrainz.search.analysis.TitleAnalyzer;

/**
 * Fields created in Lucene Search Index
 */
public enum WorkIndexField implements IndexField {
	
    ID		        	("_id",		    Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    ALIAS		        ("alias",		Field.Store.YES, 	Field.Index.ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    ARTIST_ID		    ("arid",			Field.Store.NO,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    ARTIST              ("artist",          Field.Store.NO,	Field.Index.ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    ARTIST_RELATION     ("artistrelation",    Field.Store.YES,    Field.Index.NO),
    COMMENT		        ("comment",		Field.Store.YES,	Field.Index.ANALYZED),
    ISWC		        ("iswc",		Field.Store.YES,	Field.Index.ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    TYPE		        ("type",		Field.Store.YES,	Field.Index.ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    TAG		            ("tag",		    Field.Store.YES,	Field.Index.ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    TAGCOUNT            ("tagcount",	Field.Store.YES,	Field.Index.NO),
    WORK			    ("work",		Field.Store.YES,	Field.Index.ANALYZED, new TitleAnalyzer()),
    WORK_ACCENT         ("workaccent",  Field.Store.NO,	    Field.Index.ANALYZED, new MusicbrainzKeepAccentsAnalyzer()),
    WORK_ID		        ("wid",		    Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
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