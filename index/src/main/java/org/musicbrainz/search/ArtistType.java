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

    private String fieldname;

    ArtistType(String fieldname) {
        this.fieldname = fieldname;
    }

    public String getFieldname() {
        return fieldname;
    }

}
