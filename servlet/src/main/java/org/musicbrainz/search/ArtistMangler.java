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

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ArtistMangler implements QueryMangler {

    private Pattern matchTypeOrdinals;

    public ArtistMangler() {
        matchTypeOrdinals = Pattern.compile("(" + ArtistIndexField.TYPE.getName() + ":)(\\d)");
    }


    /**
     * Handles query parameters containing
     * artype:
     * <p/>
     * Lucene expects
     * type:
     *
     * @param query
     * @return
     */
    private String convertArtype(String query) {
        return query.replace("artype:", ArtistIndexField.TYPE.getName() + ":");
    }

    /**
     * Handles query parameters containing
     * type:1
     * <p/>
     * Lucene expects
     * type:person
     *
     * @param query
     * @return query amended as necessary
     */
    private String convertTypeByOrdinal(String query) {

        Matcher m = matchTypeOrdinals.matcher(query);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            int index = Integer.parseInt(m.group(2));
            if (index >= ArtistType.getMinDbId() && index <= ArtistType.getMaxDbId()) {
                m.appendReplacement(sb, m.group(1) + ArtistType.values()[index].getName());
            } else {
                //Can't map, so leave as is.
                m.appendReplacement(sb, m.group(1) + m.group(2));
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public String mangleQuery(String query) {
        return convertTypeByOrdinal(convertArtype(query));
    }
}