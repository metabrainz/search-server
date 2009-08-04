package org.musicbrainz.search;

import junit.framework.TestCase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.BatchUpdateException;


public abstract class AbstractIndexTest extends TestCase {

    protected Connection createConnection() throws Exception {
        return DriverManager.getConnection("jdbc:derby:testsearchindexdb;create=true");

    }

    public void setup() throws Exception
    {
        try {
            Connection conn = createConnection();
            conn.setAutoCommit(true);

            //Drop tables, if they exist
            try {
                Statement stmt = conn.createStatement();
                stmt.addBatch("DROP TABLE country");
                stmt.addBatch("DROP TABLE artist");
                stmt.addBatch("DROP TABLE artistalias");
                stmt.addBatch("DROP TABLE label");
                stmt.addBatch("DROP TABLE labelalias");
                stmt.addBatch("DROP TABLE album");
                stmt.addBatch("DROP TABLE release_group");
                stmt.addBatch("DROP TABLE annotation");
                stmt.executeBatch();
                stmt.close();
            }
            catch (BatchUpdateException bue) {
                //Ignore errors, because will just be that tables does not exist
                System.err.println(bue.getMessage());
            }

            //Create tables and data for Artist Index
            Statement stmt = conn.createStatement();

            stmt.addBatch("CREATE TABLE country" +
                    "(" +
                    "  id integer NOT NULL," +
                    "  isocode character varying(2) NOT NULL," +
                    "  name character varying(100) NOT NULL" +
                    ")");

           stmt.addBatch("CREATE TABLE artistalias (" +
                    "  id integer NOT NULL," +
                    "  ref integer NOT NULL," +
                    "  name character varying(255) NOT NULL," +
                    "  timesused integer DEFAULT 0," +
                    "  modpending integer DEFAULT 0," +
                    "  lastused timestamp" +
                    ")");
            stmt.addBatch("CREATE TABLE artist (" +
                    "  id integer NOT NULL," +
                    "  name character varying(255) NOT NULL," +
                    "  gid character(36) NOT NULL," +
                    "  modpending integer DEFAULT 0," +
                    "  sortname character varying(255) NOT NULL," +
                    "  page integer NOT NULL," +
                    "  resolution character varying(64)," +
                    "  begindate character(10)," +
                    "  enddate character(10)," +
                    "  type smallint," +
                    "  quality smallint DEFAULT -1," +
                    "  modpending_qual integer DEFAULT 0" +
                    ")");

            stmt.addBatch("CREATE TABLE labelalias (" +
                    "  id integer NOT NULL," +
                    "  ref integer NOT NULL," +
                    "  name character varying(255) NOT NULL," +
                    "  timesused integer DEFAULT 0," +
                    "  modpending integer DEFAULT 0," +
                    "  lastused timestamp" +
                    ")");

            stmt.addBatch("CREATE TABLE label (" +
                    "  id integer NOT NULL," +
                    "  name character varying(255) NOT NULL," +
                    "  gid character(36) NOT NULL," +
                    "  modpending integer DEFAULT 0," +
                    "  labelcode integer," +
                    "  sortname character varying(255) NOT NULL," +
                    "  country integer," +
                    "  page integer NOT NULL," +
                    "  resolution character varying(64)," +
                    "  begindate character(10)," +
                    "  enddate character(10)," +
                    "  type smallint" +
                    ")");

            stmt.addBatch("CREATE TABLE album" +
                    "(" +
                    "  id integer NOT NULL," +
                   "  artist integer NOT NULL," +
                   "  name character varying(255) NOT NULL," +
                    "  gid character(36) NOT NULL," +
                    "  modpending integer DEFAULT 0," +
                    //"  attributes integer[] DEFAULT '{0}'::integer[]," +
                    "  page integer NOT NULL," +
                    "  language integer," +
                    "  script integer," +
                    "  modpending_lang integer," +
                    "  quality smallint ," +
                    "  modpending_qual integer DEFAULT 0," +
                    "  release_group integer NOT NULL" +
                    ")");

             stmt.addBatch("CREATE TABLE release_group" +
                     "(" +
                     "  id integer NOT NULL," +
                     "  gid character(36)," +
                     "  name character varying(255)," +
                     "  page integer NOT NULL," +
                     "  artist integer NOT NULL," +
                     "  type integer," +
                     "  modpending integer DEFAULT 0" +
                     ")");

            stmt.addBatch("CREATE TABLE annotation" +
                    "(" +
                    "  id integer NOT NULL," +
                    "  moderator integer NOT NULL," +
                    "  type smallint NOT NULL," +
                    "  rowid integer NOT NULL," +
                    "  text varchar(1000)," +
                    "  changelog character varying(255)," +
                    "  created timestamp," +
                    "  moderation integer NOT NULL DEFAULT 0," +
                    "  modpending integer NOT NULL DEFAULT 0" +
                    ")");

            stmt.executeBatch();
            stmt.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

}
