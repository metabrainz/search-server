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

public enum ReleaseType {

    ALBUM(1, "album"),
    SINGLE(2, "single"),
    EP(3, "ep"),
    COMPILATION(4, "compilation"),
    SOUNDTRACK(5, "soundtrack"),
    SPOKENWORD(6, "spokenword"),
    INTERVIEW(7, "interview"),
    AUDIOBOOK(8, "audiobook"),
    LIVE(9, "live"),
    REMIX(10, "remix"),
    OTHER(11, "other"),;

    private Integer dbId;
    private String name;

    ReleaseType(Integer dbId, String name) {
        this.dbId = dbId;
        this.name = name;
    }

    public Integer getDbId() {
        return dbId;
    }

    public String getName() {
        return name;
    }


    public static ReleaseType getByDbId(int id) {
        for (ReleaseType type : ReleaseType.values()) {
            if (type.getDbId() == id) {
                return type;
            }
        }
        return null;
    }

    public static int getMinDbId() {
        return ALBUM.getDbId();
    }

    public static int getMaxDbId() {
        return OTHER.getDbId();
    }

}

