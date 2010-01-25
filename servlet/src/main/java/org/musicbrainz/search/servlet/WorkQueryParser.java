package org.musicbrainz.search.servlet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.util.Version;
import org.musicbrainz.search.index.*;


/**
 * Subclasses QueryParser to handle numeric fields that we might want wish to do range queries for and handle type
 * searches specified using integers.
 */
public class WorkQueryParser extends QueryParser {

    public WorkQueryParser(String field, Analyzer a) {
        super(Version.LUCENE_CURRENT,field, a);
    }
}