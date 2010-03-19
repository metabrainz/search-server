package org.musicbrainz.search.servlet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;
import org.musicbrainz.search.LuceneVersion;

/**
 * Subclasses QueryParser to handle numeric fields that we might want wish to do range queries for and handle type
 * searches specified using integers.
 */
public class WorkQueryParser extends QueryParser {

    public WorkQueryParser(String field, Analyzer a) {
        super(LuceneVersion.LUCENE_VERSION, field, a);
    }
}