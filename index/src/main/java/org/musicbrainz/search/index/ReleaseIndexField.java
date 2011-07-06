package org.musicbrainz.search.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Field;
import org.musicbrainz.search.analysis.CaseInsensitiveKeywordAnalyzer;
import org.musicbrainz.search.analysis.StandardUnaccentWithPosGapAnalyzer;
import org.musicbrainz.search.analysis.StripLeadingZeroAnalyzer;
import org.musicbrainz.search.analysis.TitleAnalyzer;

/**
 * Fields created in Lucene Search Index
 */
public enum ReleaseIndexField implements IndexField {
	
    ID				    ("_id",		    	Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),	
    ARTIST_ID		    ("arid",			Field.Store.NO,	    Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    ARTIST              ("artist",          Field.Store.NO,	    Field.Index.ANALYZED, new StandardUnaccentWithPosGapAnalyzer()),
    ARTIST_NAME         ("artistname",		Field.Store.NO,	    Field.Index.ANALYZED, new StandardUnaccentWithPosGapAnalyzer()),
    ARTIST_NAMECREDIT   ("creditname",	    Field.Store.NO,	    Field.Index.ANALYZED, new StandardUnaccentWithPosGapAnalyzer()),
    ARTIST_CREDIT       ("artistcredit",    Field.Store.YES,    Field.Index.NO),
    RELEASE_ID		    ("reid",		    Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    RELEASE			    ("release",		    Field.Store.YES,	Field.Index.ANALYZED, new TitleAnalyzer()),
    RELEASEGROUP_ID	    ("rgid",			Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
	NUM_TRACKS		    ("tracks",		    Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    NUM_TRACKS_MEDIUM   ("tracksmedium",	Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    NUM_MEDIUMS         ("mediums",	        Field.Store.NO,	    Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    BARCODE			    ("barcode",		    Field.Store.YES,	Field.Index.ANALYZED_NO_NORMS, new StripLeadingZeroAnalyzer()),
    CATALOG_NO		    ("catno",		    Field.Store.YES,	Field.Index.ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    LABEL			    ("label",		    Field.Store.YES,	Field.Index.ANALYZED, new StandardUnaccentWithPosGapAnalyzer()),
    LABEL_ID            ("laid",            Field.Store.YES,    Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    DATE			    ("date",		    Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    COUNTRY			    ("country",		    Field.Store.YES,	Field.Index.ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    NUM_DISCIDS         ("discids",		    Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    NUM_DISCIDS_MEDIUM  ("discidsmedium",   Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    AMAZON_ID		    ("asin",		    Field.Store.YES,	Field.Index.ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    SCRIPT			    ("script",		    Field.Store.YES,	Field.Index.ANALYZED, new CaseInsensitiveKeywordAnalyzer()),
    LANGUAGE		    ("lang",	        Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    TYPE		        ("type",		    Field.Store.YES,	Field.Index.ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    STATUS		        ("status",		    Field.Store.YES,	Field.Index.ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    FORMAT  		    ("format",		    Field.Store.YES,	Field.Index.ANALYZED, new CaseInsensitiveKeywordAnalyzer()),
    PUID    		    ("puid",		    Field.Store.NO,	    Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    COMMENT		        ("comment",		    Field.Store.YES,	Field.Index.ANALYZED),

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