package org.musicbrainz.search;

/**
 * Defines the name of the webservice resources as defined at http://wiki.musicbrainz.org/XML_Web_Service#The_URL_Schema
 */
public enum ResourceType {

    ARTIST("artist"),
    LABEL("label"),
    RELEASE("release"),
    RELEASE_GROUP("release-group"),
    TRACK("track"), 
    CDSTUB("cdstub"),
    FREEDB("freedb"),
    ANNOTATION("annotation"),
    ;

    private String name;

    ResourceType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ResourceType getValue(String value) {
        for ( ResourceType candidateEnum : ResourceType.values() ) {
            if(candidateEnum.getName().equals(value)) return candidateEnum;
        }
        return null;
    }

}
