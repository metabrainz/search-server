package org.musicbrainz.search.servlet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;

public class DismaxSearcher {

    private DismaxQueryParser.DismaxAlias dismaxAlias;

    public DismaxSearcher(DismaxQueryParser.DismaxAlias dismaxAlias)
    {
        this.dismaxAlias = dismaxAlias;
    }

    public Query parseQuery(String query, Analyzer analyzer) throws ParseException
    {
        query= QueryParser.escape(query);
        DismaxQueryParser queryParser = new DismaxQueryParser(analyzer);
        queryParser.addAlias(DismaxQueryParser.IMPOSSIBLE_FIELD_NAME, dismaxAlias);
        Query q = queryParser.parse(query);
        return q;
    }
}
