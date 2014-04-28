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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
     * for the table, a merge of required artist credit_name & artist information
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
                "  a.id," +
                "  a.gid as artistId,  " +
                "  a.comment as comment, " +
                "  a.name as artistName, " +
                "  acn.name as artistCreditName, " +
                "  a.sort_name as artistSortName " +
                " FROM artist_credit_name acn  " +
                "  INNER JOIN artist a ON a.id=acn.artist " +
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

    private void createReleaseTableUsingDb() throws SQLException {
        System.out.println("tmp_release     :Started at:" + Utils.formatCurrentTimeForOutput());
        StopWatch clock = new StopWatch();
        clock.start();

        getDbConnection().createStatement().execute(
            "CREATE TEMPORARY TABLE tmp_release AS " +
                "SELECT r.id, r.gid, r.name as name, " +
                "  barcode, " +
                "  rgt.name as type, rg.id as rg_id, rg.gid as rg_gid, rm.amazon_asin, " +
                "  language.iso_code_3 as language, language.iso_code_2t as language_2t, script.iso_code as script, rs.name as status, " +
                "  sum(m.track_count) as tracks," +
                "  r.artist_credit, r.quality,rp.name as packaging," +
                "  r.comment" +
                " FROM release r " +
                "  LEFT JOIN release_meta rm ON r.id = rm.id " +
                "  LEFT JOIN release_group rg ON rg.id = r.release_group " +
                "  LEFT JOIN release_group_primary_type rgt  ON rg.type = rgt.id " +
                "  LEFT JOIN release_status rs ON r.status = rs.id " +
                "  LEFT JOIN language ON r.language=language.id " +
                "  LEFT JOIN script ON r.script=script.id " +
                "  LEFT JOIN medium m ON m.release=r.id" +
                "  LEFT JOIN release_packaging rp ON r.packaging = rp.id " +
                " GROUP BY r.id,r.gid,r.name,barcode,rgt.name,rg.id, rg.gid," +
                "  rm.amazon_asin, language.iso_code_3, language.iso_code_2t, script.iso_code,rs.name,r.artist_credit, r.quality, rp.name,r.comment");
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

    private void createReleaseEventsTableUsingDb() throws SQLException {
        System.out.println("tmp_release_event     :Started at:" + Utils.formatCurrentTimeForOutput());
        StopWatch clock = new StopWatch();
        clock.start();

        //Note:assumes a release country always only maps to an area with a single 3166_1 code
        getDbConnection().createStatement().execute(
                "CREATE TEMPORARY TABLE tmp_release_event AS " +
                        " SELECT r1.release, r2.code as country, " +
                        "  r1.date_year, r1.date_month, r1.date_day," +
                        "  a1.gid as gid, a1.name as name" +
                        " FROM release_country r1 " +
                        " LEFT JOIN area a1 " +
                        " ON r1.country = a1.id" +
                        " LEFT JOIN iso_3166_1 r2 " +
                        " ON a1.id = r2.area " +
                        " UNION" +
                        " SELECT release, null as country, " +
                        "  date_year, date_month, date_day," +
                        "  null as gid, null as name"+
                        " FROM release_unknown_country r1 ");
        clock.stop();
        System.out.println("tmp_release_event     :Finished:" + Utils.formatClock(clock));
        clock.reset();

        clock.start();
        getDbConnection().createStatement().execute(
                "CREATE INDEX tmp_release_event_idx_release ON tmp_release_event (release) ");
        clock.stop();
        System.out.println("tmp_release_event     :Created Indexes:" + Utils.formatClock(clock));
        clock.reset();
    }

    private void createTrackTableUsingDb() throws SQLException
    {
        System.out.println("tmp_track       :Started at:" + Utils.formatCurrentTimeForOutput());
        StopWatch clock = new StopWatch();
        clock.start();

        getDbConnection().createStatement().execute(
            "CREATE TEMPORARY TABLE tmp_track AS " +
                "SELECT t.id, t.gid, t.recording, t.length, t.name as track_name, t.position as track_position, t.number as track_number, m.track_count, " +
                "  m.release as release_id, m.position as medium_position, mf.name as format " +
                " FROM track t " +
                "  INNER JOIN medium m ON t.medium=m.id " +
                "  LEFT JOIN  medium_format mf ON m.format=mf.id ");

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
            (indexesToBeBuilt.contains(RecordingIndex.INDEX_NAME))
          )
        {
            createArtistCreditTableUsingDb();
        }


        if(
           (indexesToBeBuilt.contains(ReleaseIndex.INDEX_NAME))||
           (indexesToBeBuilt.contains(RecordingIndex.INDEX_NAME))
          )
        {
            createReleaseTableUsingDb();
            createReleaseEventsTableUsingDb();
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
