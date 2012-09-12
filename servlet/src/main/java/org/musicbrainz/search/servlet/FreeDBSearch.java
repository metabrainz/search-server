package org.musicbrainz.search.servlet;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.FreeDBIndexField;
import org.musicbrainz.search.servlet.mmd2.FreeDBWriter;

import java.io.IOException;
import java.util.ArrayList;


public class FreeDBSearch extends SearchServer {

    protected void setupDefaultFields() {
        defaultFields = new ArrayList<String>();
        defaultFields.add(FreeDBIndexField.ARTIST.getName());
        defaultFields.add(FreeDBIndexField.TITLE.getName());
    }

    private FreeDBSearch() throws Exception {
        resultsWriter = new FreeDBWriter();
        mmd1Writer = null;
        setupDefaultFields();
        analyzer = DatabaseIndex.getAnalyzer(FreeDBIndexField.class);
    }

    public FreeDBSearch(IndexSearcher searcher) throws Exception {
        this();
        indexSearcher = searcher;
    }


    @Override
    protected QueryParser getParser() {
     return new MultiFieldQueryParser(LuceneVersion.LUCENE_VERSION, defaultFields.toArray(new String[0]), analyzer);
  }

    @Override
    protected  String printExplainHeader(Document doc)
            throws IOException, ParseException {
        return doc.get(FreeDBIndexField.ARTIST.getName()) +':'
                + doc.get(FreeDBIndexField.TITLE.getName())
                + '\n';
    }

}