package org.musicbrainz.search.index;

import org.apache.lucene.document.Field;

/**
 * Fields created in Lucene Search Index
 */
public enum LabelIndexField implements IndexField {

    ENTITY_TYPE ("_type",     Field.Store.YES,      Field.Index.NOT_ANALYZED),
    ENTITY_GID	("_gid",      Field.Store.YES,      Field.Index.NOT_ANALYZED),
    LABEL	("label",     Field.Store.NO,       Field.Index.ANALYZED),
    SORTNAME    ("sortname",  Field.Store.NO,       Field.Index.ANALYZED),
    ALIAS       ("alias",     Field.Store.NO,       Field.Index.ANALYZED),
    COUNTRY	("country",   Field.Store.NO,       Field.Index.ANALYZED),
    CODE	("code",      Field.Store.NO,       Field.Index.ANALYZED),
    BEGIN	("begin",     Field.Store.NO,       Field.Index.NOT_ANALYZED),
    END		("end",       Field.Store.NO,       Field.Index.NOT_ANALYZED),
    COMMENT	("comment",   Field.Store.NO,       Field.Index.ANALYZED),
    // TODO: Check if this field should really be analyzed 
    TYPE	("type",      Field.Store.NO,       Field.Index.ANALYZED),
    ;

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

    @Override
    public String toString() {
        return name;
    }
    
}