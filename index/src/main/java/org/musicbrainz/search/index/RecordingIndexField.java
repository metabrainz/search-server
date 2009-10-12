package org.musicbrainz.search.index;

import org.apache.lucene.document.Field;

/**
 * Fields created in Lucene Search Index
 */
public enum RecordingIndexField implements IndexField {
    
    ENTITY_TYPE           ("_type",       Field.Store.YES,   Field.Index.NOT_ANALYZED),
    ENTITY_GID            ("_gid",        Field.Store.YES,   Field.Index.NOT_ANALYZED),
    RECORDING             ("recording",   Field.Store.NO,    Field.Index.ANALYZED),
    ARTIST                ("artist",      Field.Store.NO,    Field.Index.ANALYZED),    
    TRACK                 ("track",       Field.Store.NO,    Field.Index.ANALYZED),
    COMMENT               ("comment",     Field.Store.NO,    Field.Index.ANALYZED),
    DURATION              ("dur",         Field.Store.NO,    Field.Index.NOT_ANALYZED),
    QUANTIZED_DURATION    ("qdur",        Field.Store.NO,    Field.Index.NOT_ANALYZED),
    ;

    private String name;
    private Field.Store store;
    private Field.Index index;

    private RecordingIndexField(String name, Field.Store store, Field.Index index) {
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
