package org.musicbrainz.search.servlet;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.WorkIndexField;
import org.musicbrainz.search.servlet.mmd2.WorkWriter;

import java.io.IOException;
import java.util.ArrayList;


public class WorkSearch extends SearchServer {

    protected void setupDefaultFields() {
        defaultFields = new ArrayList<String>();
        defaultFields.add(WorkIndexField.WORK.getName());
        defaultFields.add(WorkIndexField.ALIAS.getName());
    }

    public WorkSearch() throws Exception {
        resultsWriter = new WorkWriter();
        setupDefaultFields();
        analyzer = DatabaseIndex.getAnalyzer(WorkIndexField.class);
    }

    public WorkSearch(IndexSearcher searcher) throws Exception {
        this();
        indexSearcher = searcher;
    }

    public WorkSearch(IndexSearcher searcher, String query, int offset, int limit) throws Exception {
        this(searcher);
        this.query=query;
        this.offset=offset;
        this.limit=limit;
    }

     @Override
    protected QueryParser getParser() {
       return new WorkQueryParser(defaultFields.toArray(new String[0]), analyzer);
    }

    @Override
    protected  String printExplainHeader(Document doc)
            throws IOException, ParseException {
        return doc.get(WorkIndexField.WORK_ID.getName()) +':'
                + doc.get(WorkIndexField.WORK.getName())
                + '\n';
    }

}