package org.musicbrainz.search.servlet;

import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;


public class WorkQueryParser extends MultiFieldQueryParser
{

    public WorkQueryParser(java.lang.String[] strings, org.apache.lucene.analysis.Analyzer analyzer)
    {
        super(strings, analyzer);
    }
}
