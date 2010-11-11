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

package org.musicbrainz.search.servlet.mmd1;

/**
 * Required when searching releases or release groups using the old release(group)id
 */
public enum ReleaseGroupType {

    NONALBUMTRACKS(0, "nat"),
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

    private String name;
    private Integer searchId;

    ReleaseGroupType(Integer searchId,String name) {
        this.searchId=searchId;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ReleaseGroupType getBySearchId(int id) {
        for (ReleaseGroupType type : ReleaseGroupType.values()) {
            if (type.getSearchId() == id) {
                return type;
            }
        }
        return null;
    }

    public Integer getSearchId() {
        return searchId;
    }



    public static int getMinSearchId() {
        return NONALBUMTRACKS.getSearchId();
    }

    public static int getMaxSearchId() {
        return OTHER.getSearchId();
    }

}
