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

public class ArtistCreditName {

    protected int artistId;
    protected String name;
    protected String joinPhrase;
    protected String artistName;
    
    public int getArtistId() {
        return artistId;
    }

    public String getName() {
        return name;
    }

    public String getJoinPhrase() {
        return joinPhrase;
    }

    public String getArtistName() {
        return artistName;
    }

    public ArtistCreditName(String name, String joinPhrase, int artistId, String artistName) {
        this.name = name;
        this.joinPhrase = joinPhrase;
        this.artistId = artistId;
        this.artistName = artistName;
    }    
    
}