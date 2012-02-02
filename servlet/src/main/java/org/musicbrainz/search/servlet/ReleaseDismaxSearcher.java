package org.musicbrainz.search.servlet;

import org.apache.lucene.analysis.Analyzer;

public class ReleaseDismaxSearcher extends DismaxSearcher {

    public ReleaseDismaxSearcher(DismaxQueryParser.DismaxAlias dismaxAlias)
    {
        super(dismaxAlias);
    }

    protected DismaxQueryParser getParser(Analyzer analyzer)  {
        return new ReleaseDismaxQueryParser(analyzer);
    }
}
