package org.musicbrainz.search.servlet;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.musicbrainz.search.index.LabelIndexField;
import org.musicbrainz.search.servlet.mmd1.LabelType;

public class InstrumentQueryParser extends MultiFieldQueryParser
{

    public InstrumentQueryParser(String[] strings, org.apache.lucene.analysis.Analyzer analyzer)
    {
        super(strings, analyzer);
    }
}
