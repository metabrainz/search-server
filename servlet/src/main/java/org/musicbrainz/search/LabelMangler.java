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

import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class LabelMangler implements QueryMangler {


    private Pattern matchTypeOrdinals;
    private Pattern matchLabelCodeWithLeadingZeroes;

    public LabelMangler() {
        matchTypeOrdinals = Pattern.compile("(" + LabelIndexField.TYPE.getName() + ":)(\\d)");
        matchLabelCodeWithLeadingZeroes = Pattern.compile(LabelIndexField.CODE.getName() + ":0+");
    }

    /**
     * Handles query parameters containing
     * code:05807
     * <p/>
     * Lucene expects
     * code:5807
     *
     * @param query
     * @return
     */
    private String convertLabelCodesWithLeadingZeroes(String query) {
        Matcher m = matchLabelCodeWithLeadingZeroes.matcher(query);
        return m.replaceAll(LabelIndexField.CODE.getName() + ":");

    }

    /**
     * Handles query parameters containing
     * type:1
     * <p/>
     * Lucene expects
     * type:distributor
     *
     * @param query
     * @return query amended as necessary
     */
    private String convertTypeByOrdinal(String query) {
        Matcher m = matchTypeOrdinals.matcher(query);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            int index = Integer.parseInt(m.group(2));

            if (index >= LabelType.getMinDbId() && index <= LabelType.getMaxDbId()) {
                m.appendReplacement(sb, m.group(1) + LabelType.getByDbId(index).getName());
            } else {
                //Can't map, so leave as is.
                m.appendReplacement(sb, m.group(1) + m.group(2));

            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public String mangleQuery(String originalQuery) {
        return convertLabelCodesWithLeadingZeroes(convertTypeByOrdinal(originalQuery));
    }
}