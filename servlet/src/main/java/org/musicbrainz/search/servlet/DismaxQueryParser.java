package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.musicbrainz.search.LuceneVersion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DismaxQueryParser {

    public static String IMPOSSIBLE_FIELD_NAME = "\uFFFC\uFFFC\uFFFC";
    private DisjunctionQueryParser dqp;

    public DismaxQueryParser(org.apache.lucene.analysis.Analyzer analyzer) {
        dqp = new DisjunctionQueryParser(IMPOSSIBLE_FIELD_NAME, analyzer);
    }

    public Query parse(String query) throws org.apache.lucene.queryParser.ParseException {

        Query q0 = dqp.parse(DismaxQueryParser.IMPOSSIBLE_FIELD_NAME + ":(" + query + ")");
        Query phrase = dqp.parse(DismaxQueryParser.IMPOSSIBLE_FIELD_NAME + ":\"" + query + "\"");
        if (phrase instanceof DisjunctionMaxQuery) {
            BooleanQuery bq = new BooleanQuery(true);
            bq.add(q0, BooleanClause.Occur.MUST);
            bq.add(phrase, BooleanClause.Occur.SHOULD);
            return bq;
        }
        else {
            return q0;
        }

    }

    public void addAlias(String field, DismaxAlias dismaxAlias) {
        dqp.addAlias(field, dismaxAlias);
    }

    static class DisjunctionQueryParser extends QueryParser {

        //Only make terms that are this length fuzzy
        private static final int MIN_FIELD_LENGTH_TO_MAKE_FUZZY = 4;
        private static final float FUZZY_SIMILARITY = 0.5f;

        //Reduce boost of wildcard/fuzzy matches compared to exact matches
        private static final float WILDCARD_BOOST_REDUCER = 0.8f;

        //Reduce phrase query scores otherwise there is too much difference between a document that matches on
        //phrase and one that doesn't quite.
        private static final float PHRASE_BOOST_REDUCER   = 0.2f;


        public DisjunctionQueryParser(String defaultField, org.apache.lucene.analysis.Analyzer analyzer) {
            super(LuceneVersion.LUCENE_VERSION, defaultField, analyzer);
        }


        protected Map<String, DismaxAlias> aliases = new HashMap<String, DismaxAlias>(3);

        //Field to DismaxAlias
        public void addAlias(String field, DismaxAlias dismaxAlias) {
            aliases.put(field, dismaxAlias);
        }

        protected Query getFieldQuery(String field, String queryText, boolean quoted)
                throws org.apache.lucene.queryParser.ParseException {
            //If field is an alias
            if (aliases.containsKey(field)) {

                DismaxAlias a = aliases.get(field);
                DisjunctionMaxQuery q = new DisjunctionMaxQuery(a.getTie());
                boolean ok = false;

                for (String f : a.getFields().keySet()) {

                    //if query can be created for this field and text
                    Query querySub;
                    Query queryWildcard = null;
                    Query queryFuzzy    = null;

                    if (!quoted && queryText.length() >= MIN_FIELD_LENGTH_TO_MAKE_FUZZY) {
                        querySub = getFieldQuery(f, queryText, quoted);
                        queryWildcard = getWildcardQuery(((TermQuery) querySub).getTerm().field(), ((TermQuery) querySub).getTerm().text() + '*');
                        queryFuzzy = getFuzzyQuery(((TermQuery) querySub).getTerm().field(), ((TermQuery) querySub).getTerm().text(), FUZZY_SIMILARITY);
                    } else {
                        querySub = getFieldQuery(f, queryText, quoted);
                    }

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
                            ok = true;
                        }
                    }

                    if (queryFuzzy != null) {
                        if (a.getFields().get(f) != null) {
                            queryFuzzy.setBoost(a.getFields().get(f) * WILDCARD_BOOST_REDUCER);
                        }
                        q.add(queryFuzzy);
                    }

                    if (queryWildcard != null) {
                        if (a.getFields().get(f) != null) {
                            queryWildcard.setBoost(a.getFields().get(f) * WILDCARD_BOOST_REDUCER);
                        }
                        q.add(queryWildcard);
                    }
                }
                //Something has been added to disjunction query
                return ok ? q : null;

            } else {
                //usual Field
                try {
                    return super.getFieldQuery(field, queryText, quoted);
                } catch (Exception e) {
                    return null;
                }
            }
        }
    }

    static class DismaxAlias {
        public DismaxAlias() {

        }

        private float tie;
        //Field Boosts
        private Map<String, Float> fields;

        public float getTie() {
            return tie;
        }

        public void setTie(float tie) {
            this.tie = tie;
        }

        public Map<String, Float> getFields() {
            return fields;
        }

        public void setFields(Map<String, Float> fields) {
            this.fields = fields;
        }
    }
}