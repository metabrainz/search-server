package org.musicbrainz.search.servlet;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;
import org.musicbrainz.search.index.*;

public class LabelQueryParser extends MultiFieldQueryParser {

    public LabelQueryParser(java.lang.String[] strings, org.apache.lucene.analysis.Analyzer analyzer)
    {
        super(Version.LUCENE_CURRENT,strings,analyzer);
    }

     protected Query newTermQuery(Term term) {

         if(
                 ( term.field() == LabelIndexField.TYPE.getName())

            ) {
            try {
                int typeId = Integer.parseInt(term.text());
                if (typeId >= LabelType.getMinSearchId() && typeId <= LabelType.getMaxSearchId()) {
                    TermQuery tq = new TermQuery(new Term(term.field(),LabelType.getBySearchId(typeId).getName()));
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
