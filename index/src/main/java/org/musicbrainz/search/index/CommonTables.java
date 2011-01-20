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
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.util.NumericUtils;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.io.IOException;
import java.sql.*;

/**
 * Build temporary tables that are used by multiple indexes
 *
 */
public class CommonTables  {

    protected Connection dbConnection;

    public CommonTables(Connection dbConnection) {
        this.dbConnection=dbConnection;
    }

    public Connection getDbConnection() {
            return dbConnection;
        }

    /**
     * Create table mapping a release to all that puids that tracks within the release contain, then create index
     * and vacuum analyze the table ready for use.
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
        System.out.println(" Created index on tmp_artist_credit in " + Float.toString(clock.getTime()/1000) + " seconds");
        clock.reset();
    }

    public void createTemporaryTables()  throws SQLException
    {
        createArtistCreditdTableUsingDb();
    }
}
