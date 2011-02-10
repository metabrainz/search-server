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
import java.util.*;

/**
 * Build temporary tables that are used by multiple indexes
 *
 */
public class CommonTables  {

    protected Connection dbConnection;
    private   List<String> indexesToBeBuilt ;


    public CommonTables(Connection dbConnection, String indexToBeBuilt) {
        this.dbConnection=dbConnection;
        this.indexesToBeBuilt= new ArrayList<String>();
        this.indexesToBeBuilt.add(indexToBeBuilt);
    }

    public CommonTables(Connection dbConnection, List<String> indexesToBeBuilt) {
        this.dbConnection=dbConnection;
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
    private void createArtistCreditTableUsingDb() throws SQLException
    {
        System.out.println("tmp_artistcredit:Started at:" + Utils.formatCurrentTimeForOutput());
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
        System.out.println("tmp_artistcredit:Finished:"+ Utils.formatClock(clock));
        clock.reset();

        clock.start();
        getDbConnection().createStatement().execute(
             "CREATE INDEX tmp_artistcredit_idx ON tmp_artistcredit (artist_credit) ");
        clock.stop();
        System.out.println("tmp_artistcredit:Created Indexes:"+Utils.formatClock(clock));
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
        System.out.println("tmp_release_puid:Started at:" + Utils.formatCurrentTimeForOutput());
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
        System.out.println("tmp_release_puid:Finished:"+ Utils.formatClock(clock));
        clock.reset();

        clock.start();
        getDbConnection().createStatement().execute(
                "CREATE INDEX tmp_release_puid_idx_release ON tmp_release_puid (release) ");
        getDbConnection().createStatement().execute(
             "CREATE INDEX tmp_release_puid_idx_recording ON tmp_release_puid (recording) ");

        clock.stop();
        System.out.println("tmp_release_puid:Created Indexes:"+ Utils.formatClock(clock));
        clock.reset();
    }


    private void createReleaseTableUsingDb() throws SQLException
    {
        System.out.println("tmp_release     :Started at:" + Utils.formatCurrentTimeForOutput());
        StopWatch clock = new StopWatch();
        clock.start();

        getDbConnection().createStatement().execute(
            "CREATE TEMPORARY TABLE tmp_release AS " +
                "SELECT r.id, r.gid, rn.name as name, " +
                "  barcode, lower(country.iso_code) as country, " +
                "  date_year, date_month, date_day, rgt.name as type, rm.amazon_asin, " +
                "  language.iso_code_3t as language, script.iso_code as script, rs.name as status, " +
                "  sum(tr.track_count) as tracks" +
                " FROM release r " +
                "  LEFT JOIN release_meta rm ON r.id = rm.id " +
                "  LEFT JOIN release_group rg ON rg.id = r.release_group " +
                "  LEFT JOIN release_group_type rgt  ON rg.type = rgt.id " +
                "  LEFT JOIN country ON r.country=country.id " +
                "  LEFT JOIN release_name rn ON r.name = rn.id " +
                "  LEFT JOIN release_status rs ON r.status = rs.id " +
                "  LEFT JOIN language ON r.language=language.id " +
                "  LEFT JOIN script ON r.script=script.id " +
                "  LEFT JOIN medium m ON m.release=r.id" +
                "  LEFT JOIN tracklist tr ON m.tracklist=tr.id " +
                " GROUP BY r.id,r.gid,rn.name,barcode,country.iso_code,date_year,date_month,date_day,rgt.name," +
                "  rm.amazon_asin, language.iso_code_3t, script.iso_code,rs.name");
        clock.stop();
        System.out.println("tmp_release     :Finished:" + Utils.formatClock(clock));
        clock.reset();

        clock.start();
        getDbConnection().createStatement().execute(
                "CREATE INDEX tmp_release_idx_release ON tmp_release (id) ");
        clock.stop();
        System.out.println("tmp_release     :Created Indexes:" + Utils.formatClock(clock));
        clock.reset();
    }


    private void createTrackTableUsingDb() throws SQLException
    {
        System.out.println("tmp_track       :Started at:" + Utils.formatCurrentTimeForOutput());
        StopWatch clock = new StopWatch();
        clock.start();

        getDbConnection().createStatement().execute(
            "CREATE TEMPORARY TABLE tmp_track AS " +
                "SELECT t.recording, tn.name as track_name, t.position as track_position, tl.track_count, " +
                "  m.release as release_id, m.position as medium_position " +
                " FROM track t " +
                "  INNER JOIN track_name tn ON t.name=tn.id" +
                "  INNER JOIN tracklist tl ON t.tracklist=tl.id " +
                "  INNER JOIN medium m ON m.tracklist=tl.id ");

        clock.stop();
        System.out.println("tmp_track       :Finished:" + Utils.formatClock(clock));
        clock.reset();

        clock.start();
        getDbConnection().createStatement().execute(
                "CREATE INDEX tmp_track_idx_recording ON tmp_track (recording) ");
        clock.stop();
        System.out.println("tmp_track       :Created Indexes"+ Utils.formatClock(clock));
        clock.reset();
    }


    public void createTemporaryTables(boolean isUpdater)  throws SQLException
    {

        if(
            (indexesToBeBuilt.contains(ReleaseIndex.INDEX_NAME))||
            (indexesToBeBuilt.contains(ReleaseGroupIndex.INDEX_NAME))||
            (indexesToBeBuilt.contains(RecordingIndex.INDEX_NAME))||
            (indexesToBeBuilt.contains(WorkIndex.INDEX_NAME))
          )
        {
            createArtistCreditTableUsingDb();
        }


        if(
           (indexesToBeBuilt.contains(ReleaseIndex.INDEX_NAME))||
           (indexesToBeBuilt.contains(RecordingIndex.INDEX_NAME))
          )
        {
            if(!isUpdater)
            {
                createReleasePuidTableUsingDb();
            }

            createReleaseTableUsingDb();
        }

        if(
           (indexesToBeBuilt.contains(RecordingIndex.INDEX_NAME))
          )
        {
            if(!isUpdater)
            {
                createTrackTableUsingDb();
            }
        }
    }
}
