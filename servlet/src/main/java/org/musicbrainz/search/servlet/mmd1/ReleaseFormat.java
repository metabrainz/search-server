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
package org.musicbrainz.search.servlet.mmd1;


public enum ReleaseFormat {
    CD(1, "CD"),
    DVD(2, "DVD"),
    SACD(3, "SACD"),
    DUALDISC(4, "DualDisc"),
    LASERDISC(5, "LaserDisc"),
    MINIDISC(6, "MiniDisc"),
    VINYL(7, "Vinyl"),
    CASSETTE(8, "Cassette"),
    CARTRIDGE(9, "Cartridge"),
    REEL_TO_REEL(10, "ReelToReel"),
    DAT(11, "DAT"),
    DIGITAL(12, "Digital"),
    OTHER(13, "Other"),
    WAX_CYLINDER(14, "WaxCylinder"),
    PIANO_ROLL(15, "PianoRoll"),
    DCC(16, "DCC"),;


    private Integer dbId;
    private String name;

    ReleaseFormat(Integer dbId, String name) {
        this.dbId = dbId;
        this.name = name;
    }

    public Integer getDbId() {
        return dbId;
    }

    public String getName() {
        return name;
    }


    public static ReleaseFormat getByDbId(int id) {
        for (ReleaseFormat format : ReleaseFormat.values()) {
            if (format.getDbId() == id) {
                return format;
            }
        }
        return null;
    }

    public static int getMinDbId() {
        return CD.getDbId();
    }

    public static int getMaxDbId() {
        return DCC.getDbId();
    }
}
