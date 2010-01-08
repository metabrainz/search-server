/*
 * MusicBrainz Search Server
 * Copyright (C) 2010  Paul Taylor

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

import org.musicbrainz.mmd2.ArtistCredit;
import org.musicbrainz.mmd2.NameCredit;


/**
 * Utility class for manipulating Artist Credits
 */
public class ArtistCreditHelper {

    public static String buildFullArtistCreditName(ArtistCredit artistCredit)
    {
        StringBuffer sb = new StringBuffer();
        for (NameCredit nameCredit : artistCredit.getNameCredit()) {
            if(nameCredit.getName()!=null){
                sb.append(nameCredit.getName());
            }
            else {
                sb.append(nameCredit.getArtist().getName());
            }
            if(nameCredit.getJoinphrase()!=null) {
                sb.append(' ' + nameCredit.getJoinphrase() + ' ');
            }
        }
        return sb.toString();
    }

    public static ArtistCredit unserialize(String artistCreditSerilized) {
        return (ArtistCredit)MMDSerializer.unserialize(artistCreditSerilized,ArtistCredit.class);
    }
}
