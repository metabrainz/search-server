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

public enum ReleaseStatus {

    OFFICIAL(1,100, "official"),
    PROMOTION(2,101, "promotion"),
    BOOTLEG(3,102, "bootleg"),
    PSEUDO_RELEASE(4,103, "pseudo-release"),;

    private Integer dbId;
    private Integer searchId;

    private String name;

    ReleaseStatus(Integer searchId,Integer dbId, String name) {
        this.dbId = dbId;
        this.searchId = searchId;
        this.name = name;
    }

    public Integer getDbId() {
        return dbId;
    }

    public Integer getSearchId() {
        return searchId;
    }

    public String getName() {
        return name;
    }

    public static ReleaseStatus getByDbId(int id) {
        for (ReleaseStatus status : ReleaseStatus.values()) {
            if (status.getDbId() == id) {
                return status;
            }
        }
        return null;
    }

    public static ReleaseStatus getBySearchId(int id) {
        for (ReleaseStatus status : ReleaseStatus.values()) {
            if (status.getSearchId() == id) {
                return status;
            }
        }
        return null;
    }

    public static int getMinDbId() {
        return OFFICIAL.getDbId();
    }

    public static int getMaxDbId() {
        return PSEUDO_RELEASE.getDbId();
    }

    public static int getMinSearchId() {
        return OFFICIAL.getSearchId();
    }

    public static int getMaxSearchId() {
        return PSEUDO_RELEASE.getSearchId();
    }
}