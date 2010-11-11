/*
 * MusicBrainz Search Server
 * Copyright (C) 2009  Paul Taylor

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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Field;

/**
 * Fields common to all Lucene Search Index
 */
public enum MetaIndexField implements IndexField {

    LAST_UPDATED			("index_lastupdate",	Field.Store.YES,	Field.Index.NOT_ANALYZED),
    REPLICATION_SEQUENCE	("index_repseq",		Field.Store.YES,	Field.Index.NOT_ANALYZED),
    SCHEMA_SEQUENCE			("index_schseq",		Field.Store.YES,	Field.Index.NOT_ANALYZED),
    LAST_CHANGE_SEQUENCE	("index_changeseq",		Field.Store.YES,	Field.Index.NOT_ANALYZED),    
    // Dumb field always filled with '1', but that easily allow to find the meta document
    META					("index_meta",			Field.Store.YES,	Field.Index.NOT_ANALYZED),    
    ;

    private String name;
	private Field.Store store;
    private Field.Index index;
    private Analyzer analyzer;

    public static final String META_VALUE = "1";

    private MetaIndexField(String name, Field.Store store, Field.Index index) {
        this.name = name;
        this.store = store;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public Field.Store getStore() {
		return store;
	}

	public Field.Index getIndex() {
		return index;
	}

    public Analyzer getAnalyzer() {
        return analyzer;
    }
}

