package org.musicbrainz.search.servlet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.NumericUtils;
import org.musicbrainz.search.index.PlaceIndexField;
import org.musicbrainz.search.index.RecordingIndexField;
import org.musicbrainz.search.servlet.mmd1.ReleaseGroupType;

/**
 * Subclasses QueryParser to handle numeric fields that we might want wish to do range queries for and handle type
 * searches specified using integers.
 */
public class PlaceQueryParser extends MultiFieldQueryParser
{

    public PlaceQueryParser(String[] strings, Analyzer analyzer)
    {
        super(strings, analyzer);
    }

    @Override
    protected Query newTermQuery(Term term) {
        if (
                (term.field().equals(PlaceIndexField.LAT.getName())) ||
                        (term.field().equals(PlaceIndexField.LONG.getName()))
                )
        {
            try
            {
                float floatnumber = Float.parseFloat(term.text());
                BytesRefBuilder bytes = new BytesRefBuilder();
                NumericUtils.intToPrefixCoded(NumericUtils.floatToSortableInt(floatnumber), 0, bytes);
                TermQuery tq = new TermQuery(new Term(term.field(), bytes.toBytesRef().utf8ToString()));
                return tq;
            }
            catch (NumberFormatException nfe) {
                //If not provided numeric argument just leave as is, won't give matches
                return super.newTermQuery(term);
            }

        }
        else {
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
                (field.equals(PlaceIndexField.LONG.getName())) ||
                (field.equals(PlaceIndexField.LAT.getName()))
                )
        {
            BytesRefBuilder bytes1 = new BytesRefBuilder();
            BytesRefBuilder bytes2 = new BytesRefBuilder();
            NumericUtils.intToPrefixCoded(NumericUtils.floatToSortableInt(Float.parseFloat(part1)), 0, bytes1);
            NumericUtils.intToPrefixCoded(NumericUtils.floatToSortableInt(Float.parseFloat(part2)), 0, bytes2);
            part1 = bytes1.toBytesRef().utf8ToString();
            part2 = bytes2.toBytesRef().utf8ToString();
        }
        TermRangeQuery query = (TermRangeQuery)
                super.newRangeQuery(field, part1, part2, startInclusive, endInclusive);
        return query;

    }

}
