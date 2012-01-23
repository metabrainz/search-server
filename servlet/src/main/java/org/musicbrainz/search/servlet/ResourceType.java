package org.musicbrainz.search.servlet;

import org.musicbrainz.search.index.*;

/**
 * Defines the name of the webservice resources as defined at http://wiki.musicbrainz.org/XML_Web_Service#The_URL_Schema
 */
public enum ResourceType {

    ARTIST 			("artist", 			ArtistIndex.class, 			ArtistSearch.class, true),
    LABEL 			("label", 			LabelIndex.class, 			LabelSearch.class, true),
    RELEASE 		("release", 		ReleaseIndex.class, 		ReleaseSearch.class, true),
    RELEASE_GROUP 	("release-group",	ReleaseGroupIndex.class, 	ReleaseGroupSearch.class, true),
    RECORDING 		("recording", 		RecordingIndex.class, 		RecordingSearch.class, true),
    CDSTUB 			("cdstub", 			CDStubIndex.class, 			CDStubSearch.class, false),
    FREEDB 			("freedb", 			FreeDBIndex.class, 			FreeDBSearch.class, false),
    ANNOTATION 		("annotation", 		AnnotationIndex.class, 		AnnotationSearch.class, false),
    WORK 			("work", 			WorkIndex.class, 			WorkSearch.class, true),
    TAG 			("tag", 			TagIndex.class, 			TagSearch.class, false),
    ;

    private String name;
    private String indexName = null;
    private Class<SearchServer> searchServerClass;
    private boolean isUsedBySearchAll;
    
    ResourceType(String name, Class indexClass, Class searchServerClass, boolean isUsedBySearchAll) {
        this.isUsedBySearchAll = isUsedBySearchAll;
        this.name = name;
        this.searchServerClass = searchServerClass; 
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

    public String getName() {
        return name;
    }

    public String getIndexName() {
    	return indexName;
    }
    
    public Class<SearchServer> getSearchServerClass() {
    	return searchServerClass;
    }

    public boolean isUsedBySearchAll() {
        return isUsedBySearchAll;
    }

    public static ResourceType getValue(String value) {
        for ( ResourceType candidateEnum : ResourceType.values() ) {
            if(candidateEnum.getName().equals(value)) return candidateEnum;
        }
        return null;
    }

}
