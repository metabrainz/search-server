package org.musicbrainz.search.index;

public enum ReleaseGroupPrimaryType {
    ALBUM("Album"),
    SINGLE("Single"),
    EP("EP"),
    AUDIOBOOK("Audiobook"),
    OTHER("Other");

    private String name;
    ReleaseGroupPrimaryType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
