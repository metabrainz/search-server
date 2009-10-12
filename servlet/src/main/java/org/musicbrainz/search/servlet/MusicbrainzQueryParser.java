package org.musicbrainz.search.servlet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.NumericUtils;
import org.musicbrainz.search.index.TrackIndexField;


/**
 * Subclasses QueryParser to handle numeric fields that we might want wish to do range queries for.
 */
public class MusicbrainzQueryParser extends QueryParser {

    public MusicbrainzQueryParser(String field, Analyzer a) {
        super(field, a);
    }

    protected Query newTermQuery(Term term) {
        if (
                (term.field() == TrackIndexField.DURATION.getName()) ||
                        (term.field() == TrackIndexField.QUANTIZED_DURATION.getName()) ||
                        (term.field() == TrackIndexField.TRACKNUM.getName()) ||
                        (term.field() == TrackIndexField.NUM_TRACKS.getName())
                ) {
            TermQuery tq = new TermQuery(new Term(term.field(), NumericUtils.intToPrefixCoded(Integer.parseInt(term.text()))));
            return tq;
        } else {
            return super.newTermQuery(term);
        }
    }

    public Query newRangeQuery(String field,
                               String part1,
                               String part2,
                               boolean inclusive) {

        if (
                (field.equals(TrackIndexField.DURATION.getName())) ||
                (field.equals(TrackIndexField.QUANTIZED_DURATION.getName())) ||
                (field.equals(TrackIndexField.TRACKNUM.getName())) ||
                (field.equals(TrackIndexField.NUM_TRACKS.getName()))
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
