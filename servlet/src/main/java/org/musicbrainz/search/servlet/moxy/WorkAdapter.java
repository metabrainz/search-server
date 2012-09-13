package org.musicbrainz.search.servlet.moxy;

import org.musicbrainz.mmd2.Relation;
import org.musicbrainz.mmd2.RelationList;
import org.musicbrainz.mmd2.Work;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.List;

public class WorkAdapter extends XmlAdapter<WorkAdapter.AdaptedWork, Work> {

    public static class AdaptedWork {
        public List<Relation> relations = new ArrayList<Relation>();
    }

    @Override
    public AdaptedWork marshal(Work work) throws Exception {
        AdaptedWork adaptedWork = new AdaptedWork();
        for(RelationList relationList : work.getRelationList()) {
            for(Relation relation : relationList.getRelation()) {
                adaptedWork.relations.add(relation);
            }
        }
        return adaptedWork;
    }

    @Override
    public Work unmarshal(AdaptedWork adaptedWork) throws Exception {
        Work work = new Work();
        RelationList relationList = new RelationList();
        for(Relation relation : adaptedWork.relations) {
            relationList.getRelation().add(relation);
        }
        work.getRelationList().add(relationList);
        return work;
    }

}

