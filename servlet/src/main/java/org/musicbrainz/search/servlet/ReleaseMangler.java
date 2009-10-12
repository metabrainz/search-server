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

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.musicbrainz.search.index.ReleaseIndexField;
import org.musicbrainz.search.index.ReleaseStatus;
import org.musicbrainz.search.index.ReleaseType;


public class ReleaseMangler implements QueryMangler {


    private Pattern matchTypeOrdinals;
    private Pattern matchStatusOrdinals;

    public ReleaseMangler() {
        matchTypeOrdinals = Pattern.compile("(" + ReleaseIndexField.TYPE.getName() + ":)(\\d+)");
        matchStatusOrdinals = Pattern.compile("(" + ReleaseIndexField.STATUS.getName() + ":)(\\d)");
    }




    /**
     * Handles query parameters containing
     * type:1
     * <p/>
     * Lucene expects
     * type:album
     *
     * @param query
     * @return query amended as necessary
     */
    private String convertTypeByOrdinal(String query) {
        //Release type
        Matcher m = matchTypeOrdinals.matcher(query);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            int index = Integer.parseInt(m.group(2));
            //Matches behaviour on mb server, if user enters type:0 gets mapped to other
            if (index == 0) {
                m.appendReplacement(sb, m.group(1) + ReleaseType.OTHER.getName());
            } else if (index >= ReleaseType.getMinDbId() && index <= ReleaseType.getMaxDbId()) {
                m.appendReplacement(sb, m.group(1) + ReleaseType.getByDbId(index).getName());
            } else {
                //Can't map, so leave as is.
                m.appendReplacement(sb, m.group(1) + m.group(2));
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * Handles query parameters containing
     * status:2
     * <p/>
     * Lucene expects
     * status:promotion
     *
     * @param query
     * @return query amended as necessary
     */
    private String convertStatusByOrdinal(String query) {
        //Release type
        Matcher m = matchStatusOrdinals.matcher(query);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            int index = Integer.parseInt(m.group(2));
            if (index >= ReleaseStatus.getMinSearchId() && index <= ReleaseStatus.getMaxSearchId()) {
                m.appendReplacement(sb, m.group(1) + ReleaseStatus.getBySearchId(index).getName());
            } else {
                //Can't map, so leave as is.
                m.appendReplacement(sb, m.group(1) + m.group(2));
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public String mangleQuery(String originalQuery) {
        return convertStatusByOrdinal(convertTypeByOrdinal(originalQuery));

    }
}