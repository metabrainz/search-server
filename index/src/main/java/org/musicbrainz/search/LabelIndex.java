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

public class LabelIndex extends Index {

	private String[] TYPES = {"unknown", "distributor", "holding", "production",
		"orig. prod.", "bootleg prod.", "reissue prod.", "publisher"};

    public String getName() {
		return "label";
	}

	public int getMaxId(Connection conn) throws SQLException {
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT MAX(id) FROM label");
		rs.next();
		return rs.getInt(1);
	}

	public void indexData(IndexWriter indexWriter, Connection conn, int min, int max) throws SQLException, IOException {
		Map<Integer, List<String>> aliases = new HashMap<Integer, List<String>>();
		PreparedStatement st = conn.prepareStatement("SELECT ref, name FROM labelalias WHERE ref BETWEEN ? AND ?");
		st.setInt(1, min);
		st.setInt(2, max);
		ResultSet rs = st.executeQuery();
		while (rs.next()) {
			int labelId = rs.getInt(1);
			List<String> list;
			if (!aliases.containsKey(labelId)) {
				list = new LinkedList<String>();
				aliases.put(labelId, list);
			}
			else {
				list = aliases.get(labelId);
			}
			list.add(rs.getString(2));
		}
		st.close();
		st = conn.prepareStatement(
			"SELECT label.id, gid, label.name, sortname, type, begindate, enddate, resolution, labelcode, lower(isocode) " +
			"FROM label JOIN country ON label.country=country.id WHERE label.id BETWEEN ? AND ?");
		st.setInt(1, min);
		st.setInt(2, max);
		rs = st.executeQuery();
		while (rs.next()) {
			indexWriter.addDocument(documentFromResultSet(rs, aliases));
		}
		st.close();
	}

	public Document documentFromResultSet(ResultSet rs, Map<Integer, List<String>> aliases) throws SQLException {
		Document doc = new Document();
		int labelId = rs.getInt(1);
		doc.add(new Field("laid", rs.getString(2), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("label", rs.getString(3), Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("sortname", rs.getString(4), Field.Store.YES, Field.Index.ANALYZED));
		Integer type = rs.getInt(5);
		if (type == null)
			type = 0;
		doc.add(new Field("type", TYPES[type], Field.Store.YES, Field.Index.NOT_ANALYZED));
		String begin = rs.getString(6);
		if (begin != null && !begin.isEmpty())
			doc.add(new Field("begin", normalizeDate(begin), Field.Store.YES, Field.Index.NOT_ANALYZED));
		String end = rs.getString(7);
		if (end != null && !end.isEmpty())
			doc.add(new Field("end", normalizeDate(end), Field.Store.YES, Field.Index.NOT_ANALYZED));
		String comment = rs.getString(8);
		if (comment != null && !comment.isEmpty()) {
			doc.add(new Field("comment", comment, Field.Store.YES, Field.Index.ANALYZED));
		}
		String code = rs.getString(9);
		if (code != null && !code.isEmpty()) {
			doc.add(new Field("code", code, Field.Store.YES, Field.Index.NOT_ANALYZED));
		}
		String country = rs.getString(10);
		if (country != null && !country.isEmpty()) {
			doc.add(new Field("country", country, Field.Store.YES, Field.Index.NOT_ANALYZED));
		}
		if (aliases.containsKey(labelId)) {
			for (String alias: aliases.get(labelId)) {
				doc.add(new Field("alias", alias, Field.Store.NO, Field.Index.ANALYZED));
			}
		}
		return doc;
	}

}
