package org.musicbrainz.search.servlet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.util.Version;
import org.musicbrainz.search.index.*;
import org.musicbrainz.search.LuceneVersion;


/**
 * Subclasses QueryParser to handle numeric fields that we might want wish to do range queries for and handle type
 * searches specified using integers.
 */
public class ReleaseQueryParser extends QueryParser {

    public ReleaseQueryParser(String field, Analyzer a) {
        super(LuceneVersion.LUCENE_VERSION, field, a);
    }

    protected Query newTermQuery(Term term) {
        if (term.field() == ReleaseIndexField.TYPE.getName()) {
            try {
                int typeId = Integer.parseInt(term.text());
                if (typeId >= ReleaseGroupType.getMinSearchId() && typeId <= ReleaseGroupType.getMaxSearchId()) {
                    TermQuery tq = new TermQuery(new Term(term.field(), ReleaseGroupType.getBySearchId(typeId).getName()));
                    return tq;
                } else {
                    return super.newTermQuery(term);
                }
            }
            catch (NumberFormatException nfe) {
                return super.newTermQuery(term);

            }
        } else if (term.field() == ReleaseIndexField.STATUS.getName()) {
            try {
                int statusId = Integer.parseInt(term.text());
                if (statusId >= ReleaseStatus.getMinSearchId() && statusId <= ReleaseStatus.getMaxSearchId()) {
                    TermQuery tq = new TermQuery(new Term(term.field(), ReleaseStatus.getBySearchId(statusId).getName()));
                    return tq;
                } else {
                    return super.newTermQuery(term);
                }
            }
            catch (NumberFormatException nfe) {
                return super.newTermQuery(term);

            }
        } else if(
                (term.field() == ReleaseIndexField.NUM_TRACKS.getName())||
                (term.field() == ReleaseIndexField.NUM_TRACKS_MEDIUM.getName())||
                (term.field() == ReleaseIndexField.NUM_MEDIUMS.getName())||
                (term.field() == ReleaseIndexField.NUM_DISCIDS.getName()) ||
                (term.field() == ReleaseIndexField.NUM_DISCIDS_MEDIUM.getName())
                ){
            try {
                int number = Integer.parseInt(term.text());
                TermQuery tq = new TermQuery(new Term(term.field(), NumericUtils.intToPrefixCoded(number)));
                return tq;
            }
            catch (NumberFormatException nfe) {
                //If not provided numeric argument just leave as is, won't give matches
                return super.newTermQuery(term);
            }
        } else {
            return super.newTermQuery(term);

        }
    }

     /**
     *
     * Convert Numeric Fields
     *
     * @param field
     * @param part1
     * @param part2
     * @param inclusive
     * @return
     */
    @Override
    public Query newRangeQuery(String field,
                               String part1,
                               String part2,
                               boolean inclusive) {

        if (
                (field.equals(ReleaseIndexField.NUM_TRACKS.getName())) ||
                (field.equals(ReleaseIndexField.NUM_TRACKS_MEDIUM.getName())) ||
                (field.equals(ReleaseIndexField.NUM_MEDIUMS.getName())) ||
                (field.equals(ReleaseIndexField.NUM_DISCIDS.getName())) ||
                (field.equals(ReleaseIndexField.NUM_DISCIDS_MEDIUM.getName()))
            )
        {
            part1 = NumericUtils.intToPrefixCoded(Integer.parseInt(part1));
            part2 = NumericUtils.intToPrefixCoded(Integer.parseInt(part2));
        }
        TermRangeQuery query = (TermRangeQuery)
                super.newRangeQuery(field, part1, part2,inclusive);
        return query;
    }
}