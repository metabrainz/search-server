package org.musicbrainz.search.helper;

import org.musicbrainz.search.type.ReleaseGroupPrimaryType;
import org.musicbrainz.search.type.ReleaseGroupSecondaryType;

import java.util.List;

public class ReleaseGroupHelper {

    /**
     * Map the ReleaseType/SecondaryTypes introduced in Schema Change 15/05/2012 to the type field as it was before
     * this change.
     *
     * @param primaryType
     * @param secondaryTypes
     * @return
     */
    public static String calculateOldTypeFromPrimaryType(String primaryType, List<String> secondaryTypes)
    {
        if(primaryType==null||!primaryType.equals(ReleaseGroupPrimaryType.ALBUM.getName()) || secondaryTypes==null)
        {
            return primaryType;
        }
        if(secondaryTypes.contains(ReleaseGroupSecondaryType.COMPILATION.getName()))
        {
            return ReleaseGroupSecondaryType.COMPILATION.getName();
        }
        else if(secondaryTypes.contains(ReleaseGroupSecondaryType.INTERVIEW.getName()))
        {
            return ReleaseGroupSecondaryType.INTERVIEW.getName();
        }
        else if(secondaryTypes.contains(ReleaseGroupSecondaryType.LIVE.getName()))
        {
            return ReleaseGroupSecondaryType.LIVE.getName();
        }
        else if(secondaryTypes.contains(ReleaseGroupSecondaryType.REMIX.getName()))
        {
            return ReleaseGroupSecondaryType.REMIX.getName();
        }
        else if(secondaryTypes.contains(ReleaseGroupSecondaryType.SOUNDTRACK.getName()))
        {
            return ReleaseGroupSecondaryType.SOUNDTRACK.getName();
        }
        else if(secondaryTypes.contains(ReleaseGroupSecondaryType.SPOKENWORD.getName()))
        {
            return ReleaseGroupSecondaryType.SPOKENWORD.getName();
        }
        else
        {
            return primaryType;
        }
    }

}
