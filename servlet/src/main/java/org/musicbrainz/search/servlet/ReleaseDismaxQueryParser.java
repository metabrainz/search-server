/*
 Copyright (c) 2012 Paul Taylor
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

import org.apache.lucene.search.*;
import org.musicbrainz.search.index.ReleaseIndexField;

public class ReleaseDismaxQueryParser extends DismaxQueryParser {

    public ReleaseDismaxQueryParser(org.apache.lucene.analysis.Analyzer analyzer) {
        dqp = new ReleaseDisjunctionQueryParser(IMPOSSIBLE_FIELD_NAME, analyzer);
    }

    /**
     * A catalogno may have been entered with spaces, this will incorrectly be treated as two separate tokens
     * by the term query wheresas the phrase query will correctly analyse it into one term. So in this case
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
                    else  {
                        querySub.setBoost(a.getFields().get(f).getBoost());
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
