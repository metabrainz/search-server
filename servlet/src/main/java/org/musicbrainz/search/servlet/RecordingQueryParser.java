package org.musicbrainz.search.servlet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.NumericUtils;
import org.musicbrainz.search.index.RecordingIndexField;
import org.musicbrainz.search.index.ReleaseGroupType;


/**
 * Subclasses QueryParser to handle numeric fields that we might want wish to do range queries for and handle type
 * searches specified using integers.
 */
public class RecordingQueryParser extends QueryParser {

    public RecordingQueryParser(String field, Analyzer a) {
        super(field, a);
    }

    protected Query newTermQuery(Term term) {
        if (
                (term.field() == RecordingIndexField.DURATION.getName()) ||
                        (term.field() == RecordingIndexField.QUANTIZED_DURATION.getName()) ||
                        (term.field() == RecordingIndexField.TRACKNUM.getName()) ||
                        (term.field() == RecordingIndexField.NUM_TRACKS.getName())
                ) {
            try
            {
                int number = Integer.parseInt(term.text());
                TermQuery tq = new TermQuery(new Term(term.field(), NumericUtils.intToPrefixCoded(number)));
                return tq;
            }
            catch (NumberFormatException nfe) {
                //If not provided numeric argument just leave as is, won't give matches 
                return super.newTermQuery(term);
            }

        } else if( term.field() == RecordingIndexField.RELEASE_TYPE.getName()) {
            try {
                int typeId = Integer.parseInt(term.text());
                if (typeId >= ReleaseGroupType.getMinSearchId() && typeId <= ReleaseGroupType.getMaxSearchId()) {
                    TermQuery tq = new TermQuery(new Term(term.field(),ReleaseGroupType.getBySearchId(typeId).getName()));
                    return tq;
                }
                else {
                    return super.newTermQuery(term);    
                }
            }
            catch(NumberFormatException nfe) {
                return super.newTermQuery(term);

            }
        } else {
            return super.newTermQuery(term);
        }
    }

    public Query newRangeQuery(String field,
                               String part1,
                               String part2,
                               boolean inclusive) {

        if (
                (field.equals(RecordingIndexField.DURATION.getName())) ||
                (field.equals(RecordingIndexField.QUANTIZED_DURATION.getName())) ||
                (field.equals(RecordingIndexField.TRACKNUM.getName())) ||
                (field.equals(RecordingIndexField.NUM_TRACKS.getName()))
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
