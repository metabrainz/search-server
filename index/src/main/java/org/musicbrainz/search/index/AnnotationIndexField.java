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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Field;

/**
 * Fields created in Lucene Search Index
 */
public enum AnnotationIndexField implements IndexField {

	ID		("_id",		Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
	ENTITY	("entity",	Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
	NAME	("name",	Field.Store.YES,	Field.Index.ANALYZED),
	TYPE	("type",	Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
	TEXT	("text",	Field.Store.YES,	Field.Index.ANALYZED);

	private String name;
	private Field.Store store;
	private Field.Index index;
    private Analyzer analyzer;

	private AnnotationIndexField(String name, Field.Store store, Field.Index index) {
		this.name = name;
		this.store = store;
		this.index = index;
	}

    private AnnotationIndexField(String name, Field.Store store, Field.Index index, Analyzer analyzer) {
        this(name, store, index);
        this.analyzer = analyzer;
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


    public Analyzer getAnalyzer()
    {
        return analyzer;
    }

}
