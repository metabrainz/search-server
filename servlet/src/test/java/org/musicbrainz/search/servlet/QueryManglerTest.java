package org.musicbrainz.search.servlet;
import junit.framework.TestCase;

import org.musicbrainz.search.servlet.ArtistMangler;
import org.musicbrainz.search.servlet.LabelMangler;
import org.musicbrainz.search.servlet.QueryMangler;
import org.musicbrainz.search.servlet.ReleaseGroupMangler;
import org.musicbrainz.search.servlet.ReleaseMangler;
import org.musicbrainz.search.servlet.TrackMangler;

public class QueryManglerTest extends TestCase {

    public void testArtistMangler() {
        QueryMangler qm = new ArtistMangler();
        assertEquals("type:unknown", qm.mangleQuery("type:0"));
        assertEquals("type:person", qm.mangleQuery("artype:1"));
        assertEquals("type:person", qm.mangleQuery("type:1"));
        assertEquals("type:person", qm.mangleQuery("type:person"));
        assertEquals("type:person OR type:group", qm.mangleQuery("type:1 OR type:2"));
        assertEquals("name:fred OR type:person", qm.mangleQuery("name:fred OR artype:1"));
        assertEquals("type:5", qm.mangleQuery("artype:5"));
    }

    public void testLabelMangler() {
        QueryMangler qm = new LabelMangler();
        assertEquals("type:unknown", qm.mangleQuery("type:0"));
        assertEquals("type:distributor", qm.mangleQuery("type:1"));
        assertEquals("type:distributor OR type:holding", qm.mangleQuery("type:1 OR type:2"));
        assertEquals("name:fred OR type:holding", qm.mangleQuery("name:fred OR type:2"));
        assertEquals("type:distributor", qm.mangleQuery("type:distributor"));
        assertEquals("type:8", qm.mangleQuery("type:8"));
        assertEquals("code:5807", qm.mangleQuery("code:05807"));

    }

    public void testReleaseMangler() {
        QueryMangler qm = new ReleaseMangler();
        assertEquals("type:other", qm.mangleQuery("type:0"));
        assertEquals("type:album", qm.mangleQuery("type:1"));
        assertEquals("type:album OR type:single", qm.mangleQuery("type:1 OR type:2"));
        assertEquals("name:fred OR type:single", qm.mangleQuery("name:fred OR type:2"));
        assertEquals("type:album", qm.mangleQuery("type:album"));
        assertEquals("type:audiobook", qm.mangleQuery("type:8"));
        assertEquals("status:official", qm.mangleQuery("status:1"));
        assertEquals("status:0", qm.mangleQuery("status:0"));
        assertEquals("status:official OR status:promotion", qm.mangleQuery("status:1 OR status:2"));
        assertEquals("barcode:88847474", qm.mangleQuery("barcode:088847474"));


    }

    public void testReleaseGroupMangler() {
        QueryMangler qm = new ReleaseGroupMangler();
        assertEquals("type:nat", qm.mangleQuery("type:0"));
        assertEquals("type:album", qm.mangleQuery("type:1"));
        assertEquals("type:album OR type:single", qm.mangleQuery("type:1 OR type:2"));
        assertEquals("name:fred OR type:single", qm.mangleQuery("name:fred OR type:2"));
        assertEquals("type:album", qm.mangleQuery("type:album"));
        assertEquals("type:audiobook", qm.mangleQuery("type:8"));

    }

    public void testTrackMangler() {
        QueryMangler qm = new TrackMangler();
        assertEquals("type:other", qm.mangleQuery("type:0"));
        assertEquals("type:album", qm.mangleQuery("type:1"));
        assertEquals("type:album OR type:single", qm.mangleQuery("type:1 OR type:2"));
        assertEquals("name:fred OR type:single", qm.mangleQuery("name:fred OR type:2"));
        assertEquals("type:album", qm.mangleQuery("type:album"));
        assertEquals("type:audiobook", qm.mangleQuery("type:8"));
        assertEquals("qdur:00000000000039", qm.mangleQuery("qdur:117"));
        assertEquals("qdur:[0000000000002p TO 00000000000039]", qm.mangleQuery("qdur:[97 TO 117]"));
        assertEquals("dur:000000000050k0", qm.mangleQuery("dur:234000"));
        assertEquals("dur:[000000000050k0 TO 000000000051bs]", qm.mangleQuery("dur:[234000 TO 235000]"));


    }
}
