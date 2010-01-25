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

public enum ArtistType {
    UNKNOWN(0, "unknown"),
    PERSON(1, "person"),
    GROUP(2, "group"),;

    private Integer searchId;

    private String name;

    ArtistType(Integer dbId, String name) {
        this.searchId = dbId;
        this.name = name;
    }

    public Integer getSearchId() {
        return searchId;
    }


    public String getName() {
        return name;
    }

    public static ArtistType getBySearchId(int id) {
        return ArtistType.values()[id];
    }


    public static int getMinSearchId() {
        return UNKNOWN.getSearchId();
    }

    public static int getMaxSearchId() {
        return GROUP.getSearchId();
    }

}
