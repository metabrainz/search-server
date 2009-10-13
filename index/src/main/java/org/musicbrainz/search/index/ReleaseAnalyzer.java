package org.musicbrainz.search.index;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.musicbrainz.search.analysis.StandardUnaccentAnalyzer;
import org.musicbrainz.search.analysis.CaseInsensitiveKeywordAnalyzer;
import org.musicbrainz.search.analysis.StripLeadingZeroAnalyzer;


public class ReleaseAnalyzer extends PerFieldAnalyzerWrapper {

    public ReleaseAnalyzer()
    {
        super(new StandardUnaccentAnalyzer());
        addAnalyzer(ReleaseIndexField.ARTIST_ID.getName(),new KeywordAnalyzer());
        addAnalyzer(ReleaseIndexField.RELEASE_ID.getName(),new KeywordAnalyzer());
        addAnalyzer(ReleaseIndexField.AMAZON_ID.getName(),new CaseInsensitiveKeywordAnalyzer());
        addAnalyzer(ReleaseIndexField.COUNTRY.getName(),new CaseInsensitiveKeywordAnalyzer());
        addAnalyzer(ReleaseIndexField.SCRIPT.getName(),new CaseInsensitiveKeywordAnalyzer());
        addAnalyzer(ReleaseIndexField.LANGUAGE.getName(),new CaseInsensitiveKeywordAnalyzer());
        addAnalyzer(ReleaseIndexField.CATALOG_NO.getName(),new CaseInsensitiveKeywordAnalyzer());
        addAnalyzer(ReleaseIndexField.TYPE.getName(),new CaseInsensitiveKeywordAnalyzer());
        addAnalyzer(ReleaseIndexField.BARCODE.getName(),new StripLeadingZeroAnalyzer());
        addAnalyzer(ReleaseIndexField.STATUS.getName(),new CaseInsensitiveKeywordAnalyzer());
    }

}