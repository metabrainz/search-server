package org.musicbrainz.search.servlet;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * This class reads a number of urls from an input file , then open the websiet and records how long it
 * takes for the results to come back. The total time taken to open the urls is output, the urls should be
 * in encoded format.
 */
public class TestWebServerSearch {

    static Map<URL,Integer> map = new HashMap<URL,Integer>();
    static long totalQueryTime = 0;
    public static void main(String[] args) throws Exception
    {
        Options options = new Options();
        CmdLineParser parser = new CmdLineParser(options);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            e.printStackTrace();
            System.err.println("Couldn't parse command line parameters");
            parser.printUsage(System.out);
            System.exit(1);
        }

        if ((options.getTestFile().equals(""))) {
            System.err.println("Require Test File");
            System.exit(1);
        }

        processUrls(new File(options.getTestFile()));
    }

    private static void processUrls(File urlFile) throws Exception
    {
        String input;
        BufferedReader in = new BufferedReader(new FileReader(urlFile));
        while((input = in.readLine()) != null){
            map.put(new URL(input), null);

        }
        in.close();

        for(URL url:map.keySet())
        {
            processUrl(url);
        }
        System.out.println("Total Query TIme was:"+totalQueryTime + "Ms");
    }

    private static void processUrl(URL url) throws InterruptedException
    {
        try
        {

            HttpURLConnection uc = (HttpURLConnection)url.openConnection();
            uc.setRequestProperty("User-Agent","Musicbrainz Test");
            long start = System.nanoTime();
            int responseCode = uc.getResponseCode();
            long end = System.nanoTime();
            long queryInMs = ((end - start) / 1000000 );
            totalQueryTime+=queryInMs;
            if(responseCode!=HttpURLConnection.HTTP_OK)
            {
                System.out.println("url failed :"+url+":"+uc.getResponseCode());
            }
            else
            {
                System.out.println("url ok :"+url + ":" + queryInMs+" ms");
            }

            if(queryInMs < 1000)
            {

                Thread.sleep(1000 - queryInMs);
            }
        }
        catch(IOException ioe)
        {
            System.out.println("url failed :"+url);
        }
    }

    static class Options {

        // File containing Test urls
        @Option(name = "--testfile", aliases = {"-f"}, usage = "Test file containing one url each line encoded")
        private String testFile = "";

        public String getTestFile() {
            return testFile;
        }
    }
}
