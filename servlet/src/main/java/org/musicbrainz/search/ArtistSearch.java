package org.musicbrainz.search;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.File;
import java.util.ArrayList;


public class ArtistSearch extends SearchServer {

    private ArtistSearch() throws Exception {
        xmlWriter = new ArtistXmlWriter();
        htmlWriter = new ArtistHtmlWriter();
        queryMangler = new ArtistMangler();
        defaultFields = new ArrayList<String>();
        defaultFields.add(ArtistIndexField.ARTIST.getName());
        defaultFields.add(ArtistIndexField.ALIAS.getName());
        defaultFields.add(ArtistIndexField.SORTNAME.getName());
    }

    public ArtistSearch(String indexDir) throws Exception {

        this();
        indexSearcher = createIndexSearcherFromFileIndex(indexDir,"artist_index");
    }


    public ArtistSearch(IndexSearcher searcher) throws Exception {

        this();
        indexSearcher = searcher;
    }

}
