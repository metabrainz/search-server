package org.musicbrainz.search.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Field;
import org.musicbrainz.search.analysis.CaseInsensitiveKeywordAnalyzer;
import org.musicbrainz.search.analysis.TitleAnalyzer;

/**
 * Fields created in Lucene Search Index
 */
public enum RecordingIndexField implements IndexField {

    RECORDING_ID        ("rid",		Field.Store.YES,	Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    RECORDING           ("recording",		Field.Store.YES,	Field.Index.ANALYZED),
    ARTIST_ID		    ("arid",			Field.Store.NO,	Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    ARTIST              ("artist",          Field.Store.NO,	Field.Index.ANALYZED),
    ARTIST_NAME         ("artistname",		Field.Store.NO,	Field.Index.ANALYZED),
    ARTIST_NAMECREDIT   ("creditname",	    Field.Store.NO,	Field.Index.ANALYZED),
    ARTIST_CREDIT       ("artistcredit",    Field.Store.YES,    Field.Index.NO),
    RELEASE_ID			("reid",		Field.Store.YES,	Field.Index.NOT_ANALYZED),
    RELEASE				("release",		Field.Store.YES,	Field.Index.ANALYZED, new TitleAnalyzer()),
    NUM_TRACKS_MEDIUM   ("tracksmedium",		Field.Store.YES,	Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    NUM_TRACKS          ("tracks",		Field.Store.YES,	Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    DURATION			("dur",			Field.Store.YES,	Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    QUANTIZED_DURATION	("qdur",		Field.Store.NO,	    Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    TRACKNUM			("tnum",		Field.Store.YES,	Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    RELEASE_TYPE        ("type",        Field.Store.YES,    Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    ARTIST_COMMENT      ("comment",		Field.Store.YES,    Field.Index.NO),
    TRACK_OUTPUT        ("trackoutoutput",		Field.Store.YES,	Field.Index.NO),    
    RECORDING_OUTPUT    ("recordingoutput",		Field.Store.YES,	Field.Index.NO),
    MEDIUM_POS_OUTPUT   ("mediumposoutput",		Field.Store.YES,	Field.Index.NO),
    ISRC    		    ("isrc",		    Field.Store.YES,	Field.Index.ANALYZED, new CaseInsensitiveKeywordAnalyzer());

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
