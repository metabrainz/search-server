package org.musicbrainz.search.index;

import org.apache.lucene.document.Field;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;

/**
 * Fields created in Lucene Search Index
 */
public enum RecordingIndexField implements IndexField {

    RECORDING_ID        ("rid",		Field.Store.YES,	Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    RECORDING           ("recording",		Field.Store.YES,	Field.Index.ANALYZED),
    ARTIST_ID			("arid",		Field.Store.YES,	Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    ARTIST				("artist",		Field.Store.YES,	Field.Index.ANALYZED),
    ARTIST_NAME         ("artist_name",	Field.Store.YES,	Field.Index.ANALYZED),
    ARTIST_NAMECREDIT   ("creditname",	    Field.Store.YES,	Field.Index.ANALYZED),
    ARTIST_JOINPHRASE	("joinphrase",	    Field.Store.YES,	Field.Index.NO),       //Never Searched
    ARTIST_SORTNAME	    ("sortname",	Field.Store.YES,	Field.Index.ANALYZED, new KeywordAnalyzer()),
    RELEASE_ID			("reid",		Field.Store.YES,	Field.Index.NOT_ANALYZED),
    RELEASE				("release",		Field.Store.YES,	Field.Index.ANALYZED),
    NUM_TRACKS			("tracks",		Field.Store.YES,	Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    DURATION			("dur",			Field.Store.YES,	Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    QUANTIZED_DURATION	("qdur",		Field.Store.YES,	Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    TRACKNUM			("tnum",		Field.Store.YES,	Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    RELEASE_TYPE        ("type",        Field.Store.YES,    Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    ARTIST_COMMENT      ("comment",		Field.Store.YES,    Field.Index.ANALYZED),
    TRACK_OUTPUT        ("trackoutoutput",		Field.Store.YES,	Field.Index.NO),    
    RECORDING_OUTPUT    ("recordinungoutput",		Field.Store.YES,	Field.Index.NO),
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
