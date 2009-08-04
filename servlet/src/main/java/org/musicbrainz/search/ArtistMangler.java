package org.musicbrainz.search;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ArtistMangler implements QueryMangler {

    private Pattern matchTypeOrdinals;

    public ArtistMangler() {
        matchTypeOrdinals = Pattern.compile("(type:)([0-2])");
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
        return query.replace("artype:",  ArtistIndexField.TYPE.getName()+":");
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
            m.appendReplacement(sb, m.group(1) + ArtistType.values()[Integer.parseInt(m.group(2))].getName());
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public String mangleQuery(String query) {
        return convertTypeByOrdinal(convertArtype(query));
    }
}