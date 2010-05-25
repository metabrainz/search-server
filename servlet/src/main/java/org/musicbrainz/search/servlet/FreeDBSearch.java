package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;
import org.musicbrainz.search.index.FreeDBIndex;
import org.musicbrainz.search.index.FreeDBIndexField;
import org.musicbrainz.search.servlet.mmd2.FreeDBWriter;

import java.util.ArrayList;


public class FreeDBSearch extends SearchServer {

    private FreeDBSearch() throws Exception {

        resultsWriter = new FreeDBWriter();
        mmd1XmlWriter = null;
        defaultFields = new ArrayList<String>();
        defaultFields.add(FreeDBIndexField.ARTIST.getName());
        defaultFields.add(FreeDBIndexField.TITLE.getName());
        analyzer = new PerFieldEntityAnalyzer(FreeDBIndexField.class);
    }

    public FreeDBSearch(String indexDir, boolean useMMapDirectory) throws Exception {

        this();
        if(useMMapDirectory) {
            indexSearcher = createIndexSearcherFromMMapIndex(indexDir, new FreeDBIndex().getFilename());
        }
        else {
            indexSearcher = createIndexSearcherFromFileIndex(indexDir, new FreeDBIndex().getFilename());
        }
        this.setLastServerUpdatedDate();
    }


    public FreeDBSearch(IndexSearcher searcher) throws Exception {

        this();
        indexSearcher = searcher;
    }


    @Override
    protected QueryParser getParser() {
     return new MultiFieldQueryParser(LuceneVersion.LUCENE_VERSION, defaultFields.toArray(new String[0]), analyzer);
  }
}