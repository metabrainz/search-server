package org.musicbrainz.search.servlet;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.ReleaseIndexField;
import org.musicbrainz.search.servlet.mmd1.ReleaseMmd1XmlWriter;
import org.musicbrainz.search.servlet.mmd2.ReleaseWriter;

import java.io.IOException;
import java.util.ArrayList;


public class ReleaseSearch extends SearchServer {

    protected void setupDefaultFields() {
        defaultFields       = new ArrayList<String>();
        defaultFields.add(ReleaseIndexField.RELEASE.getName());
    }

    public ReleaseSearch() throws Exception {
    
        resultsWriter = new ReleaseWriter();
        mmd1XmlWriter = new ReleaseMmd1XmlWriter();
        setupDefaultFields();
        analyzer = DatabaseIndex.getAnalyzer(ReleaseIndexField.class);
    }

    public ReleaseSearch(IndexSearcher searcher) throws Exception {
        this();
        indexSearcher = searcher;
    }

    public ReleaseSearch(IndexSearcher searcher, String query, int offset, int limit) throws Exception {
        this(searcher);
        this.query=query;
        this.offset=offset;
        this.limit=limit;
    }

     @Override
    protected QueryParser getParser() {
       return new ReleaseQueryParser(defaultFields.toArray(new String[0]), analyzer);
    }

    @Override
    protected  String printExplainHeader(Document doc)
            throws IOException, ParseException {
        return doc.get(ReleaseIndexField.RELEASE_ID.getName()) +':'
                + doc.get(ReleaseIndexField.RELEASE.getName())
                + '\n';
    }

}