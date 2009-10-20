package org.musicbrainz.search.index;

/**
 * 
 */
class ArtistWrapper {
    private String artistId;
    private String artistName;
    private String artistCreditName;
    private String artistSortName;
    private String artistComment;
    private int artistPos;
    private String joinPhrase;

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getArtistCreditName() {
        return artistCreditName;
    }

    public void setArtistCreditName(String artistCreditName) {
        this.artistCreditName = artistCreditName;
    }

    public String getArtistComment() {
        return artistComment;
    }

    public void setArtistComment(String artistComment) {
        this.artistComment = artistComment;
    }

    public int getArtistPos() {
        return artistPos;
    }

    public void setArtistPos(int artistPos) {
        this.artistPos = artistPos;
    }

    public String getJoinPhrase() {
        return joinPhrase;
    }

    public void setJoinPhrase(String joinPhrase) {
        this.joinPhrase = joinPhrase;
    }

    public String getArtistSortName() {
        return artistSortName;
    }

    public void setArtistSortName(String artistSortName) {
        this.artistSortName = artistSortName;
    }
}
