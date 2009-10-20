package org.musicbrainz.search.index;

import org.apache.lucene.document.Field;

/**
 * Fields created in Lucene Search Index
 */
public enum ReleaseIndexField implements IndexField {

    ARTIST_ID		("arid",		Field.Store.YES,	Field.Index.NOT_ANALYZED),
    ARTIST			("artist",		Field.Store.YES,	Field.Index.ANALYZED),
    ARTIST_SORTNAME	("sortname",	Field.Store.YES,	Field.Index.ANALYZED),
    RELEASE_ID		("reid",		Field.Store.YES,	Field.Index.NOT_ANALYZED),
    RELEASE			("release",		Field.Store.YES,	Field.Index.ANALYZED),
    NUM_TRACKS		("tracks",		Field.Store.YES,	Field.Index.ANALYZED),
    BARCODE			("barcode",		Field.Store.YES,	Field.Index.ANALYZED),
    CATALOG_NO		("catno",		Field.Store.YES,	Field.Index.ANALYZED),
    LABEL			("label",		Field.Store.YES,	Field.Index.ANALYZED),
    DATE			("date",		Field.Store.YES,	Field.Index.NOT_ANALYZED),
    COUNTRY			("country",		Field.Store.YES,	Field.Index.NOT_ANALYZED),
    NUM_DISC_IDS	("discids",		Field.Store.YES,	Field.Index.ANALYZED),
    AMAZON_ID		("asin",		Field.Store.YES,	Field.Index.ANALYZED),
    SCRIPT			("script",		Field.Store.YES,	Field.Index.ANALYZED),
    LANGUAGE		("lang",	    Field.Store.YES,	Field.Index.NOT_ANALYZED),
    TYPE		    ("type",		Field.Store.YES,	Field.Index.NOT_ANALYZED),
    STATUS		    ("status",		Field.Store.YES,	Field.Index.ANALYZED),
    ARTIST_COMMENT  ("comment",		Field.Store.YES,    Field.Index.ANALYZED),
    FORMAT  		("format",		Field.Store.YES,	Field.Index.ANALYZED),
    ARTIST_V1   	("artistv1",	Field.Store.YES,	Field.Index.ANALYZED),
    ;

    private String name;
	private Field.Store store;
    private Field.Index index;

    private ReleaseIndexField(String name, Field.Store store, Field.Index index) {
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