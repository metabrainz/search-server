package org.musicbrainz.search.servlet.moxy;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class BooleanStringAdapter extends XmlAdapter<Boolean, String> {

    @Override
    public String unmarshal(Boolean v) throws Exception {
        return v.toString();
    }

    @Override
    public Boolean marshal(String v) throws Exception {
        boolean result = Boolean.valueOf(v);
        if(result)
        {
            return result;
        }
        return null;
    }

}
