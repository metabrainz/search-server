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

package org.musicbrainz.search;

public enum LabelType {

    UNKNOWN(0, "unknown"),
    DISTRIBUTOR(1, "distributor"),
    HOLDING(2, "holding"),
    PRODUCTION(3, "production"),
    ORIGINAL_PROD(4, "orig. prod."),
    BOOTLEG_PROD(5, "bootleg prod."),
    REISSUE_PROD(6, "reissue prod."),
    PUBLISHER(7, "publisher"),;

    private Integer dbId;
    private String name;

    LabelType(Integer dbId, String name) {
        this.dbId = dbId;
        this.name = name;
    }


    public Integer getDbId() {
        return dbId;
    }

    public String getName() {
        return name;
    }

    public static LabelType getByDbId(int id) {
        return LabelType.values()[id];
    }


    public static int getMinDbId() {
        return UNKNOWN.getDbId();
    }

    public static int getMaxDbId() {
        return PUBLISHER.getDbId();
    }
}

