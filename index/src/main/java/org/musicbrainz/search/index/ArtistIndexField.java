package org.musicbrainz.search.index;

import org.apache.lucene.document.Field;

/**
 * Fields created in Lucene Search Index
 */
public enum ArtistIndexField implements IndexField {

    ENTITY_TYPE         ("_type",       Field.Store.YES,   Field.Index.NOT_ANALYZED),
    ENTITY_GID          ("_gid",        Field.Store.YES,   Field.Index.NOT_ANALYZED),
    ARTIST              ("artist",      Field.Store.NO,    Field.Index.ANALYZED),
    SORTNAME            ("sortname",    Field.Store.NO,    Field.Index.ANALYZED),
    ALIAS               ("alias",       Field.Store.NO,    Field.Index.ANALYZED),
    BEGIN               ("begin",       Field.Store.NO,    Field.Index.NOT_ANALYZED),
    END                 ("end",         Field.Store.NO,    Field.Index.NOT_ANALYZED),
    GENDER              ("gender",      Field.Store.NO,    Field.Index.NOT_ANALYZED),
    COUNTRY             ("country",     Field.Store.NO,    Field.Index.NOT_ANALYZED),
    COMMENT             ("comment",     Field.Store.NO,    Field.Index.ANALYZED),
    TYPE                ("type",        Field.Store.NO,    Field.Index.NOT_ANALYZED),
    ;

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

    @Override
    public String toString() {
        return name;
    }
}