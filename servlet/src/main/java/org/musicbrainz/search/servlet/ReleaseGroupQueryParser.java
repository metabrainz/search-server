package org.musicbrainz.search.servlet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.musicbrainz.search.index.ReleaseGroupType;
import org.musicbrainz.search.index.ReleaseGroupIndexField;


/**
 * Subclasses QueryParser to handle numeric fields that we might want wish to do range queries for and handle type
 * searches specified using integers.
 */
public class ReleaseGroupQueryParser extends QueryParser {

    public ReleaseGroupQueryParser(String field, Analyzer a) {
        super(field, a);
    }

    protected Query newTermQuery(Term term) {
        if( term.field() == ReleaseGroupIndexField.TYPE.getName()) {
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
}