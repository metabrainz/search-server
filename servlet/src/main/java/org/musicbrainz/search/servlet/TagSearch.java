package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;
import org.musicbrainz.search.index.TagIndex;
import org.musicbrainz.search.index.TagIndexField;
import org.musicbrainz.search.servlet.mmd2.TagWriter;

import java.util.ArrayList;


public class TagSearch extends SearchServer {

    public TagSearch() throws Exception {

        resultsWriter = new TagWriter();
        defaultFields = new ArrayList<String>();
        defaultFields.add(TagIndexField.TAG.getName());
        analyzer = new PerFieldEntityAnalyzer(TagIndexField.class);
    }

    public TagSearch(String indexDir, boolean useMMapDirectory) throws Exception {

        this();
        if(useMMapDirectory) {
            indexSearcher = createIndexSearcherFromMMapIndex(indexDir, new TagIndex().getFilename());
        }
        else {
            indexSearcher = createIndexSearcherFromFileIndex(indexDir, new TagIndex().getFilename());
        }
        this.setLastServerUpdatedDate();
    }


    public TagSearch(IndexSearcher searcher) throws Exception {

        this();
        indexSearcher = searcher;
    }

     @Override
    protected QueryParser getParser() {
       return new TagQueryParser(defaultFields.get(0), analyzer);
    }
}