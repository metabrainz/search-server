package org.musicbrainz.search.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.musicbrainz.search.analysis.MusicbrainzAnalyzer;

/**
 * Fields created in Lucene Search Index
 */
public enum TagIndexField implements IndexField {
	
	ID		("_id",		MusicBrainzFieldTypes.TEXT_STORED_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    TAG		("tag",		MusicBrainzFieldTypes.TEXT_STORED_ANALYZED, new MusicbrainzAnalyzer()),
    ;

    private String name;
    private Analyzer analyzer;
    private FieldType fieldType;

    private TagIndexField(String name, FieldType fieldType) {
        this.name = name;
        this.fieldType=fieldType;
    }

    private TagIndexField(String name, FieldType fieldType, Analyzer analyzer) {
        this(name, fieldType);
        this.analyzer = analyzer;
    }

    public String getName() {
        return name;
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }

    public FieldType getFieldType()
    {
        return fieldType;
    }


}