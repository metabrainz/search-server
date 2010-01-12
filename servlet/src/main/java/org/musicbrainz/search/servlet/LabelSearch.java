package org.musicbrainz.search.servlet;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.queryParser.QueryParser;
import org.musicbrainz.search.index.LabelIndex;
import org.musicbrainz.search.index.LabelIndexField;
import org.musicbrainz.search.servlet.mmd1.LabelMmd1XmlWriter;
import org.musicbrainz.search.servlet.mmd2.LabelWriter;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.util.ArrayList;


public class LabelSearch extends SearchServer {

    public LabelSearch() throws Exception {

        resultsWriter = new LabelWriter();
        mmd1XmlWriter = new LabelMmd1XmlWriter();
        defaultFields = new ArrayList<String>();
        defaultFields.add(LabelIndexField.LABEL.getName());
        defaultFields.add(LabelIndexField.ALIAS.getName());
        defaultFields.add(LabelIndexField.SORTNAME.getName());
        analyzer = new PerFieldEntityAnalyzer(LabelIndexField.class);

    }

    public LabelSearch(String indexDir) throws Exception {

        this();
        indexSearcher = createIndexSearcherFromFileIndex(indexDir,new LabelIndex().getFilename());
        this.setLastServerUpdatedDate();
    }


    public LabelSearch(IndexSearcher searcher) throws Exception {

        this();
        indexSearcher = searcher;
    }

       @Override
    protected QueryParser getParser() {
       return new LabelQueryParser(defaultFields.toArray(new String[0]), analyzer);
    }

}
