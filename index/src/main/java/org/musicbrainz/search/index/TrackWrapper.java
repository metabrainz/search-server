package org.musicbrainz.search.index;

/**
 * Track Details for use by Recording Index
 */
class TrackWrapper {
    private int     trackId;
    private int     trackPosition;
    private int     trackCount;
    private int     releaseId;
    private String  trackName;
    private int     mediumPosition;
    private String  mediumFormat;
    private int     duration;
    private String  trackNumber;

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

    public String getMediumFormat() {
        return mediumFormat;
    }

    public void setMediumFormat(String mediumFormat) {
        this.mediumFormat = mediumFormat;
    }

    public int getTrackId() {
        return trackId;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(String trackNumber) {
        this.trackNumber = trackNumber;
    }
}