package org.musicbrainz.search.index;

/**
 * Release Details for use by other entities
 */
class ReleaseWrapper {

    private String  releaseId;
    private String  releaseName;

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


}