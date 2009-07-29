package org.musicbrainz.search;

import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class ReleaseGroupMangler implements QueryMangler{


    private Pattern matchTypeOrdinals;

    public ReleaseGroupMangler()
    {
        matchTypeOrdinals=Pattern.compile("(type:)(\\d+)");
    }

    /**
     * Handles query parameters containing
     *     type:1
     *
     * Lucene expects
     *     type:album
     *
     * @param query
     * @return query amended as necessary
     */
    private String convertTypeByOrdinal(String query)
    {
        //Release type
        Matcher m    = matchTypeOrdinals.matcher(query);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
             int index = Integer.parseInt(m.group(2)) - 1;
             if(index<ReleaseType.values().length)
             {
                m.appendReplacement(sb, m.group(1) + ReleaseType.values()[(index)].getName());
             }
            else
             {
                 //Can't map, so leave as is.
                m.appendReplacement(sb, m.group(1) + m.group(2));
             }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public String mangleQuery(String originalQuery)
    {
        return convertTypeByOrdinal(originalQuery);

    }
}