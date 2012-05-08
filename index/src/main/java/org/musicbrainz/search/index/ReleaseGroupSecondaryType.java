package org.musicbrainz.search.index;

public enum ReleaseGroupSecondaryType {
    COMPILATION("Compilation"),
    INTERVIEW("Interview"),
    LIVE("Live"),
    REMIX("Remix"),
    SOUNDTRACK("Soundtrack"),
    SPOKENWORD("Spokenword"),
    ;

    private String name;
    ReleaseGroupSecondaryType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
