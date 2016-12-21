package org.musicbrainz.search.servlet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.NumericUtils;
import org.musicbrainz.search.index.LabelIndexField;
import org.musicbrainz.search.index.ReleaseIndexField;
import org.musicbrainz.search.servlet.mmd1.ReleaseGroupType;
import org.musicbrainz.search.servlet.mmd1.ReleaseStatus;


/**
 * Subclasses QueryParser to handle numeric fields that we might want wish to do range queries for and handle type
 * searches specified using integers.
 */
public class ReleaseQueryParser extends MultiFieldQueryParser {

    public ReleaseQueryParser(java.lang.String[] strings, Analyzer a) {
        super(strings, a);
    }

    protected Query newTermQuery(Term term) {
        if (term.field().equals(ReleaseIndexField.TYPE.getName())) {
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
        } else if (term.field().equals(ReleaseIndexField.STATUS.getName())) {
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
                (term.field().equals(ReleaseIndexField.NUM_TRACKS.getName()))||
                (term.field().equals(ReleaseIndexField.NUM_TRACKS_MEDIUM.getName()))||
                (term.field().equals( ReleaseIndexField.NUM_MEDIUMS.getName()))||
                (term.field().equals(ReleaseIndexField.NUM_DISCIDS.getName())) ||
                (term.field().equals(ReleaseIndexField.NUM_DISCIDS_MEDIUM.getName()))
                ){
            try {

                int number = Integer.parseInt(term.text());
                BytesRefBuilder bytes = new BytesRefBuilder();
                NumericUtils.intToPrefixCoded(number, 0, bytes);
                TermQuery tq = new TermQuery(new Term(term.field(), bytes.toBytesRef().utf8ToString()));
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

    @Override
    public Query newRangeQuery(String field,
                               String part1,
                               String part2,
                               boolean startInclusive,
                               boolean endInclusive)
    {
        if (
                (field.equals(ReleaseIndexField.NUM_TRACKS.getName())) ||
                (field.equals(ReleaseIndexField.NUM_TRACKS_MEDIUM.getName())) ||
                (field.equals(ReleaseIndexField.NUM_MEDIUMS.getName())) ||
                (field.equals(ReleaseIndexField.NUM_DISCIDS.getName())) ||
                (field.equals(ReleaseIndexField.NUM_DISCIDS_MEDIUM.getName()))
            )
        {
            BytesRefBuilder bytes1 = new BytesRefBuilder();
            BytesRefBuilder bytes2 = new BytesRefBuilder();
            NumericUtils.intToPrefixCoded(Integer.parseInt(part1), 0, bytes1);
            NumericUtils.intToPrefixCoded(Integer.parseInt(part2), 0, bytes2);
            part1 = bytes1.toBytesRef().utf8ToString();
            part2 = bytes2.toBytesRef().utf8ToString();
        }
        TermRangeQuery query = (TermRangeQuery)
                super.newRangeQuery(field, part1, part2, startInclusive, endInclusive);
        return query;

    }
}
