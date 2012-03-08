package org.musicbrainz.search.servlet;

import org.apache.commons.lang.time.StopWatch;
import org.musicbrainz.mmd2.Metadata;

import javax.xml.bind.JAXBContext;


public class RecordingIT extends AbstractIntegration {

    public RecordingIT(String testName) {
        super(testName);
    }

    public void testSearchForRecording() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?type=recording&query=fred");
        assertTrue(metadata.getRecordingList().getRecording().size()>0);
    }

    public void testSearchForTrackV1() throws Exception {
        com.jthink.brainz.mmd.Metadata metadata = doSearchV1("http://localhost:8080/?type=track&query=fred&version=1");
        assertTrue(metadata.getTrackList().getTrack().size()>0);
    }

    public void testSearchForRecordingDismax() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=recording&query=fred");
        assertTrue(metadata.getRecordingList().getRecording().size()>0);
    }

    public void testSearchForRecordingDismaxPopularTerm() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=recording&query=love");
        assertTrue(metadata.getRecordingList().getRecording().size()>0);
    }

    /** Will not do fuzzy because term length to short
     *
     * @throws Exception
     */
    public void testSearchForRecordingDismaxNoFuzzy() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=recording&query=the");
        assertTrue(metadata.getRecordingList().getRecording().size()>0);
    }

    public void testSearchForRecordingDismaxMultiTerm() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=recording&query=love+rocket");
        assertTrue(metadata.getRecordingList().getRecording().size()>0);
    }
}