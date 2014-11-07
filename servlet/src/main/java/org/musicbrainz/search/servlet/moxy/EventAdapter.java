/* Copyright (c) 2012 Paul Taylor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the MusicBrainz project nor the names of the
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.musicbrainz.search.servlet.moxy;

import org.musicbrainz.mmd2.Event;
import org.musicbrainz.mmd2.Relation;
import org.musicbrainz.mmd2.RelationList;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends XmlAdapter<EventAdapter.AdaptedEvent, Event> {

    public static class AdaptedEvent extends Event {
        public List<Relation> relations = new ArrayList<Relation>();
    }

    /**
     * Call when convert model to json, replaces work in model with adaptedWork
     * which does not contain a list of RelationList, instead all relations in each existing
     * RelationList are merged into a list of relations. We do this because it is not possible to merge
     * a List of RelationLists into the work using oxml.xml mapping
     */
    @Override
    public AdaptedEvent marshal(Event event) throws Exception {
        
        AdaptedEvent adaptedEvent = new AdaptedEvent();
        for(RelationList relationList : event.getRelationList()) {
            for(Relation relation : relationList.getRelation()) {
                adaptedEvent.relations.add(relation);
            }
        }

        //Also need to copy any other elements/attributes we may want to output
        adaptedEvent.setAliasList(event.getAliasList());
        adaptedEvent.setDisambiguation(event.getDisambiguation());
        adaptedEvent.setId(event.getId());
        adaptedEvent.setName(event.getName());
        adaptedEvent.setTime(event.getTime());
        adaptedEvent.setLifeSpan(event.getLifeSpan());
        adaptedEvent.setRating(event.getRating());
        adaptedEvent.setScore(event.getScore());
        adaptedEvent.setTagList(event.getTagList());
        adaptedEvent.setType(event.getType());
        adaptedEvent.setUserRating(event.getUserRating());
        adaptedEvent.setUserTagList(event.getUserTagList());
        return adaptedEvent;
    }

    /*
    Not used in Search Server
     */
    @Override
    public Event unmarshal(AdaptedEvent adaptedEvent) throws Exception {
        throw new UnsupportedOperationException("Umarshalling json back to model not supported");
    }

}

