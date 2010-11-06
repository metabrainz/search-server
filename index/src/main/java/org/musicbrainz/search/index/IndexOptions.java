package org.musicbrainz.search.index;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import org.kohsuke.args4j.Option;

public class IndexOptions {

    private static final int MAX_TEST_ID = 50000;
    private static final int IDS_PER_CHUNK = 20000;

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

    public Connection getMainDatabaseConnection() throws SQLException {
        String url = "jdbc:postgresql://" + getMainDatabaseHost() + "/" + getMainDatabaseName();
        Properties props = new Properties();
        props.setProperty("user", getMainDatabaseUser());
        props.setProperty("password", getMainDatabasePassword());
        Connection c = DriverManager.getConnection(url, props);
        prepareDbConnection(c);
        return c;
    }
    
    // Raw database connection parameters

    @Option(name="--raw-db-host", aliases = { "-o" }, usage="The raw database server to connect to. (default: localhost)")
    private String rawDatabaseHost = "";
    public String getRawDatabaseHost() { return rawDatabaseHost.isEmpty() ? getMainDatabaseHost() : rawDatabaseHost; }

    @Option(name="--raw-db-name", aliases = { "-a" }, usage="The name of the raw database server to connect to. (default: musicbrainz_db_raw)")
    private String rawDatabaseName = "musicbrainz_db_raw";     
    public String getRawDatabaseName() { return rawDatabaseName; }

    @Option(name="--raw-db-user", aliases = { "-s" }, usage="The username for the raw database to connect with. (default: musicbrainz_user)")
    private String rawDatabaseUser = "musicbrainz_user";
    public String getRawDatabaseUser() { return rawDatabaseUser; }

    @Option(name="--raw-db-password", aliases = { "-w" }, usage="The password of the db user of the raw database. (default: -blank-)")
    private String rawDatabasePassword = "";
    public String getRawDatabasePassword() { return rawDatabasePassword; }

    public Connection getRawDatabaseConnection() throws SQLException {
        String url = "jdbc:postgresql://" + getRawDatabaseHost() + "/" + getRawDatabaseName();
        Properties props = new Properties();
        props.setProperty("user", getRawDatabaseUser());
        props.setProperty("password", getRawDatabasePassword());
        Connection c = DriverManager.getConnection(url, props);
        prepareDbConnection(c);
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
    @Option(name="--indexes", usage="A comma-separated list of indexes to build (artist,releasegroup,release,recording,label,work,tag,annotation,cdstub,freedb)")
    private String indexes = "artist,label,release,recording,releasegroup,work,tag,annotation,cdstub,freedb";
    public ArrayList<String> selectedIndexes() { return new ArrayList<String>(Arrays.asList(indexes.split(","))); }
    public boolean buildIndex(String indexName) { return selectedIndexes().contains(indexName); }

    // Test mode
    @Option(name="--test", aliases = { "-t" }, usage="Test the index builder by creating small text indexes.")
    private boolean test = false;
    public boolean isTest() { return test; }

    @Option(name="--help", usage="Print this usage information.")
    private boolean help = false;
    public boolean isHelp() { return help; }

    @Option(name="--testindexsize", aliases = { "-b" }, usage="The number of rows to index when using the test option. (default: -10000)")
    private int testIndexSize = MAX_TEST_ID;
    public int getTestIndexSize() { return testIndexSize; }

    @Option(name="--chunksize", aliases = { "-c" }, usage="Chunk Size, The number of rows to return in each SQL query. (default: -10000)")
    private int databaseChunkSize = IDS_PER_CHUNK;
    public int getDatabaseChunkSize() { return databaseChunkSize; }
    
    
    /**
     * Prepare a database connection, and set its default Postgres schema
     * 
     * @param connection
     * @throws SQLException 
     */
    private static void prepareDbConnection(Connection connection) throws SQLException
    {
		Statement st = connection.createStatement();
        // Forces Query Analyser to take advantage of indexes when they exist, this works round the problem with the
        // explain sometimes deciding to do full table scans when building recording index causing query to run unacceptably slow.
        st.executeUpdate("SET enable_seqscan = off");
		st.executeUpdate("SET search_path TO '" + IndexOptions.DB_SCHEMA + "'");
    }
    
}
