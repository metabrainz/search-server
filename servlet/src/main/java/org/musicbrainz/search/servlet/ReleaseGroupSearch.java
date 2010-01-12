package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;
import org.musicbrainz.search.index.ReleaseGroupIndex;
import org.musicbrainz.search.index.ReleaseGroupIndexField;
import org.musicbrainz.search.servlet.mmd1.ReleaseGroupMmd1XmlWriter;
import org.musicbrainz.search.servlet.mmd2.ReleaseGroupWriter;

import java.util.ArrayList;


public class ReleaseGroupSearch extends SearchServer{

    public ReleaseGroupSearch() throws Exception
    {
        resultsWriter = new ReleaseGroupWriter();
        mmd1XmlWriter = new ReleaseGroupMmd1XmlWriter();
        defaultFields       = new ArrayList<String>();
        defaultFields.add(ReleaseGroupIndexField.RELEASEGROUP.getName());
        analyzer = new PerFieldEntityAnalyzer(ReleaseGroupIndexField.class);
    }

    public ReleaseGroupSearch(String indexDir, boolean useMMapDirectory) throws Exception {

        this();
        if(useMMapDirectory) {
            indexSearcher = createIndexSearcherFromMMapIndex(indexDir, new ReleaseGroupIndex().getFilename());
        }
        else {
            indexSearcher = createIndexSearcherFromFileIndex(indexDir, new ReleaseGroupIndex().getFilename());
        }
        this.setLastServerUpdatedDate();
    }


    public ReleaseGroupSearch(IndexSearcher searcher) throws Exception {

        this();
        indexSearcher = searcher;
    }


    @Override
    protected QueryParser getParser() {
       return new ReleaseGroupQueryParser(defaultFields.get(0), analyzer);
    }

}