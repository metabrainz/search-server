package org.musicbrainz.search.servlet;

import org.apache.lucene.search.similarities.Similarity;
import org.musicbrainz.search.analysis.MusicbrainzSimilarity;
import org.musicbrainz.search.analysis.RecordingSimilarity;
import org.musicbrainz.search.analysis.ReleaseGroupSimilarity;
import org.musicbrainz.search.index.*;

/**
 * Defines the name of the webservice resources as defined at http://wiki.musicbrainz.org/XML_Web_Service#The_URL_Schema
 */
public enum ResourceType {
  AREA("area", AreaIndex.class, AreaSearch.class, AreaDismaxSearch.class, MusicbrainzSimilarity.class, false),
  ARTIST("artist", ArtistIndex.class, ArtistSearch.class, ArtistDismaxSearch.class, MusicbrainzSimilarity.class, true),
  LABEL("label", LabelIndex.class, LabelSearch.class, LabelDismaxSearch.class, MusicbrainzSimilarity.class, true),
  RELEASE("release", ReleaseIndex.class, ReleaseSearch.class, ReleaseDismaxSearch.class, true),
  RELEASE_GROUP("release-group", ReleaseGroupIndex.class, ReleaseGroupSearch.class, ReleaseGroupDismaxSearch.class, ReleaseGroupSimilarity.class, true),
  RECORDING("recording", RecordingIndex.class, RecordingSearch.class, RecordingDismaxSearch.class, RecordingSimilarity.class, true),
  CDSTUB("cdstub", CDStubIndex.class, CDStubSearch.class, CDStubDismaxSearch.class, false),
  FREEDB("freedb", FreeDBIndex.class, FreeDBSearch.class, FreeDBDismaxSearch.class, false),
  ANNOTATION("annotation", AnnotationIndex.class, AnnotationSearch.class, AnnotationDismaxSearch.class, false),
  WORK("work", WorkIndex.class, WorkSearch.class, WorkDismaxSearch.class, true),
  TAG("tag", TagIndex.class, TagSearch.class, TagDismaxSearch.class, false), ;

  private String name;
  private String indexName = null;
  private Class<AbstractSearchServer> searchServerClass;
  private Class<AbstractDismaxSearchServer> dismaxSearchServerClass;
  private Class<Similarity> similarityClass = null;
  private boolean isUsedBySearchAll;

  ResourceType(String name, Class indexClass, Class searchServerClass, Class dismaxSearchServerClass,
      boolean isUsedBySearchAll) {
    this.isUsedBySearchAll = isUsedBySearchAll;
    this.name = name;
    this.searchServerClass = searchServerClass;
    this.dismaxSearchServerClass = dismaxSearchServerClass;
    Index index;
    try {
      index = (Index) indexClass.newInstance();
      this.indexName = index.getName();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  ResourceType(String name, Class indexClass, Class searchServerClass, Class dismaxSearchServerClass,
      Class similarityClass, boolean isUsedBySearchAll) {
    this(name, indexClass, searchServerClass, dismaxSearchServerClass, isUsedBySearchAll);
    this.similarityClass = similarityClass;
  }

  public String getName() {
    return name;
  }

  public String getIndexName() {
    return indexName;
  }

  public Class<AbstractSearchServer> getSearchServerClass() {
    return searchServerClass;
  }

  public Class<AbstractDismaxSearchServer> getDismaxSearchServerClass() {
    return dismaxSearchServerClass;
  }

  public boolean isUsedBySearchAll() {
    return isUsedBySearchAll;
  }

  public static ResourceType getValue(String value) {
    for (ResourceType candidateEnum : ResourceType.values()) {
      if (candidateEnum.getName().equals(value))
        return candidateEnum;
    }
    return null;
  }

  public Class<Similarity> getSimilarityClass() {
    return similarityClass;
  }
}
