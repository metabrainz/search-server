package org.musicbrainz.search.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Field;
import org.musicbrainz.search.analysis.*;

/**
 * Fields created in Lucene Search Index
 */
public enum ReleaseIndexField implements IndexField {
	
    ID				    ("_id",		    	Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    AMAZON_ID		    ("asin",		    Field.Store.YES,	Field.Index.ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    ARTIST_ID		    ("arid",			Field.Store.NO,	    Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    ARTIST              ("artist",          Field.Store.NO,	    Field.Index.ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    ARTIST_CREDIT       ("artistcredit",    Field.Store.YES,    Field.Index.NO),
    ARTIST_NAME         ("artistname",		Field.Store.NO,	    Field.Index.ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    ARTIST_NAMECREDIT   ("creditname",	    Field.Store.NO,	    Field.Index.ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    BARCODE			    ("barcode",		    Field.Store.YES,	Field.Index.ANALYZED_NO_NORMS, new StripLeadingZeroAnalyzer()),
    CATALOG_NO		    ("catno",		    Field.Store.YES,	Field.Index.ANALYZED_NO_NORMS, new StripSpacesAnalyzer()),
    COMMENT		        ("comment",		    Field.Store.YES,	Field.Index.ANALYZED),
    COUNTRY			    ("country",		    Field.Store.YES,	Field.Index.ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    DATE			    ("date",		    Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    FORMAT  		    ("format",		    Field.Store.YES,	Field.Index.ANALYZED, new CaseInsensitiveKeywordAnalyzer()),
    LABEL			    ("label",		    Field.Store.YES,	Field.Index.ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    LABEL_ID            ("laid",            Field.Store.YES,    Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    LANGUAGE		    ("lang",	        Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    NUM_DISCIDS         ("discids",		    Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    NUM_DISCIDS_MEDIUM  ("discidsmedium",   Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    NUM_MEDIUMS         ("mediums",	        Field.Store.NO,	    Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    NUM_TRACKS		    ("tracks",		    Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    NUM_TRACKS_MEDIUM   ("tracksmedium",	Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    PRIMARY_TYPE        ("primarytype",		Field.Store.YES,	Field.Index.ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    PUID    		    ("puid",		    Field.Store.NO,	    Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    RELEASE			    ("release",		    Field.Store.YES,	Field.Index.ANALYZED, new TitleAnalyzer()),
    RELEASE_ACCENT      ("releaseaccent",   Field.Store.NO,	    Field.Index.ANALYZED, new MusicbrainzKeepAccentsAnalyzer()),
    RELEASE_ID		    ("reid",		    Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    RELEASEGROUP_ID	    ("rgid",			Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    SCRIPT			    ("script",		    Field.Store.YES,	Field.Index.ANALYZED, new CaseInsensitiveKeywordAnalyzer()),
    SECONDARY_TYPE      ("secondarytype",   Field.Store.YES,	Field.Index.ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    STATUS		        ("status",		    Field.Store.YES,	Field.Index.ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    TYPE		        ("type",		    Field.Store.YES,	Field.Index.ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
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


}