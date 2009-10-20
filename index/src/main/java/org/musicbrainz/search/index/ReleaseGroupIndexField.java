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

import org.apache.lucene.document.Field;

/**
 * Fields created in Lucene Search Index
 */
//TODO users probably just want to search for artist , and have it sorted all combinations
public enum ReleaseGroupIndexField implements IndexField {

	ARTIST_ID		    ("arid",			Field.Store.YES,	Field.Index.NOT_ANALYZED),
    ARTIST              ("artist",          Field.Store.YES,	Field.Index.ANALYZED),  //FullArtist(s) for releasegroup
	ARTIST_NAME         ("artistname",		Field.Store.YES,	Field.Index.ANALYZED),
    ARTIST_SORTNAME	    ("sortname",	    Field.Store.YES,	Field.Index.ANALYZED),
    ARTIST_NAMECREDIT   ("creditname",	    Field.Store.YES,	Field.Index.ANALYZED),
    ARTIST_JOINPHRASE	("joinphrase",	    Field.Store.YES,	Field.Index.NO),       //Never Searched
    ARTIST_COMMENT	    ("artistcomment",	Field.Store.YES,	Field.Index.NO),       //ONLY used by Html, maybe can be dropped
    RELEASEGROUP_ID	    ("rgid",			Field.Store.YES,	Field.Index.NOT_ANALYZED),
	RELEASEGROUP	    ("releasegroup",	Field.Store.YES,	Field.Index.ANALYZED),
	TYPE			    ("type",			Field.Store.YES,	Field.Index.NOT_ANALYZED),
	RELEASE             ("release", 		Field.Store.YES,	Field.Index.ANALYZED);

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