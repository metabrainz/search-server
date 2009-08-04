package org.musicbrainz.search;

import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class ReleaseMangler implements QueryMangler{


    private Pattern matchTypeOrdinals;
    private Pattern matchStatusOrdinals;
    private Pattern matchBarcodeWithLeadingZeroes;

    public ReleaseMangler()
    {
        matchTypeOrdinals=Pattern.compile("(type:)(\\d+)");
        matchStatusOrdinals=Pattern.compile("(status:)([1-4]+)");
        matchBarcodeWithLeadingZeroes=Pattern.compile("barcode:0+");

    }

    /**
     * Handles query parameters containing
     *     barcode:barcode:075596085625
     *
     * Lucene expects
     *     barcode:75596085625
     *
     * @param query
     * @return
     */
    private String convertBarcodesWithLeadingZeroes(String query)
    {
        Matcher m    = matchBarcodeWithLeadingZeroes.matcher(query);
        return m.replaceAll("barcode:");

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
             //Matches behaviour on mb server, if user enters type:0 gets mapped to other                        
             if(index==-1)
             {
                 m.appendReplacement(sb, m.group(1) + ReleaseType.OTHER.getName());
             }
             else if(index<ReleaseType.values().length)
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

    /**
     * Handles query parameters containing
     *     status:2
     *
     * Lucene expects
     *     status:promotion
     *
     * @param query
     * @return query amended as necessary
     */
    private String convertStatusByOrdinal(String query)
    {
        //Release type
        Matcher m    = matchStatusOrdinals.matcher(query);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
             int index = Integer.parseInt(m.group(2)) - 1;
             if(index<ReleaseStatus.values().length)
             {
                m.appendReplacement(sb, m.group(1) + ReleaseStatus.values()[(index)].getName());
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
        return convertBarcodesWithLeadingZeroes(convertStatusByOrdinal(convertTypeByOrdinal(originalQuery)));

    }
}