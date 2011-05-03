package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.WorkIndexField;
import org.musicbrainz.search.servlet.mmd2.WorkWriter;

import java.util.ArrayList;


public class WorkSearch extends SearchServer {

    public WorkSearch() throws Exception {
    
        resultsWriter = new WorkWriter();
        defaultFields = new ArrayList<String>();
        defaultFields.add(WorkIndexField.WORK.getName());
        defaultFields.add(WorkIndexField.ALIAS.getName());
        analyzer = DatabaseIndex.getAnalyzer(WorkIndexField.class);
    }

    public WorkSearch(IndexSearcher searcher) throws Exception {
        this();
        indexSearcher = searcher;
    }

     @Override
    protected QueryParser getParser() {
       return new WorkQueryParser(defaultFields.toArray(new String[0]), analyzer);
    }
}