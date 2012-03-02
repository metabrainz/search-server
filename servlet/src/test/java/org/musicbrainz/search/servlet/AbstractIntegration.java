package org.musicbrainz.search.servlet;

import junit.framework.TestCase;
import org.musicbrainz.mmd2.Metadata;

import javax.xml.bind.JAXBContext;
import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

/**
 * Abstract Integration test for  searching against real indexes using latest search code in jetty
 *
 * Assumes that the web.xml is configured to use indexes compatible with the search code so when code changes
 * have been made that necessitate real indexes would need to do the following
 *
 * mvn package
 * Rebuild indexes
 * mvn integration-test to run tests, if test fail get an error but build doesn't currently fall over to
 * allow for the fact that in some environments it may be difficult to setup up integration tests
 *
 */
public class AbstractIntegration extends TestCase {

    protected JAXBContext context;

    protected AbstractIntegration(String testName) {
       super(testName);
    }

    protected void setUp() throws Exception {
        context = JAXBContext.newInstance("org.musicbrainz.mmd2");
    }

    public Metadata doSearch(String searchUrl) throws Exception {
        BufferedInputStream bis;
        URL url = new URL(searchUrl);
        HttpURLConnection uc = (HttpURLConnection)url.openConnection();
        int responseCode = uc.getResponseCode();
        assertEquals(responseCode,HttpURLConnection.HTTP_OK);

        if("gzip".equals(uc.getContentEncoding())) {
            bis = new BufferedInputStream(new GZIPInputStream(uc.getInputStream()));
        }
        else {
            bis = new BufferedInputStream(uc.getInputStream());
        }

        Metadata metadata = (Metadata) context.createUnmarshaller().unmarshal(bis);
        return metadata;
    }
}
