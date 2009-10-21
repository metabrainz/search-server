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
import org.apache.lucene.document.NumericField;
import org.apache.lucene.document.NumberTools;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.NumericUtils;

public abstract class Index {

	protected Connection dbConnection;
	
	public Index(Connection dbConnection) {
		this.dbConnection = dbConnection;
	}
	
	public abstract int getMaxId() throws SQLException;
    public abstract int getNoOfRows(int maxId) throws SQLException ;
    public abstract String getName();
    public abstract Analyzer getAnalyzer();
    public abstract void indexData(IndexWriter indexWriter, int min, int max) throws SQLException, IOException;

	protected static String normalizeDate(String date) {
		return date.replace("-00", "");
	}

    /**
     * Add numeric field to document, handled specailly so that ranges searches work properly
     * @param doc
     * @param field
     * @param value
     */
    public static void addNumericFieldToDocument(Document doc, IndexField field, Integer value) {
        addFieldToDocument(doc,field,NumericUtils.intToPrefixCoded(value));
    }

    /**
     * Add field to document
     *
     * @param doc
     * @param field
     * @param value
     */
    public static void addFieldToDocument(Document doc, IndexField field, String value) {
		doc.add(new Field(field.getName(), value, field.getStore(), field.getIndex()));
	}

    /**
     * Add field to document if not empty
     *
     * @param doc
     * @param field
     * @param value
     */
    public static void addNonEmptyFieldToDocument(Document doc, IndexField field, String value) {
        if (value != null && !value.isEmpty()) {
        	doc.add(new Field(field.getName(), value, field.getStore(), field.getIndex()));
        }
	}

    /**
     * Add field to document if not empty, otherwise add hyphen.
     *
     * This method is necessary when adding fields that make up a set within in a list so that
     * order is preserved.
     *
     * @param doc
     * @param field
     * @param value
     */
    //TODO is thia way we can achieve the same effect without needing to store these fields
    public static void addFieldOrHyphenToDocument(Document doc, IndexField field, String value) {
        if (value != null && !value.isEmpty()) {
        	doc.add(new Field(field.getName(), value, field.getStore(), field.getIndex()));
        }
        else {
           doc.add(new Field(field.getName(), "-", field.getStore(), field.getIndex()));
        }

    }

}
