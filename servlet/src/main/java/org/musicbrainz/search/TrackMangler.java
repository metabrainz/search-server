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

import org.apache.lucene.document.NumberTools;

import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class TrackMangler implements QueryMangler {

    private Pattern matchTypeOrdinals;
    private Pattern matchDurAndQdur;
    private Pattern matchDurAndQdurRange;
    private Pattern matchTnum;
    private Pattern matchTnumRange;

    public TrackMangler() {
        matchTypeOrdinals = Pattern.compile("(" + TrackIndexField.RELEASE_TYPE.getName() + ":)(\\d+)");
        matchDurAndQdur = Pattern.compile("(" + TrackIndexField.DURATION.getName() + ":)([0-9]+)");
        matchDurAndQdurRange = Pattern.compile("(" + TrackIndexField.DURATION.getName() + ":\\[)([0-9]+)( TO )([0-9]+)(\\])");
        matchTnum = Pattern.compile("(" + TrackIndexField.TRACKNUM.getName() + ":)([0-9]+)");
        matchTnumRange = Pattern.compile("(" + TrackIndexField.TRACKNUM.getName() + ":\\[)([0-9]+)( TO )([0-9]+)(\\])");

    }

    /**
     * These values have to be converted to a String representation this allows the values to be ordered
     * i.e 91 comes before 210, allows clauses such as qdur:[99 TO 110] to work
     *
     * @param query
     * @return
     */
    private String convertDurAndQdurToOrderable(String query) {
        Matcher m = matchDurAndQdur.matcher(query);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            int duration = Integer.parseInt(m.group(2));
            m.appendReplacement(sb, m.group(1) + NumberTools.longToString(duration));
        }
        m.appendTail(sb);
        query = sb.toString();

        m = matchDurAndQdurRange.matcher(query);
        sb = new StringBuffer();
        while (m.find()) {
            int firstTrackNo = Integer.parseInt(m.group(2));
            int secondTrackNo = Integer.parseInt(m.group(4));
            m.appendReplacement(sb, m.group(1)
                    + NumberTools.longToString(firstTrackNo)
                    + m.group(3)
                    + NumberTools.longToString(secondTrackNo)
                    + m.group(5));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * These values have to be converted to a String representation this allows the values to be ordered
     * i.e 1 comes before 11, allows clauses such as tnum:[1 TO 11] to work
     *
     * @param query
     * @return
     */
    private String convertTnumToOrderable(String query) {
        Matcher m = matchTnum.matcher(query);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            int trackNo = Integer.parseInt(m.group(2));
            m.appendReplacement(sb, m.group(1) + NumberTools.longToString(trackNo));
        }
        m.appendTail(sb);
        query = sb.toString();

        m = matchTnumRange.matcher(query);
        sb = new StringBuffer();
        while (m.find()) {
            int firstTrackNo = Integer.parseInt(m.group(2));
            int secondTrackNo = Integer.parseInt(m.group(4));
            m.appendReplacement(sb, m.group(1)
                    + NumberTools.longToString(firstTrackNo)
                    + m.group(3)
                    + NumberTools.longToString(secondTrackNo)
                    + m.group(5));
        }
        m.appendTail(sb);
        return sb.toString();
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


    public String mangleQuery(String query) {
        return convertTypeByOrdinal(convertTnumToOrderable(convertDurAndQdurToOrderable(query)));

    }
}