package org.musicbrainz.search.index;

public enum ReleaseQuality {
    LOW(),
    NORMAL(),
    HIGH(),
    ;

    public static ReleaseQuality mapReleaseQuality(int quality)
    {
        if(quality==0) {
            return LOW;
        }
        else if(quality==2) {
            return HIGH;
        }
        else {

            return NORMAL;
        }
    }

}
