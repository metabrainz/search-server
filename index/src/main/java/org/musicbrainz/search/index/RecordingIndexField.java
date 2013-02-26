package org.musicbrainz.search.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Field;
import org.musicbrainz.search.analysis.CaseInsensitiveKeywordAnalyzer;
import org.musicbrainz.search.analysis.MusicbrainzKeepAccentsAnalyzer;
import org.musicbrainz.search.analysis.MusicbrainzWithPosGapAnalyzer;
import org.musicbrainz.search.analysis.TitleWithPosGapAnalyzer;

/**
 * Fields created in Lucene Search Index
 */
public enum RecordingIndexField implements IndexField {

    ID                      ("_id",		            Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    ARTIST_ID		        ("arid",			    Field.Store.NO,	    Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    ARTIST                  ("artist",              Field.Store.NO,	    Field.Index.ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    ARTIST_NAME             ("artistname",		    Field.Store.NO,	    Field.Index.ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    ARTIST_NAMECREDIT       ("creditname",	        Field.Store.NO,	    Field.Index.ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    ARTIST_CREDIT           ("artistcredit",        Field.Store.YES,    Field.Index.NO),
    COMMENT		            ("comment",		        Field.Store.YES,	Field.Index.ANALYZED),
    COUNTRY			        ("country",		        Field.Store.YES,	Field.Index.ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    DURATION			    ("dur",			        Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    FORMAT			        ("format",		        Field.Store.YES,	Field.Index.ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    ISRC    		        ("isrc",		        Field.Store.YES,	Field.Index.ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    NUM_TRACKS              ("tracks",		        Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    NUM_TRACKS_RELEASE      ("tracksrelease",       Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    POSITION                ("position",		    Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    NUMBER                  ("number",		        Field.Store.YES,	Field.Index.ANALYZED_NO_NORMS),
    PUID    		        ("puid",		        Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    QUANTIZED_DURATION	    ("qdur",		        Field.Store.NO,	    Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    RECORDING_ID            ("rid",		            Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    RECORDING               ("recording",		    Field.Store.NO,	    Field.Index.ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    RECORDING_ACCENT        ("recordingaccent",     Field.Store.NO,	    Field.Index.ANALYZED, new MusicbrainzKeepAccentsAnalyzer()),
    RECORDING_OUTPUT        ("recordingoutput",     Field.Store.YES,	Field.Index.NO),
    RELEASE				    ("release",		        Field.Store.YES,	Field.Index.ANALYZED, new TitleWithPosGapAnalyzer()),
    RELEASE_DATE	        ("date",		        Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    RELEASE_AC_VA           ("release_ac_va",       Field.Store.YES,    Field.Index.NO),
    RELEASE_ID			    ("reid",		        Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    RELEASE_PRIMARY_TYPE    ("primarytype",		    Field.Store.YES,	Field.Index.ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    RELEASE_SECONDARY_TYPE  ("secondarytype",       Field.Store.YES,	Field.Index.ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    RELEASE_STATUS          ("status",              Field.Store.YES,    Field.Index.ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    RELEASE_TYPE            ("type",                Field.Store.YES,    Field.Index.ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    RELEASEGROUP_ID	        ("rgid",			    Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    SECONDARY_TYPE_OUTPUT   ("secondarytypeoutput", Field.Store.YES,	Field.Index.NO),
    TAG		                ("tag",		            Field.Store.YES,	Field.Index.ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    TAGCOUNT                ("tagcount",	        Field.Store.YES,	Field.Index.NO),
    TRACKNUM			    ("tnum",		        Field.Store.YES,	Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    TRACK_ARTIST_CREDIT     ("trackartistcredit",   Field.Store.YES,   Field.Index.NO),
    TRACK_OUTPUT            ("trackoutput",		    Field.Store.YES,	Field.Index.NO),
    TRACK_DURATION_OUTPUT   ("trackdur",		    Field.Store.YES,	Field.Index.NO),
    RECORDING_DURATION_OUTPUT("recordingdur",	    Field.Store.YES,	Field.Index.NO),


    ;

    private String name;
	private Field.Store store;
    private Field.Index index;
    private Analyzer analyzer;

    private RecordingIndexField(String name, Field.Store store, Field.Index index) {
        this.name = name;
        this.store = store;
        this.index = index;
    }


     private RecordingIndexField(String name, Field.Store store, Field.Index index, Analyzer analyzer) {
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


}
