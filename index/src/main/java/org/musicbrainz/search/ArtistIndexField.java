package org.musicbrainz.search;

import org.apache.lucene.document.Field;

/**
 * Fields created in Lucene Search Index
 */
public enum ArtistIndexField implements IndexField {
	
    ALIAS		("alias",		Field.Store.NO,		Field.Index.ANALYZED),
    ARTIST_ID	("arid",		Field.Store.YES,	Field.Index.NOT_ANALYZED),
    ARTIST		("artist",		Field.Store.YES,	Field.Index.ANALYZED),
    SORTNAME	("sortname",	Field.Store.YES,	Field.Index.ANALYZED),
    BEGIN		("begin",		Field.Store.YES,	Field.Index.NOT_ANALYZED),
    END			("end",			Field.Store.YES,	Field.Index.NOT_ANALYZED),
    COMMENT		("comment",		Field.Store.YES,	Field.Index.ANALYZED),
    TYPE		("type",		Field.Store.YES,	Field.Index.NOT_ANALYZED),;

    private String name;
	private Field.Store store;
    private Field.Index index;

    private ArtistIndexField(String name, Field.Store store, Field.Index index) {
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