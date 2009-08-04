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

            if (index >= 0 && index < LabelType.values().length) {
                m.appendReplacement(sb, m.group(1) + LabelType.values()[index].getName());
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