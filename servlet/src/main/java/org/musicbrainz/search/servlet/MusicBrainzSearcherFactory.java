package org.musicbrainz.search.servlet;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.Similarity;

public class MusicBrainzSearcherFactory extends SearcherFactory {

  private final ResourceType resourceType;

  public MusicBrainzSearcherFactory(ResourceType resourceType) {
    this.resourceType = resourceType;
  }

  @Override
  public IndexSearcher newSearcher(IndexReader reader) throws IOException {

    IndexSearcher searcher = super.newSearcher(reader);

    // Try to set the similarity if it's defined by the ResourceType
    if (this.resourceType.getSimilarityClass() != null) {
      try {
        Similarity similarity = this.resourceType.getSimilarityClass().newInstance();
        searcher.setSimilarity(similarity);
      } catch (InstantiationException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }

    return searcher;
  }

}
