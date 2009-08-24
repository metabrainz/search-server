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

import org.apache.lucene.document.Field;

/**
 * Fields created in Lucene Search Index
 */
public enum ReleaseGroupIndexField implements IndexField {

	ARTIST_ID		("arid",			Field.Store.YES,	Field.Index.NOT_ANALYZED),
	ARTIST			("artist",			Field.Store.YES,	Field.Index.ANALYZED),
	RELEASEGROUP_ID	("rgid",			Field.Store.YES,	Field.Index.NOT_ANALYZED),
	RELEASEGROUP	("releasegroup",	Field.Store.YES,	Field.Index.ANALYZED),
	TYPE			("type",			Field.Store.YES,	Field.Index.ANALYZED),
	RELEASES		("releases",		Field.Store.YES,	Field.Index.ANALYZED),;

	private String name;
	private Field.Store store;
	private Field.Index index;

	private ReleaseGroupIndexField(String name, Field.Store store, Field.Index index) {
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

}