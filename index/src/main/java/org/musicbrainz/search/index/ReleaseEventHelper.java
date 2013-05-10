/*
 Copyright (c) 2010 Paul Taylor
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.
  2. Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
     documentation and/or other materials provided with the distribution.
  3. Neither the name of the MusicBrainz project nor the names of the
     contributors may be used to endorse or promote products derived from
     this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.musicbrainz.search.index;

import com.google.common.base.Strings;
import org.musicbrainz.mmd2.*;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReleaseEventHelper {

    public static Map<Integer,List<ReleaseEvent>> completeReleaseEventsFromDbResults(ResultSet rs,
                                                                  String entityKey) throws SQLException {
        Map<Integer, List<ReleaseEvent>> releaseEvents = new HashMap<Integer, List<ReleaseEvent>>();
        ObjectFactory of = new ObjectFactory();
        List<ReleaseEvent> releaseEventList;
        while (rs.next()) {
            int entityId = rs.getInt(entityKey);
            if (!releaseEvents.containsKey(entityId)) {
                releaseEventList = new ArrayList<ReleaseEvent>();
                releaseEvents.put(entityId, releaseEventList);
            } else {
                releaseEventList = releaseEvents.get(entityId);
            }

            String iso_code = rs.getString("country");
            ReleaseEvent releaseEvent = of.createReleaseEvent();
            if(iso_code!=null) {
                Iso31661CodeList isoList = of.createIso31661CodeList();
                isoList.getIso31661Code().add(iso_code);
                DefAreaElementInner area = of.createDefAreaElementInner();
                area.setIso31661CodeList(isoList);
                releaseEvent.setArea(area);
            }
            releaseEvent.setDate(Strings.emptyToNull(Utils.formatDate(rs.getInt("date_year"), rs.getInt("date_month"), rs.getInt("date_day"))));
            releaseEventList.add(releaseEvent);
        }
        return releaseEvents;
    }



}
