package org.musicbrainz.search;

import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class ReleaseGroupMangler implements QueryMangler{


    private Pattern matchTypeOrdinals;

    public ReleaseGroupMangler()
    {
        matchTypeOrdinals=Pattern.compile("("+ReleaseGroupIndexField.TYPE.getName()+":)(\\d+)");
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
             int index = Integer.parseInt(m.group(2));
             if(index >=0 && index<ReleaseType.values().length)
             {
                m.appendReplacement(sb, m.group(1) + ReleaseGroupType.values()[(index)].getName());
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