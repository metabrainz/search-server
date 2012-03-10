package org.musicbrainz.search.servlet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.NumericUtils;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.index.RecordingIndexField;
import org.musicbrainz.search.servlet.mmd1.ReleaseGroupType;
import org.musicbrainz.search.servlet.mmd1.V1TrackIndexField;


/**
 * Subclasses QueryParser to handle numeric fields that we might want wish to do range queries for and handle type
 * searches specified using integers.
 */
public class RecordingQueryParser extends MultiFieldQueryParser {

    public RecordingQueryParser(java.lang.String[] strings, Analyzer a) {
        super(LuceneVersion.LUCENE_VERSION, strings, a);
    }

    /**
     * Convert Numeric Fields
     *
     * @param term
     * @return
     */
    @Override
    protected Query newTermQuery(Term term) {
        if (
                (term.field() == RecordingIndexField.DURATION.getName()) ||
                        (term.field() == RecordingIndexField.QUANTIZED_DURATION.getName()) ||
                        (term.field() == RecordingIndexField.TRACKNUM.getName()) ||
                        (term.field() == RecordingIndexField.NUM_TRACKS.getName()) ||
                        (term.field() == RecordingIndexField.NUM_TRACKS_RELEASE.getName())
                                                )
                {
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
        }
        else {
            return super.newTermQuery(term);
        }
    }

    /**
     * In V1 would search by track field, now been changed to recording, but need to support the old field
     *
     * @param field
     * @param queryText
     * @return
     * @throws ParseException
     */
    @Override
    protected Query getFieldQuery(String field, String queryText,boolean quoted)  throws ParseException {
        if(field!=null) {
            if( field.equals(V1TrackIndexField.TRACK.getName())) {
                field=RecordingIndexField.RECORDING.getName();
            }
            else if( field.equals(V1TrackIndexField.TRACK_ID.getName())) {
                field=RecordingIndexField.RECORDING_ID.getName();
            }
        }
        return super.getFieldQuery(field,queryText, quoted);
    }

    @Override
    protected Query getFieldQuery(String field, String queryText,int slop)  throws ParseException {
        if(field!=null) {
            if( field.equals(V1TrackIndexField.TRACK.getName())) {
                field=RecordingIndexField.RECORDING.getName();
            }
            else if( field.equals(V1TrackIndexField.TRACK_ID.getName())) {
                field=RecordingIndexField.RECORDING_ID.getName();
            }
        }
        return super.getFieldQuery(field,queryText, slop);
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
                (field.equals(RecordingIndexField.DURATION.getName())) ||
                (field.equals(RecordingIndexField.QUANTIZED_DURATION.getName())) ||
                (field.equals(RecordingIndexField.TRACKNUM.getName())) ||
                (field.equals(RecordingIndexField.NUM_TRACKS.getName())) ||
                (field.equals(RecordingIndexField.NUM_TRACKS_RELEASE.getName()))
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
