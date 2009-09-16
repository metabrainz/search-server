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

package org.musicbrainz.search.index;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

public abstract class Index {

	protected Connection dbConnection;
	
	public Index(Connection dbConnection) {
		this.dbConnection = dbConnection;
	}
	
	public abstract int getMaxId() throws SQLException;
    public abstract String getName();
	public abstract void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException;

	protected static String normalizeDate(String date) {
		return date.replace("-00", "");
	}
	
	public static void addFieldToDocument(Document doc, IndexField field, String value) {
		doc.add(new Field(field.getName(), value, field.getStore(), field.getIndex()));
	}

}
