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
            if (index >= 0 && index < ArtistType.values().length) {
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