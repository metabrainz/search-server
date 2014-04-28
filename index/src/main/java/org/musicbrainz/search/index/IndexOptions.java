package org.musicbrainz.search.index;

import org.kohsuke.args4j.Option;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class IndexOptions {

    private static final int MAX_TEST_ID = 50000;
    private static final int IDS_PER_CHUNK = 20000;

    // Lucene parameters
    public static final int MAX_BUFFERED_DOCS = 10000;

    // PostgreSQL schema that holds MB data
	public static final String DB_SCHEMA = "musicbrainz";
	
	// Main database connection parameters

    @Option(name="--db-host", aliases = { "-h" }, usage="The database server to connect to. (default: localhost)")
    private String mainDatabaseHost = "localhost";
    public String getMainDatabaseHost() { return mainDatabaseHost; }

    @Option(name="--db-name", aliases = { "-d" }, usage="The name of the database server to connect to. (default: musicbrainz_db)")
    private String mainDatabaseName = "musicbrainz_db";        
    public String getMainDatabaseName() { return mainDatabaseName; }

    @Option(name="--db-user", aliases = { "-u" }, usage="The username to connect with. (default: musicbrainz_user)")
    private String mainDatabaseUser = "musicbrainz_user";
    public String getMainDatabaseUser() { return mainDatabaseUser; }

    @Option(name="--db-password", aliases = { "-p" }, usage="The password for the db user. (default: -blank-)")
    private String mainDatabasePassword = "";
    public String getMainDatabasePassword() { return mainDatabasePassword; }

    public Connection getMainDatabaseConnection() {
        String url = "jdbc:postgresql://" + getMainDatabaseHost() + "/" + getMainDatabaseName();
        Properties props = new Properties();
        props.setProperty("user", getMainDatabaseUser());
        props.setProperty("password", getMainDatabasePassword());
        Connection c = null;
		try {
			c = DriverManager.getConnection(url, props);
			PrepareDatabase.prepareDbConnection(c);
		} catch (SQLException e) {
			e.printStackTrace();
		}
        
        return c;
    }
    
    // Indexes directory
    @Option(name="--indexes-dir", usage="The directory . (default: ./data/)")
    private String indexesDir = "." + System.getProperty("file.separator") + "data" + System.getProperty("file.separator");
    public String getIndexesDir() {
        if (indexesDir.endsWith(System.getProperty("file.separator"))) return indexesDir; 
        else return indexesDir + System.getProperty("file.separator");
    }

    // FreeDB dump file
    @Option(name="--freedb-dump", usage="The FreeDB dump file to index.")
    private String freeDBDump = "";
    public String getFreeDBDump() { return freeDBDump; }

    // Selection of indexes to build
    @Option(name="--indexes", usage="A comma-separated list of indexes to build (annotation,area,artist,editor,instrument,label,place,releasegroup,release,recording,series,work,tag,url,cdstub,freedb)")
    private String indexes = "annotation,area,artist,editor,instrument,label,place,release,recording,releasegroup,series,work,tag,url,cdstub,freedb";
    public ArrayList<String> selectedIndexes() { return new ArrayList<String>(Arrays.asList(indexes.split(","))); }
    public boolean buildIndex(String indexName) { return selectedIndexes().contains(indexName); }

    // Test mode
    @Option(name="--test", aliases = { "-t" }, usage="Test the index builder by creating small test indexes.")
    private boolean test = false;
    public boolean isTest() { return test; }

    @Option(name="--help", usage="Print this usage information.")
    private boolean help = false;
    public boolean isHelp() { return help; }

    @Option(name="--testindexsize", aliases = { "-b" }, usage="The number of rows to index when using the test option. (default: "+MAX_TEST_ID+")")
    private int testIndexSize = MAX_TEST_ID;
    public int getTestIndexSize() { return testIndexSize; }

    @Option(name="--chunksize", aliases = { "-c" }, usage="Chunk Size, The number of rows to return in each SQL query. (default: -"+IDS_PER_CHUNK+")")
    private int databaseChunkSize = IDS_PER_CHUNK;
    public int getDatabaseChunkSize() { return databaseChunkSize; }

    // Check Open File Limit
    @Option(name="--checkfilelimit", usage="Check Open File Limit, all other options are ignored and no indexes are built.")
    private boolean checkFileLimit = false;
    public boolean isCheckFileLimit() { return checkFileLimit; }

    // Check Open File Limit
    @Option(name="--debug", usage="Debug Mode, provides additional info, only use for debugging because runs slower.")
    private boolean debug = false;
    public boolean isDebug() { return debug; }

    // Max Buffered Docs
    @Option(name="--maxbuffereddocs", usage="The Max Buffered docs before writing to index segment during Indexing. (default: "+MAX_BUFFERED_DOCS+")")
    private int maxBufferedDocs = MAX_BUFFERED_DOCS;
    public int getMaxBufferedDocs() { return maxBufferedDocs; }


}
