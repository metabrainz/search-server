package org.musicbrainz.search.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.musicbrainz.search.analysis.*;

/**
 * Fields created in Lucene Search Index
 */
public enum ReleaseIndexField implements IndexField {
	
    ID				    ("_id",		    	MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    AMAZON_ID		    ("asin",		    MusicBrainzFieldTypes.TEXT_STORED_ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    ARTIST_ID		    ("arid",			MusicBrainzFieldTypes.TEXT_NOT_STORED_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    ARTIST              ("artist",          MusicBrainzFieldTypes.TEXT_NOT_STORED_ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    ARTIST_CREDIT       ("artistcredit",    MusicBrainzFieldTypes.TEXT_STORED_NOT_INDEXED),
    ARTIST_NAME         ("artistname",		MusicBrainzFieldTypes.TEXT_NOT_STORED_ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    ARTIST_NAMECREDIT   ("creditname",	    MusicBrainzFieldTypes.TEXT_NOT_STORED_ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    BARCODE			    ("barcode",		    MusicBrainzFieldTypes.TEXT_STORED_ANALYZED_NO_NORMS, new StripLeadingZeroAnalyzer()),
    CATALOG_NO		    ("catno",		    MusicBrainzFieldTypes.TEXT_STORED_ANALYZED_NO_NORMS, new StripSpacesAnalyzer()),
    COMMENT		        ("comment",		    MusicBrainzFieldTypes.TEXT_STORED_ANALYZED),
    COUNTRY			    ("country",		    MusicBrainzFieldTypes.TEXT_STORED_ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    DATE			    ("date",		    MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    FORMAT  		    ("format",		    MusicBrainzFieldTypes.TEXT_STORED_ANALYZED, new CaseInsensitiveKeywordAnalyzer()),
    LABEL			    ("label",		    MusicBrainzFieldTypes.TEXT_STORED_ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    LABEL_ID            ("laid",            MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    LANGUAGE		    ("lang",	        MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    NUM_DISCIDS         ("discids",		    MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    NUM_DISCIDS_MEDIUM  ("discidsmedium",   MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    NUM_MEDIUMS         ("mediums",	        MusicBrainzFieldTypes.TEXT_NOT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    NUM_TRACKS		    ("tracks",		    MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    NUM_TRACKS_MEDIUM   ("tracksmedium",	MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    PRIMARY_TYPE        ("primarytype",		MusicBrainzFieldTypes.TEXT_STORED_ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    PUID    		    ("puid",		    MusicBrainzFieldTypes.TEXT_NOT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    RELEASE			    ("release",		    MusicBrainzFieldTypes.TEXT_STORED_ANALYZED, new TitleAnalyzer()),
    RELEASE_ACCENT      ("releaseaccent",   MusicBrainzFieldTypes.TEXT_NOT_STORED_ANALYZED, new MusicbrainzKeepAccentsAnalyzer()),
    RELEASE_ID		    ("reid",		    MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    RELEASEGROUP_ID	    ("rgid",			MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    SCRIPT			    ("script",		    MusicBrainzFieldTypes.TEXT_STORED_ANALYZED, new CaseInsensitiveKeywordAnalyzer()),
    SECONDARY_TYPE      ("secondarytype",   MusicBrainzFieldTypes.TEXT_STORED_ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    STATUS		        ("status",		    MusicBrainzFieldTypes.TEXT_STORED_ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    TYPE		        ("type",		    MusicBrainzFieldTypes.TEXT_STORED_ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    ;

    private String name;
    private Analyzer analyzer;

    private FieldType fieldType;

    private ReleaseIndexField(String name, FieldType fieldType) {
        this.name = name;
        this.fieldType=fieldType;
    }

    private ReleaseIndexField(String name, FieldType fieldType, Analyzer analyzer) {
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