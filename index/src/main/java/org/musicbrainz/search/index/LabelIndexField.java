package org.musicbrainz.search.index;

import org.apache.lucene.document.Field;

/**
 * Fields created in Lucene Search Index
 */
public enum LabelIndexField implements IndexField {

    LABEL_ID	("laid",		Field.Store.YES,	Field.Index.NOT_ANALYZED),
    LABEL		("label",		Field.Store.YES, 	Field.Index.ANALYZED),
    COUNTRY		("country",		Field.Store.YES, 	Field.Index.ANALYZED),
    CODE		("code",		Field.Store.YES, 	Field.Index.ANALYZED),
    ALIAS		("alias",		Field.Store.NO, 	Field.Index.ANALYZED),
    SORTNAME	("sortname",	Field.Store.YES,	Field.Index.ANALYZED),
    BEGIN		("begin",		Field.Store.YES, 	Field.Index.NOT_ANALYZED),
    END			("end",			Field.Store.YES, 	Field.Index.NOT_ANALYZED),
    COMMENT		("comment",		Field.Store.YES, 	Field.Index.ANALYZED),
    TYPE		("type",		Field.Store.YES, 	Field.Index.NOT_ANALYZED),;

    private String name;
	private Field.Store store;
    private Field.Index index;

    private LabelIndexField(String name, Field.Store store, Field.Index index) {
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