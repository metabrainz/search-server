package org.musicbrainz.search;

import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class LabelMangler implements QueryMangler{


    private Pattern matchTypeOrdinals;
    private Pattern matchLabelCodeWithLeadingZeroes;

    public LabelMangler()
    {
        matchTypeOrdinals=Pattern.compile("(type:)([0-7])");
        matchLabelCodeWithLeadingZeroes =Pattern.compile("code:0+");
    }

     /**
     * Handles query parameters containing
     *     code:05807
     *
     * Lucene expects
     *     code:5807
     *
     * @param query
     * @return
     */
    private String convertLabelCodesWithLeadingZeroes(String query)
    {
        Matcher m    = matchLabelCodeWithLeadingZeroes.matcher(query);
        return m.replaceAll("code:");      

    }

    /**
     * Handles query parameters containing
     *     type:1
     *
     * Lucene expects
     *     type:distributor
     *
     * @param query
     * @return query amended as necessary
     */
    private String convertTypeByOrdinal(String query)
    {
        Matcher m    = matchTypeOrdinals.matcher(query);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
             m.appendReplacement(sb, m.group(1) + LabelType.values()[Integer.parseInt(m.group(2))].getName());
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public String mangleQuery(String originalQuery)
    {
        return convertLabelCodesWithLeadingZeroes(convertTypeByOrdinal(originalQuery));
    }
}