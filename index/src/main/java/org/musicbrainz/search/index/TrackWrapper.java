package org.musicbrainz.search.index;

/**
 * Track Details for use by Recording Index
 */
class TrackWrapper {
    private int     trackPosition;
    private int     trackCount;
    private int     releaseId;
    private String  trackName;
    private int     mediumPosition;

    public int getTrackPosition() {
        return trackPosition;
    }

    public void setTrackPosition(int trackPosition) {
        this.trackPosition = trackPosition;
    }

    public int getTrackCount() {
        return trackCount;
    }

    public void setTrackCount(int trackCount) {
        this.trackCount = trackCount;
    }

    public int getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(int releaseId) {
        this.releaseId = releaseId;
    }


    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public int getMediumPosition() {
        return mediumPosition;
    }

    public void setMediumPosition(int mediumPosition) {
        this.mediumPosition = mediumPosition;
    }

}