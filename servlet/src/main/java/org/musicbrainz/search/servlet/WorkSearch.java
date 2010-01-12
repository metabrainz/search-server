package org.musicbrainz.search.servlet;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.queryParser.QueryParser;
import org.musicbrainz.search.index.WorkIndex;
import org.musicbrainz.search.index.WorkIndexField;
import org.musicbrainz.search.servlet.mmd2.WorkWriter;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.util.ArrayList;


public class WorkSearch extends SearchServer {

    public WorkSearch() throws Exception {

        resultsWriter = new WorkWriter();
        defaultFields = new ArrayList<String>();
        defaultFields.add(WorkIndexField.WORK.getName());
        analyzer = new PerFieldEntityAnalyzer(WorkIndexField.class);
    }

    public WorkSearch(String indexDir) throws Exception {

        this();
        indexSearcher = createIndexSearcherFromFileIndex(indexDir,new WorkIndex().getFilename());
        this.setLastServerUpdatedDate();
    }


    public WorkSearch(IndexSearcher searcher) throws Exception {

        this();
        indexSearcher = searcher;
    }

     @Override
    protected QueryParser getParser() {
       return new WorkQueryParser(defaultFields.get(0), analyzer);
    }
}