package org.musicbrainz.search.servlet;

import org.apache.lucene.search.*;
import org.musicbrainz.search.index.ReleaseIndexField;

public class ReleaseDismaxQueryParser extends DismaxQueryParser {

    public ReleaseDismaxQueryParser(org.apache.lucene.analysis.Analyzer analyzer) {
        dqp = new ReleaseDisjunctionQueryParser(IMPOSSIBLE_FIELD_NAME, analyzer);
    }

    /**
     * A catalogno may have been entered with spaces, this will incorrectly be treated as two separate tokens
     * by the term query wheres as the phrase query will correctly analyse it into one term. So in this case
     * the term query could return no matches whereas the phrase query could get a match.
     *
     * @param term
     * @param phrase
     * @return
     */
    @Override
    protected Query buildTopQuery(Query term, Query phrase) {
        if (phrase instanceof DisjunctionMaxQuery) {
            BooleanQuery bq = new BooleanQuery(true);
            bq.add(term, BooleanClause.Occur.SHOULD);
            bq.add(phrase, BooleanClause.Occur.SHOULD);
            return bq;
        } else {
            return term;
        }
    }

    static class ReleaseDisjunctionQueryParser extends DisjunctionQueryParser {

        public ReleaseDisjunctionQueryParser(String defaultField, org.apache.lucene.analysis.Analyzer analyzer) {
            super(defaultField, analyzer);
        }

        /**
         * Overridden to allow catnos containing spaces to be added as part of phrase search so they can be matched
         *
         *
         */
        @Override
        protected boolean checkQuery(DisjunctionMaxQuery q, Query querySub, boolean quoted, DismaxAlias a, String f) {
            if (querySub != null) {
                //if query was quoted but doesn't generate a phrase query we reject it
                if (
                        (quoted == false) ||
                        (querySub instanceof PhraseQuery)
                    ) {
                    //Reduce phrase because will have matched both parts giving far too much score differential
                    if (quoted == true) {
                        querySub.setBoost(PHRASE_BOOST_REDUCER);
                    }
                    //Boost as specified
                    else if (a.getFields().get(f) != null) {
                        querySub.setBoost(a.getFields().get(f));
                    }
                    q.add(querySub);
                    return true;
                // Unless it is a catalogno
                } else if (f.equals(ReleaseIndexField.CATALOG_NO.getName())) {
                    querySub.setBoost(PHRASE_BOOST_REDUCER);
                    q.add(querySub);
                    return true;
                }
            }
            return false;
        }
    }
}
