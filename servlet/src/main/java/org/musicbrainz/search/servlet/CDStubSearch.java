package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;
import org.musicbrainz.search.index.CDStubIndex;
import org.musicbrainz.search.index.CDStubIndexField;
import org.musicbrainz.search.servlet.mmd2.CDStubWriter;

import java.util.ArrayList;


public class CDStubSearch extends SearchServer {

    private CDStubSearch() throws Exception {
        resultsWriter = new CDStubWriter();
        mmd1XmlWriter = null;
        defaultFields = new ArrayList<String>();
        defaultFields.add(CDStubIndexField.ARTIST.getName());
        defaultFields.add(CDStubIndexField.TITLE.getName());
        analyzer = new PerFieldEntityAnalyzer(CDStubIndexField.class);
    }

    public CDStubSearch(String indexDir, boolean useMMapDirectory) throws Exception {

        this();

        if(useMMapDirectory) {
            indexSearcher = createIndexSearcherFromMMapIndex(indexDir, new CDStubIndex().getFilename());
        }
        else {
            indexSearcher = createIndexSearcherFromFileIndex(indexDir, new CDStubIndex().getFilename());
        }
        this.setLastServerUpdatedDate();
    }


    public CDStubSearch(IndexSearcher searcher) throws Exception {

        this();
        indexSearcher = searcher;
    }

     @Override
    protected QueryParser getParser() {
     return new MultiFieldQueryParser(LuceneVersion.LUCENE_VERSION, defaultFields.toArray(new String[0]), analyzer);
  }

}
