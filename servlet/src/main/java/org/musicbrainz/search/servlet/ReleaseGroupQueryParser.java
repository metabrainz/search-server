/*
 Copyright (c) 2010 Paul Taylor
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.
  2. Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
     documentation and/or other materials provided with the distribution.
  3. Neither the name of the MusicBrainz project nor the names of the
     contributors may be used to endorse or promote products derived from
     this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.musicbrainz.search.servlet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.NumericUtils;
import org.musicbrainz.search.index.RecordingIndexField;
import org.musicbrainz.search.index.ReleaseGroupIndexField;
import org.musicbrainz.search.servlet.mmd1.ReleaseGroupType;


/**
 * Subclasses QueryParser to handle numeric fields that we might want wish to do range queries for and handle type
 * searches specified using integers.
 */
public class ReleaseGroupQueryParser extends MultiFieldQueryParser {

    public ReleaseGroupQueryParser(java.lang.String[] strings, Analyzer a) {
        super(strings, a);
    }

    protected Query newTermQuery(Term term) {
        if (term.field().equals(ReleaseGroupIndexField.TYPE.getName())) {
            try {
                int typeId = Integer.parseInt(term.text());
                if (typeId >= ReleaseGroupType.getMinSearchId() && typeId <= ReleaseGroupType.getMaxSearchId()) {
                    TermQuery tq = new TermQuery(new Term(term.field(), ReleaseGroupType.getBySearchId(typeId).getName()));
                    return tq;
                } else {
                    return super.newTermQuery(term);
                }
            } catch (NumberFormatException nfe) {
                return super.newTermQuery(term);

            }
        } else if (term.field().equals(ReleaseGroupIndexField.NUM_RELEASES.getName())) {
            try {
                int number = Integer.parseInt(term.text());
                BytesRefBuilder bytes = new BytesRefBuilder();
                NumericUtils.intToPrefixCoded(number, 0, bytes);
                TermQuery tq = new TermQuery(new Term(term.field(), bytes.toBytesRef().utf8ToString()));
                return tq;
            } catch (NumberFormatException nfe) {
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
        if (field.equals(ReleaseGroupIndexField.NUM_RELEASES.getName()))
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
