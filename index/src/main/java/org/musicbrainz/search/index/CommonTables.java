/*
 * MusicBrainz Search Server
 * Copyright (C) 2009  Aurélien Mino

 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package org.musicbrainz.search.index;

import org.apache.commons.lang.time.StopWatch;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Build temporary tables that are used by multiple indexes
 *
 */
public class CommonTables  {

    protected Connection dbConnection;
    private   String     cacheType;
    private   List<String> indexesToBeBuilt ;


    public CommonTables(Connection dbConnection, String cacheType, String indexToBeBuilt) {
        this.dbConnection=dbConnection;
        this.cacheType=cacheType;
        this.indexesToBeBuilt= new ArrayList<String>();
        this.indexesToBeBuilt.add(indexToBeBuilt);
    }

    public CommonTables(Connection dbConnection, String cacheType, List<String> indexesToBeBuilt) {
        this.dbConnection=dbConnection;
        this.cacheType=cacheType;
        this.indexesToBeBuilt= indexesToBeBuilt;
    }

    public Connection getDbConnection() {
            return dbConnection;
        }

    /**
     * Create table showing all artist credits, then create index
     * for the table.
     *
     * @throws SQLException
     */
    private void createArtistCreditdTableUsingDb() throws SQLException
    {
        System.out.println(" Started populating tmp_artistcredit temporary table");
        StopWatch clock = new StopWatch();
        clock.start();
        getDbConnection().createStatement().execute(
            "CREATE TEMPORARY TABLE tmp_artistcredit AS " +
                "SELECT acn.artist_credit as artist_credit, " +
                "  acn.position as pos, " +
                "  acn.join_phrase as joinphrase, " +
                "  a.gid as artistId,  " +
                "  a.comment as comment, " +
                "  an.name as artistName, " +
                "  an2.name as artistCreditName, " +
                "  an3.name as artistSortName " +
                " FROM artist_credit_name acn  " +
                "  INNER JOIN artist a ON a.id=acn.artist " +
                "  INNER JOIN artist_name an ON a.name=an.id " +
                "  INNER JOIN artist_name an2 ON acn.name=an2.id " +
                "  INNER JOIN artist_name an3 ON a.sort_name=an3.id " +
                " ORDER BY acn.artist_credit,acn.position ");
        clock.stop();
        System.out.println(" Populated tmp_artistcredit temporary table in " + Float.toString(clock.getTime()/1000) + " seconds");
        clock.reset();

        clock.start();
        getDbConnection().createStatement().execute(
             "CREATE INDEX tmp_artistcredit_idx ON tmp_artistcredit (artist_credit) ");
        clock.stop();
        System.out.println(" Created indexes on tmp_artist_credit in " + Float.toString(clock.getTime()/1000) + " seconds");
        clock.reset();
    }

    /**
     * Create table mapping a release to all that puids that tracks within the release contain, then create index
     * for the table..
     *
     * @throws SQLException
     */
    private void createReleasePuidTableUsingDb() throws SQLException
    {
        System.out.println(" Started populating tmp_release_puid temporary table");
        StopWatch clock = new StopWatch();
        clock.start();
        getDbConnection().createStatement().execute(
            "CREATE TEMPORARY TABLE tmp_release_puid AS " +
            "  SELECT m.release, rp.recording, p.puid " +
            "  FROM medium m " +
            "    INNER JOIN track t ON t.tracklist = m.tracklist " +
            "    INNER JOIN recording_puid rp ON rp.recording = t.recording " +
            "    INNER JOIN puid p ON rp.puid = p.id");
        clock.stop();
        System.out.println(" Populated tmp_release_puid temporary table in " + Float.toString(clock.getTime()/1000) + " seconds");
        clock.reset();

        clock.start();
        getDbConnection().createStatement().execute(
                "CREATE INDEX tmp_release_puid_idx_release ON tmp_release_puid (release) ");
        getDbConnection().createStatement().execute(
             "CREATE INDEX tmp_release_puid_idx_recording ON tmp_release_puid (recording) ");

        clock.stop();
        System.out.println(" Created indexes on tmp_release_puid in " + Float.toString(clock.getTime()/1000) + " seconds");
        clock.reset();
    }

    public void createTemporaryTables()  throws SQLException
    {
        if(
            (indexesToBeBuilt.contains(ReleaseIndex.INDEX_NAME))||
            (indexesToBeBuilt.contains(ReleaseGroupIndex.INDEX_NAME))||
            (indexesToBeBuilt.contains(RecordingIndex.INDEX_NAME))||
            (indexesToBeBuilt.contains(WorkIndex.INDEX_NAME))
          )
        {
            createArtistCreditdTableUsingDb();
        }

        if(
           (indexesToBeBuilt.contains(ReleaseIndex.INDEX_NAME))||
           (indexesToBeBuilt.contains(RecordingIndex.INDEX_NAME))
          )
        {
            if(cacheType.equals(CacheType.TEMPTABLE))
            {
                createReleasePuidTableUsingDb();
            }
        }

    }
}
