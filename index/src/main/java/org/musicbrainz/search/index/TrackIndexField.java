package org.musicbrainz.search.index;

import org.apache.lucene.document.Field;

/**
 * Fields created in Lucene Search Index
 */
public enum TrackIndexField implements IndexField {

    TRACK_ID			("trid",		Field.Store.YES,	Field.Index.NOT_ANALYZED),
    TRACK				("track",		Field.Store.YES,	Field.Index.ANALYZED),
    ARTIST_ID			("arid",		Field.Store.YES,	Field.Index.NOT_ANALYZED),
    ARTIST				("artist",		Field.Store.YES,	Field.Index.ANALYZED),
    RELEASE_ID			("reid",		Field.Store.YES,	Field.Index.NOT_ANALYZED),
    RELEASE				("release",		Field.Store.YES,	Field.Index.ANALYZED),
    NUM_TRACKS			("tracks",		Field.Store.YES,	Field.Index.NOT_ANALYZED),
    DURATION			("dur",			Field.Store.YES,	Field.Index.NOT_ANALYZED),
    QUANTIZED_DURATION	("qdur",		Field.Store.YES,	Field.Index.NOT_ANALYZED),
    TRACKNUM			("tnum",		Field.Store.YES,	Field.Index.NOT_ANALYZED),
    RELEASE_TYPE        ("type",        Field.Store.YES,    Field.Index.NOT_ANALYZED),
    ARTIST_COMMENT      ("comment",		Field.Store.YES,    Field.Index.ANALYZED),;

    private String name;
	private Field.Store store;
    private Field.Index index;

    private TrackIndexField(String name, Field.Store store, Field.Index index) {
        this.name = name;
        this.store = store;
        this.index = index;
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

}
