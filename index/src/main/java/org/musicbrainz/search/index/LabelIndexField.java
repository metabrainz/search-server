package org.musicbrainz.search.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Field;
import org.musicbrainz.search.analysis.CaseInsensitiveKeywordAnalyzer;

/**
 * Fields created in Lucene Search Index
 */
public enum LabelIndexField implements IndexField {

    ENTITY_TYPE  ("_type",     Field.Store.YES,    Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    ENTITY_GID	 ("_gid",      Field.Store.YES,    Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    LABEL	 ("label",     Field.Store.NO,     Field.Index.ANALYZED),
    SORTNAME     ("sortname",  Field.Store.NO,     Field.Index.ANALYZED),
    ALIAS        ("alias",     Field.Store.NO,     Field.Index.ANALYZED),
    COUNTRY	 ("country",   Field.Store.NO,     Field.Index.ANALYZED),
    CODE	 ("code",      Field.Store.NO,     Field.Index.ANALYZED),
    BEGIN	 ("begin",     Field.Store.NO,     Field.Index.NOT_ANALYZED),
    END		 ("end",       Field.Store.NO,     Field.Index.NOT_ANALYZED),
    COMMENT	 ("comment",   Field.Store.NO,     Field.Index.ANALYZED),
    TYPE	 ("type",      Field.Store.NO,     Field.Index.ANALYZED,  new CaseInsensitiveKeywordAnalyzer()),
    ;

    private String name;
    private Field.Store store;
    private Field.Index index;
    private Analyzer analyzer;

    private LabelIndexField(String name, Field.Store store, Field.Index index) {
        this.name = name;
        this.store = store;
        this.index = index;
    }
    
    private LabelIndexField(String name, Field.Store store, Field.Index index, Analyzer analyzer) {
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

    @Override
    public String toString() {
        return name;
    }
    
}