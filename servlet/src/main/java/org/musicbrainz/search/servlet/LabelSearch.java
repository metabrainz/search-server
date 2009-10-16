package org.musicbrainz.search.servlet;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.queryParser.QueryParser;
import org.musicbrainz.search.index.LabelIndexField;
import org.musicbrainz.search.index.LabelAnalyzer;
import org.musicbrainz.search.servlet.mmd1.LabelMmd1XmlWriter;
import org.musicbrainz.search.servlet.mmd2.ArtistXmlWriter;
import org.musicbrainz.search.servlet.mmd2.LabelXmlWriter;

import java.util.ArrayList;


public class LabelSearch extends SearchServer {

    public LabelSearch() throws Exception {

        xmlWriter = new LabelXmlWriter();
        mmd1XmlWriter = new LabelMmd1XmlWriter();
        htmlWriter = new LabelHtmlWriter();
        defaultFields = new ArrayList<String>();
        defaultFields.add(LabelIndexField.LABEL.getName());
        defaultFields.add(LabelIndexField.ALIAS.getName());
        defaultFields.add(LabelIndexField.SORTNAME.getName());
        analyzer = new LabelAnalyzer();

    }

    public LabelSearch(String indexDir) throws Exception {

        this();
        indexSearcher = createIndexSearcherFromFileIndex(indexDir,"label_index");
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
