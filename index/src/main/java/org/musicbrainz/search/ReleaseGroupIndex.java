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

package org.musicbrainz.search;

import java.io.*;
import java.util.*;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.Document;

import java.sql.*;

public class ReleaseGroupIndex extends Index {

	public ReleaseGroupIndex(Connection dbConnection) {
		super(dbConnection);
	}

	public String getName() {
		return "releasegroup";
	}

	public int getMaxId() throws SQLException {
		Statement st = dbConnection.createStatement();
		ResultSet rs = st.executeQuery("SELECT MAX(id) FROM release_group");
		rs.next();
		return rs.getInt(1);
	}

	public void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException {

		Map<Integer, List<String>> releases = new HashMap<Integer, List<String>>();
		PreparedStatement st = dbConnection.prepareStatement(
				"SELECT DISTINCT release_group, name " +
					"FROM album " + 
					"WHERE release_group BETWEEN ? AND ?");
		st.setInt(1, min);
		st.setInt(2, max);
		ResultSet rs = st.executeQuery();
		while (rs.next()) {
			int rgId = rs.getInt("release_group");
			List<String> list;
			if (!releases.containsKey(rgId)) {
				list = new LinkedList<String>();
				releases.put(rgId, list);
			} else {
				list = releases.get(rgId);
			}
			list.add(rs.getString("name"));
		}
		st.close();

		st = dbConnection.prepareStatement(
				"SELECT rg.id, rg.gid, rg.name, rg.type, " +
					"artist.gid as artist_gid, artist.name as artist_name " +
					"FROM release_group AS rg " +
					"JOIN artist ON rg.artist=artist.id " +
					"WHERE rg.id BETWEEN ? AND ?");
		st.setInt(1, min);
		st.setInt(2, max);
		rs = st.executeQuery();
		while (rs.next()) {
			indexWriter.addDocument(documentFromResultSet(rs, releases));
		}
		st.close();

	}

	public Document documentFromResultSet(ResultSet rs, Map<Integer, List<String>> releases) throws SQLException {
		Document doc = new Document();
		int rgId = rs.getInt("id");
		addFieldToDocument(doc, ReleaseGroupIndexField.RELEASEGROUP_ID, rs.getString("gid"));
		addFieldToDocument(doc, ReleaseGroupIndexField.RELEASEGROUP, rs.getString("name"));
		addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST_ID, rs.getString("artist_gid"));
		addFieldToDocument(doc, ReleaseGroupIndexField.ARTIST, rs.getString("artist_name"));
        addFieldToDocument(doc, ReleaseGroupIndexField.TYPE, ReleaseGroupType.getByDbId(rs.getInt("type")).getName());

		if (releases.containsKey(rgId)) {
			for (String release : releases.get(rgId)) {
				addFieldToDocument(doc, ReleaseGroupIndexField.RELEASES, release);
			}
		}
		return doc;
	}

}
