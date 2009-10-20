package org.musicbrainz.search.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Field;
import org.musicbrainz.search.analysis.CaseInsensitiveKeywordAnalyzer;
import org.musicbrainz.search.analysis.StripLeadingZeroAnalyzer;

/**
 * Fields created in Lucene Search Index
 */
public enum ReleaseIndexField implements IndexField {

    ENTITY_TYPE     ("_type",       Field.Store.YES,    Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    ENTITY_GID      ("_gid",        Field.Store.YES,    Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    ARTIST          ("artist",      Field.Store.NO,     Field.Index.ANALYZED),
    RELEASE         ("release",     Field.Store.NO,     Field.Index.ANALYZED),
    
    DATE            ("date",        Field.Store.NO,     Field.Index.NOT_ANALYZED),
    COUNTRY         ("country",     Field.Store.NO,     Field.Index.NOT_ANALYZED, new CaseInsensitiveKeywordAnalyzer()),
    BARCODE         ("barcode",     Field.Store.NO,     Field.Index.ANALYZED, new StripLeadingZeroAnalyzer()),
    LABEL           ("label",       Field.Store.NO,     Field.Index.ANALYZED),
    CATALOG_NO      ("catno",       Field.Store.NO,     Field.Index.ANALYZED, new CaseInsensitiveKeywordAnalyzer()),
    
    TYPE            ("type",        Field.Store.NO,     Field.Index.ANALYZED, new CaseInsensitiveKeywordAnalyzer()),
    SCRIPT          ("script",      Field.Store.NO,     Field.Index.NOT_ANALYZED, new CaseInsensitiveKeywordAnalyzer()),
    LANGUAGE        ("lang",        Field.Store.NO,     Field.Index.NOT_ANALYZED, new CaseInsensitiveKeywordAnalyzer()),
    STATUS          ("status",      Field.Store.NO,     Field.Index.ANALYZED, new CaseInsensitiveKeywordAnalyzer()),
    PACKAGING       ("packaging",   Field.Store.NO,     Field.Index.ANALYZED, new CaseInsensitiveKeywordAnalyzer()),
    COMMENT         ("comment",     Field.Store.NO,     Field.Index.ANALYZED),
    
    FORMAT          ("format",      Field.Store.NO,     Field.Index.ANALYZED, new CaseInsensitiveKeywordAnalyzer()),
    ASIN            ("asin",        Field.Store.NO,     Field.Index.NOT_ANALYZED, new CaseInsensitiveKeywordAnalyzer()),
    MEDIUM          ("medium",      Field.Store.NO,     Field.Index.NOT_ANALYZED),
    //TODO: useless? keep?
    NUM_DISC_IDS    ("discids",     Field.Store.NO,     Field.Index.ANALYZED),
    NUM_TRACKS      ("tracks",      Field.Store.NO,     Field.Index.ANALYZED),
    ;

    private String name;
    private Field.Store store;
    private Field.Index index;
    private Analyzer analyzer;

    private ReleaseIndexField(String name, Field.Store store, Field.Index index) {
        this.name = name;
        this.store = store;
        this.index = index;
    }

    private ReleaseIndexField(String name, Field.Store store, Field.Index index, Analyzer analyzer) {
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