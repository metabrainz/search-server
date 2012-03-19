package org.musicbrainz.search.servlet;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.index.AnnotationIndexField;
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.servlet.mmd2.AnnotationWriter;

import java.io.IOException;
import java.util.ArrayList;


public class AnnotationSearch extends SearchServer {

    protected void setupDefaultFields() {
        defaultFields = new ArrayList<String>();
        defaultFields.add(AnnotationIndexField.TEXT.getName());
    }

    private AnnotationSearch() throws Exception {
        resultsWriter = new AnnotationWriter();
        mmd1XmlWriter = null;
        setupDefaultFields();
        analyzer = DatabaseIndex.getAnalyzer(AnnotationIndexField.class);
    }

    public AnnotationSearch(IndexSearcher searcher) throws Exception {
        this();
        indexSearcher = searcher;
    }


    @Override
    protected QueryParser getParser() {
     return new QueryParser(LuceneVersion.LUCENE_VERSION, defaultFields.get(0), analyzer);
  }

    @Override
    protected  String printExplainHeader(Document doc)
            throws IOException, ParseException {
        return doc.get(AnnotationIndexField.ID.getName()) +':'
                + doc.get(AnnotationIndexField.ENTITY.getName())
                + '\n';
    }


}