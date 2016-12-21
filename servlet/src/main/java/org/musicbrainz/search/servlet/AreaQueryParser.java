package org.musicbrainz.search.servlet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;

/**
 * Subclasses QueryParser to handle numeric fields that we might want wish to do range queries for and handle type
 * searches specified using integers.
 */
public class AreaQueryParser extends MultiFieldQueryParser
{

    public AreaQueryParser(java.lang.String[] strings, org.apache.lucene.analysis.Analyzer analyzer)
    {
        super(strings, analyzer);
    }
}
