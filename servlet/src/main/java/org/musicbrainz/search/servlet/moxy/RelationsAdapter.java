package org.musicbrainz.search.servlet.moxy;

import org.musicbrainz.mmd2.Relation;
import org.musicbrainz.mmd2.RelationList;

import java.util.*;

import javax.xml.bind.*;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.namespace.QName;

public class RelationsAdapter extends XmlAdapter<RelationsAdapter.AdaptedRelations, RelationList> {

    @Override
    public RelationList unmarshal(AdaptedRelations v) throws Exception {
        return null;
    }

    @Override
    public AdaptedRelations marshal(RelationList relations) throws Exception {
        AdaptedRelations adaptedRelations = new AdaptedRelations();
        for(Relation relation : relations.getRelation()) {
            adaptedRelations.relations.add(new JAXBElement<Relation>(new QName(relations.getTargetType()), Relation.class, relation));
        }
        return adaptedRelations;
    }

    @XmlSeeAlso({Registry.class})
    public static class AdaptedRelations {

        @XmlElementRef(type=JAXBElement.class, name="artist")
        public List<JAXBElement<Relation>> relations = new ArrayList<JAXBElement<Relation>>();

    }

}