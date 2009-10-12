package org.musicbrainz.search.index;

import junit.framework.TestCase;

public class ArtistCreditTest extends TestCase {

    public void testGetArtistCreditString() throws Exception {

        ArtistCreditName acn1 = new ArtistCreditName(
                "Dutronc", " et ", 1, "Jacques Dutronc"
        );
        ArtistCreditName acn2 = new ArtistCreditName(
                "Hardy", null, 2, "Francoise Hardy"
        );
        
        ArtistCredit ac = new ArtistCredit();
        ac.appendArtistCreditName(acn1);
        ac.appendArtistCreditName(acn2);
        
        assertEquals("Dutronc et Hardy", ac.getArtistCreditString());

    }

}
