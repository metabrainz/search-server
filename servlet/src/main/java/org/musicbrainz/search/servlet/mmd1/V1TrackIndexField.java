package org.musicbrainz.search.servlet.mmd1;

/**
 * Fields search for in V1 Track
 */
public enum V1TrackIndexField  {

    TRACK_ID        ("trid"),
    TRACK           ("track"),
    ;

    private String name;


    private V1TrackIndexField(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}