package org.musicbrainz.search;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: May 14, 2009
 * Time: 4:44:49 PM
 * To change this template use File | Settings | File Templates.
 */
public enum ArtistType {
    UNKNOWN("unknown"),
    PERSON("person"),
    GROUP("group"),;

    private String name;

    ArtistType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
