package org.musicbrainz.search.servlet;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.index.ArtistType;


/**
 * Subclasses MultiFieldQueryParser to handle numeric fields that we might want wish to do range queries for and handle type
 * searches specified using integers.
 */
public class ArtistQueryParser extends MultiFieldQueryParser {

    public ArtistQueryParser(java.lang.String[] strings, org.apache.lucene.analysis.Analyzer analyzer) {
        super(strings, analyzer);
    }

    protected Query newTermQuery(Term term) {
        if (term.field() == ArtistIndexField.TYPE.getName()) {
            try {
                int typeId = Integer.parseInt(term.text());
                if (typeId >= ArtistType.getMinSearchId() && typeId <= ArtistType.getMaxSearchId()) {
                    TermQuery tq = new TermQuery(new Term(term.field(), ArtistType.getBySearchId(typeId).getName()));
                    return tq;
                } else {
                    return super.newTermQuery(term);
                }
            }
            catch (NumberFormatException nfe) {
                return super.newTermQuery(term);

            }
        } else {
            return super.newTermQuery(term);
        }
    }
}