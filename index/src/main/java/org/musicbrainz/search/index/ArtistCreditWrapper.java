package org.musicbrainz.search.index;

import org.musicbrainz.mmd2.ArtistCredit;

/**
 * Wraps an Artist Credit object to allow us to store the artist credit database key
 *
 */
public class ArtistCreditWrapper {
    private int     artistCreditId;
    private ArtistCredit    artistCredit;

    public int getArtistCreditId() {
        return artistCreditId;
    }

    public void setArtistCreditId(int artistCreditId) {
        this.artistCreditId = artistCreditId;
    }

    public ArtistCredit getArtistCredit() {
        return artistCredit;
    }

    public void setArtistCredit(ArtistCredit artistCredit) {
        this.artistCredit = artistCredit;
    }
}
