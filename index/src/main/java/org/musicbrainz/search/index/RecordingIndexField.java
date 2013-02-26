package org.musicbrainz.search.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.musicbrainz.search.analysis.CaseInsensitiveKeywordAnalyzer;
import org.musicbrainz.search.analysis.MusicbrainzKeepAccentsAnalyzer;
import org.musicbrainz.search.analysis.MusicbrainzWithPosGapAnalyzer;
import org.musicbrainz.search.analysis.TitleWithPosGapAnalyzer;

/**
 * Fields created in Lucene Search Index
 */
public enum RecordingIndexField implements IndexField {

    ID                      ("_id",		            MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    ARTIST_ID		        ("arid",			    MusicBrainzFieldTypes.TEXT_NOT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    ARTIST                  ("artist",              MusicBrainzFieldTypes.TEXT_NOT_STORED_ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    ARTIST_NAME             ("artistname",		    MusicBrainzFieldTypes.TEXT_NOT_STORED_ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    ARTIST_NAMECREDIT       ("creditname",	        MusicBrainzFieldTypes.TEXT_NOT_STORED_ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    ARTIST_CREDIT           ("artistcredit",        MusicBrainzFieldTypes.TEXT_STORED_NOT_INDEXED),
    COMMENT		            ("comment",		        MusicBrainzFieldTypes.TEXT_STORED_ANALYZED),
    COUNTRY			        ("country",		        MusicBrainzFieldTypes.TEXT_STORED_ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    DURATION			    ("dur",			        MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    FORMAT			        ("format",		        MusicBrainzFieldTypes.TEXT_STORED_ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    ISRC    		        ("isrc",		        MusicBrainzFieldTypes.TEXT_STORED_ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    NUM_TRACKS              ("tracks",		        MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    NUM_TRACKS_RELEASE      ("tracksrelease",       MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    POSITION                ("position",		    MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    NUMBER                  ("number",		        MusicBrainzFieldTypes.TEXT_STORED_ANALYZED_NO_NORMS),
    PUID    		        ("puid",		        MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    QUANTIZED_DURATION	    ("qdur",		        MusicBrainzFieldTypes.TEXT_NOT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    RECORDING_ID            ("rid",		            MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    RECORDING               ("recording",		    MusicBrainzFieldTypes.TEXT_NOT_STORED_ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    RECORDING_ACCENT        ("recordingaccent",     MusicBrainzFieldTypes.TEXT_NOT_STORED_ANALYZED, new MusicbrainzKeepAccentsAnalyzer()),
    RECORDING_OUTPUT        ("recordingoutput",     MusicBrainzFieldTypes.TEXT_STORED_NOT_INDEXED),
    RELEASE				    ("release",		        MusicBrainzFieldTypes.TEXT_STORED_ANALYZED, new TitleWithPosGapAnalyzer()),
    RELEASE_DATE	        ("date",		        MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    RELEASE_AC_VA           ("release_ac_va",       MusicBrainzFieldTypes.TEXT_STORED_NOT_INDEXED),
    RELEASE_ID			    ("reid",		        MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    RELEASE_PRIMARY_TYPE    ("primarytype",		    MusicBrainzFieldTypes.TEXT_STORED_ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    RELEASE_SECONDARY_TYPE  ("secondarytype",       MusicBrainzFieldTypes.TEXT_STORED_ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    RELEASE_STATUS          ("status",              MusicBrainzFieldTypes.TEXT_STORED_ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    RELEASE_TYPE            ("type",                MusicBrainzFieldTypes.TEXT_STORED_ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    RELEASEGROUP_ID	        ("rgid",			    MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    SECONDARY_TYPE_OUTPUT   ("secondarytypeoutput", MusicBrainzFieldTypes.TEXT_STORED_NOT_INDEXED),
    TAG		                ("tag",		            MusicBrainzFieldTypes.TEXT_STORED_ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    TAGCOUNT                ("tagcount",	        MusicBrainzFieldTypes.TEXT_STORED_NOT_INDEXED),
    TRACKNUM			    ("tnum",		        MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED, new KeywordAnalyzer()),
    TRACK_ARTIST_CREDIT     ("trackartistcredit",   MusicBrainzFieldTypes.TEXT_STORED_NOT_INDEXED),
    TRACK_OUTPUT            ("trackoutput",		    MusicBrainzFieldTypes.TEXT_STORED_NOT_INDEXED),
    TRACK_DURATION_OUTPUT   ("trackdur",		    MusicBrainzFieldTypes.TEXT_STORED_NOT_INDEXED),
    RECORDING_DURATION_OUTPUT("recordingdur",	    MusicBrainzFieldTypes.TEXT_STORED_NOT_INDEXED),


    ;

    private String name;
    private Analyzer analyzer;
    private FieldType fieldType;

    private RecordingIndexField(String name, FieldType fieldType) {
        this.name = name;
        this.fieldType=fieldType;
    }

    private RecordingIndexField(String name, FieldType fieldType, Analyzer analyzer) {
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
