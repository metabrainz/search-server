package org.musicbrainz.search.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Field;
import org.musicbrainz.search.analysis.CaseInsensitiveKeywordAnalyzer;
import org.musicbrainz.search.analysis.StandardUnaccentWithPosGapAnalyzer;
import org.musicbrainz.search.analysis.TitleWithPosGapAnalyzer;

/**
 * Fields created in Lucene Search Index
 */
public enum RecordingIndexField implements IndexField {

    RECORDING_ID        ("rid",		Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    RECORDING           ("recording",		Field.Store.NO,	Field.Index.ANALYZED, new StandardUnaccentWithPosGapAnalyzer()),
    ARTIST_ID		    ("arid",			Field.Store.NO,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    ARTIST              ("artist",          Field.Store.NO,	Field.Index.ANALYZED, new StandardUnaccentWithPosGapAnalyzer()),
    ARTIST_NAME         ("artistname",		Field.Store.NO,	Field.Index.ANALYZED, new StandardUnaccentWithPosGapAnalyzer()),
    ARTIST_NAMECREDIT   ("creditname",	    Field.Store.NO,	Field.Index.ANALYZED, new StandardUnaccentWithPosGapAnalyzer()),
    ARTIST_CREDIT       ("artistcredit",    Field.Store.YES,    Field.Index.NO),
    RELEASE_ID			("reid",		Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS),
    RELEASE				("release",		Field.Store.YES,	Field.Index.ANALYZED, new TitleWithPosGapAnalyzer()),
    NUM_TRACKS          ("tracks",		Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    DURATION			("dur",			Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    QUANTIZED_DURATION	("qdur",		Field.Store.NO,	    Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    TRACKNUM			("tnum",		Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    RELEASE_TYPE        ("type",        Field.Store.YES,    Field.Index.ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    RELEASE_STATUS      ("status",      Field.Store.YES,    Field.Index.ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    TRACK_OUTPUT        ("trackoutoutput",		Field.Store.YES,	Field.Index.NO),    
    RECORDING_OUTPUT    ("recordingoutput",		Field.Store.YES,	Field.Index.NO),
    POSITION            ("position",		Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    ISRC    		    ("isrc",		    Field.Store.YES,	Field.Index.ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    NUM_TRACKS_RELEASE  ("tracksrelease",		Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    TAG		            ("tag",		        Field.Store.YES,	Field.Index.ANALYZED, new StandardUnaccentWithPosGapAnalyzer()),
    TAGCOUNT            ("tagcount",	    Field.Store.YES,	Field.Index.NO),
    PUID    		    ("puid",		    Field.Store.YES,	Field.Index.NO),
    RELEASE_DATE	    ("date",		    Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),

    ;

    private String name;
	private Field.Store store;
    private Field.Index index;
    private Analyzer analyzer;
        
    private RecordingIndexField(String name, Field.Store store, Field.Index index) {
        this.name = name;
        this.store = store;
        this.index = index;
    }


     private RecordingIndexField(String name, Field.Store store, Field.Index index, Analyzer analyzer) {
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
