package org.musicbrainz.search.index;

import java.util.List;

/**
 * Track Details for use by Recordings 
 */
class TrackWrapper {
    private int     trackPosition;
    private int     trackCount;
    private String  releaseId;
    private String  releaseName;
    private String  releaseGroupType;
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

    public String getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(String releaseId) {
        this.releaseId = releaseId;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    public String getReleaseGroupType() {
        return releaseGroupType;
    }

    public void setReleaseGroupType(String releaseGroupType) {
        this.releaseGroupType = releaseGroupType;
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