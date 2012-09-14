package org.musicbrainz.search.servlet.moxy;

import org.musicbrainz.mmd2.Relation;
import org.musicbrainz.mmd2.RelationList;
import org.musicbrainz.mmd2.Work;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.List;

public class WorkAdapter extends XmlAdapter<WorkAdapter.AdaptedWork, Work> {

    public static class AdaptedWork extends Work {
        public List<Relation> relations = new ArrayList<Relation>();
    }

    /**
     * Call when convert model to json, replaces work in model with adaptedWork
     * which does not contain a list of RelationList, instead all relations in each existing
     * RelationList are merged into a list of relations. We do this because it is not possible to merge
     * a List of RelationLists into the work using oxml.xml mapping
     */
    @Override
    public AdaptedWork marshal(Work work) throws Exception {
        
        AdaptedWork adaptedWork = new AdaptedWork();
        for(RelationList relationList : work.getRelationList()) {
            for(Relation relation : relationList.getRelation()) {
                adaptedWork.relations.add(relation);
            }
        }

        //Also need to copy any other elements/attributes we may want to output
        adaptedWork.setAliasList(work.getAliasList());
        adaptedWork.setArtistCredit(work.getArtistCredit());
        adaptedWork.setDisambiguation(work.getDisambiguation());
        adaptedWork.setId(work.getId());
        adaptedWork.setIswcList(work.getIswcList());
        adaptedWork.setLanguage(work.getLanguage());
        adaptedWork.setRating(work.getRating());
        adaptedWork.setScore(work.getScore());
        adaptedWork.setTitle(work.getTitle());
        adaptedWork.setTagList(work.getTagList());
        adaptedWork.setType(work.getType());
        adaptedWork.setUserRating(work.getUserRating());
        adaptedWork.setUserTagList(work.getUserTagList());
        return adaptedWork;
    }

    /*
    Not used in Search Server
     */
    @Override
    public Work unmarshal(AdaptedWork adaptedWork) throws Exception {
        throw new UnsupportedOperationException("Umarshalling json back to model not supported");
    }

}

