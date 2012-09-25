package org.musicbrainz.search.servlet;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.SearcherManager;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.index.CDStubIndexField;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.servlet.mmd2.CDStubWriter;


public class CDStubSearch extends SearchServer {

    protected void setupDefaultFields() {
        defaultFields = new ArrayList<String>();
        defaultFields.add(CDStubIndexField.ARTIST.getName());
        defaultFields.add(CDStubIndexField.TITLE.getName());
    }

    private CDStubSearch() throws Exception {
        resultsWriter = new CDStubWriter();
        mmd1Writer = null;
        setupDefaultFields();
        analyzer = DatabaseIndex.getAnalyzer(CDStubIndexField.class);
    }

    public CDStubSearch(SearcherManager searcherManager) throws Exception {
        this();
        this.searcherManager = searcherManager;
        setLastServerUpdatedDate();
        resultsWriter.setLastServerUpdatedDate(this.getServerLastUpdatedDate());
    }

    public CDStubSearch(SearcherManager searcherManager, String query, int offset, int limit) throws Exception {
            this(searcherManager);
            this.query=query;
            this.offset=offset;
            this.limit=limit;

        }

     @Override
    protected QueryParser getParser() {
        return new MultiFieldQueryParser(LuceneVersion.LUCENE_VERSION, defaultFields.toArray(new String[0]), analyzer);
    }

    @Override
    protected  String printExplainHeader(Document doc)
            throws IOException, ParseException {
        return doc.get(CDStubIndexField.ID.getName()) +':'
                + doc.get(CDStubIndexField.ARTIST.getName()) + ':'
                + doc.get(CDStubIndexField.TITLE.getName())
                + '\n';
    }
}
