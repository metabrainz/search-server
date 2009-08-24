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

public enum AnnotationType {

	ARTIST	(1, "artist"),
	RELEASE	(2, "release"),
	LABEL	(3, "label"),
	TRACK	(4, "track"),;

	private Integer id;
	private String name;

	AnnotationType(Integer id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Integer getId() {
		return id;
	}
}
