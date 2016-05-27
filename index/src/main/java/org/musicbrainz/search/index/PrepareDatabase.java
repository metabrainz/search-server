package org.musicbrainz.search.index;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class PrepareDatabase {

    /**
     * Prepare a database connection, and set its default Postgres schema
     *
     * @param connection
     * @throws java.sql.SQLException
     */
    public static void prepareDbConnection(Connection connection) throws SQLException
    {
		Statement st = connection.createStatement();

        // Forces Query Analyser to take advantage of indexes when they exist, this works round the problem with the
        // explain sometimes deciding to do full table scans when building recording index causing query to run unacceptably slow.
        st.executeUpdate("SET enable_seqscan = off");

        //Tables within this schema
		st.executeUpdate("SET search_path TO '" + IndexOptions.DB_SCHEMA + "','public'");
		st.executeUpdate("SET statement_timeout = 0");
    }
}
