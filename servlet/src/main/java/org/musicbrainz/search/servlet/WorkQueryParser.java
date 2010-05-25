package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.musicbrainz.search.LuceneVersion;


public class WorkQueryParser extends MultiFieldQueryParser {

    public WorkQueryParser(java.lang.String[] strings, org.apache.lucene.analysis.Analyzer analyzer)
    {
        super(LuceneVersion.LUCENE_VERSION, strings, analyzer);
    }
}