package org.musicbrainz.search.servlet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;
import org.musicbrainz.search.LuceneVersion;


public class WorkQueryParser extends MultiFieldQueryParser {

    public WorkQueryParser(java.lang.String[] strings, org.apache.lucene.analysis.Analyzer analyzer)
    {
        super(LuceneVersion.LUCENE_VERSION, strings, analyzer);
    }
}