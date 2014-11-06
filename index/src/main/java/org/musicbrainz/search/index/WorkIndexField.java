package org.musicbrainz.search.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.musicbrainz.search.analysis.CaseInsensitiveKeywordAnalyzer;
import org.musicbrainz.search.analysis.MusicbrainzKeepAccentsAnalyzer;
import org.musicbrainz.search.analysis.MusicbrainzWithPosGapAnalyzer;
import org.musicbrainz.search.analysis.TitleAnalyzer;

/**
 * Fields created in Lucene Search Index
 */
public enum WorkIndexField implements IndexField {
	
    ID		        	("_id",		        MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    ALIAS		        ("alias",		    MusicBrainzFieldTypes.TEXT_NOT_STORED_ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    ARTIST_ID		    ("arid",			MusicBrainzFieldTypes.TEXT_NOT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    ARTIST              ("artist",          MusicBrainzFieldTypes.TEXT_NOT_STORED_ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    COMMENT		        ("comment",		    MusicBrainzFieldTypes.TEXT_STORED_ANALYZED),
    ISWC		        ("iswc",		    MusicBrainzFieldTypes.TEXT_STORED_ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    LYRICS_LANG		    ("lang",	        MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    TYPE		        ("type",		    MusicBrainzFieldTypes.TEXT_STORED_ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    TAG		            ("tag",		        MusicBrainzFieldTypes.TEXT_STORED_ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    WORK			    ("work",		    MusicBrainzFieldTypes.TEXT_STORED_ANALYZED, new TitleAnalyzer()),
    WORK_ACCENT         ("workaccent",      MusicBrainzFieldTypes.TEXT_NOT_STORED_ANALYZED, new MusicbrainzKeepAccentsAnalyzer()),
    WORK_ID		        ("wid",		        MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    WORK_STORE		    ("workstore",       MusicBrainzFieldTypes.TEXT_STORED_NOT_INDEXED),
    ;

    private String name;
    private Analyzer analyzer;
    private FieldType fieldType;

    private WorkIndexField(String name, FieldType fieldType) {
        this.name = name;
        this.fieldType=fieldType;
    }

    private WorkIndexField(String name, FieldType fieldType, Analyzer analyzer) {
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