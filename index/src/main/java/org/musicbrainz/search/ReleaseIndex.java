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
import java.sql.*;

public class ReleaseIndex extends Index {

	private String[] TYPES = {"unknown", "person", "group"};

    public String getName() {
		return "release";
	}

	public int getMaxId(Connection conn) throws SQLException {
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT MAX(id) FROM album");
		rs.next();
		return rs.getInt(1);
	}

	public void indexData(IndexWriter indexWriter, Connection conn, int min, int max) throws SQLException, IOException {
		Map<Integer, List<List<String>>> events = new HashMap<Integer, List<List<String>>>();
		PreparedStatement st = conn.prepareStatement(
			"SELECT album, lower(isocode), releasedate, label.name, catno, barcode " +
			"FROM release " +
			"LEFT JOIN country ON release.country=country.id " +
			"LEFT JOIN label ON release.label=label.id " +
			"WHERE album BETWEEN ? AND ?");
		st.setInt(1, min);
		st.setInt(2, max);
		ResultSet rs = st.executeQuery();
		while (rs.next()) {
			int albumId = rs.getInt(1);
			List<List<String>> list;
			if (!events.containsKey(albumId)) {
				list = new LinkedList<List<String>>();
				events.put(albumId, list);
			}
			else {
				list = events.get(albumId);
			}
			List<String> entry = new ArrayList<String>(5);
			entry.add(rs.getString(2));
			entry.add(rs.getString(3));
			entry.add(rs.getString(4));
			entry.add(rs.getString(5));
			entry.add(rs.getString(6));
			list.add(entry);
		}
		st.close();
		st = conn.prepareStatement(
			"SELECT album.id, artist.gid, artist.name, " +
			"album.gid, album.name, attributes, tracks, discids, asin, language, script " +
			"FROM album " +
			"JOIN albummeta ON album.id=albummeta.id " +
			"JOIN artist ON album.artist=artist.id " +
			"WHERE album.id BETWEEN ? AND ?");
		st.setInt(1, min);
		st.setInt(2, max);
		rs = st.executeQuery();
		while (rs.next()) {
			indexWriter.addDocument(documentFromResultSet(rs, events));
		}
		st.close();
	}

	public Document documentFromResultSet(ResultSet rs, Map<Integer, List<List<String>>> events) throws SQLException {
		Document doc = new Document();
		int albumId = rs.getInt(1);
		doc.add(new Field("arid", rs.getString(2), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("artist", rs.getString(3), Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("reid", rs.getString(4), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("release", rs.getString(5), Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("tracks", rs.getString(7), Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("discids", rs.getString(8), Field.Store.YES, Field.Index.ANALYZED));
		if (events.containsKey(albumId)) {
			for (List<String> entry: events.get(albumId)) {
				String str;
				str = entry.get(0);
				if (str == null || str.isEmpty())
					str = "-";
				doc.add(new Field("country", str, Field.Store.YES, Field.Index.NOT_ANALYZED));
				str = entry.get(1);
				if (str == null || str.isEmpty())
					str = "-";
				doc.add(new Field("date", normalizeDate(str), Field.Store.YES, Field.Index.NOT_ANALYZED));
				str = entry.get(2);
				if (str == null || str.isEmpty())
					str = "-";
				doc.add(new Field("label", str, Field.Store.YES, Field.Index.ANALYZED));
				str = entry.get(3);
				if (str == null || str.isEmpty())
					str = "-";
				doc.add(new Field("catno", str, Field.Store.YES, Field.Index.ANALYZED));
				str = entry.get(4);
				if (str == null || str.isEmpty())
					str = "-";
				doc.add(new Field("barcode", str, Field.Store.YES, Field.Index.ANALYZED));
			}
		}
		return doc;
	}

}
