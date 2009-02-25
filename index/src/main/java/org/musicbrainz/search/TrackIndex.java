/*
 * MusicBrainz Search Server
 * Copyright (C) 2009  Lukas Lalinsky

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

package org.musicbrainz.search;

import java.io.*;
import java.util.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.NumberTools;
import java.sql.*;

public class TrackIndex extends Index {

    public String getName() {
		return "track";
	}

	public int getMaxId(Connection conn) throws SQLException {
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT MAX(id) FROM track");
		rs.next();
		return rs.getInt(1);
	}

	public void indexData(IndexWriter indexWriter, Connection conn, int min, int max) throws SQLException, IOException {
		PreparedStatement st = conn.prepareStatement(
			"SELECT artist.gid, artist.name, " +
			"track.gid, track.name, " +
			"album.gid, album.name, track.length, albumjoin.sequence " +
			"FROM track " +
			"JOIN artist ON track.artist=artist.id " +
			"JOIN albumjoin ON track.id=albumjoin.track " +
			"JOIN album ON album.id=albumjoin.album " +
			"WHERE track.id BETWEEN ? AND ?");
		st.setInt(1, min);
		st.setInt(2, max);
		ResultSet rs = st.executeQuery();
		while (rs.next()) {
			indexWriter.addDocument(documentFromResultSet(rs));
		}
		st.close();
	}

	public Document documentFromResultSet(ResultSet rs) throws SQLException {
		Document doc = new Document();
		doc.add(new Field("arid", rs.getString(1), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("artist", rs.getString(2), Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("trid", rs.getString(3), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("track", rs.getString(4), Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("reid", rs.getString(5), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("release", rs.getString(6), Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("dur", NumberTools.longToString(rs.getLong(7)), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("qdur", NumberTools.longToString(rs.getLong(7) / 2000), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("tnum", NumberTools.longToString(rs.getLong(8)), Field.Store.YES, Field.Index.NOT_ANALYZED));
		//doc.add(new Field("type", XXX, Field.Store.YES, Field.Index.NOT_ANALYZED));
		return doc;
	}

}
