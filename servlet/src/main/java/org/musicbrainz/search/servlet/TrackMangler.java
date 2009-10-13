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

package org.musicbrainz.search.servlet;

import org.musicbrainz.search.index.ReleaseGroupType;
import org.musicbrainz.search.index.TrackIndexField;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TrackMangler implements QueryMangler {

    private Pattern matchTypeOrdinals;

    public TrackMangler() {
        matchTypeOrdinals = Pattern.compile("(" + TrackIndexField.RELEASE_TYPE.getName() + ":)(\\d+)");
    }


    /**
     * Handles query parameters containing
     * type:1
     * <p/>
     * Lucene expects
     * type:album
     *
     * @param originalQuery
     * @return query amended as necessary
     */
    private String convertTypeByOrdinal(String originalQuery) {
        //Release type
        Matcher m = matchTypeOrdinals.matcher(originalQuery);
        StringBuffer sb = new StringBuffer();
         while (m.find()) {
            int index = Integer.parseInt(m.group(2));
            if (index >= ReleaseGroupType.getMinSearchId() && index <= ReleaseGroupType.getMaxSearchId()) {
                m.appendReplacement(sb, m.group(1) + ReleaseGroupType.getBySearchId(index).getName());
            } else {
                //Can't map, so leave as is.
                m.appendReplacement(sb, m.group(1) + m.group(2));
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }


    public String mangleQuery(String query) {
        return convertTypeByOrdinal(query);

    }
}