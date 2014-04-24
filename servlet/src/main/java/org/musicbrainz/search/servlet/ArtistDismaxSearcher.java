package org.musicbrainz.search.servlet;


import org.apache.lucene.analysis.Analyzer;

public class ArtistDismaxSearcher extends DismaxSearcher
{
    public ArtistDismaxSearcher(DismaxAlias dismaxAlias)
    {
        super(dismaxAlias);
    }


    protected DismaxQueryParser getParser(Analyzer analyzer)  {
        return new ArtistDismaxQueryParser(analyzer);
    }


}
