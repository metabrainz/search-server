package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.musicbrainz.search.LuceneVersion;

import java.util.HashMap;
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
            DisjunctionMaxQuery bq = new DisjunctionMaxQuery(0.0f);
            bq.add(q0);
            bq.add(phrase);
            return bq;
        } else {
            //System.out.println(q0);
            return q0;
        }

    }

    public void addAlias(String field, DismaxAlias dismaxAlias) {
        dqp.addAlias(field, dismaxAlias);
    }

    static class DisjunctionQueryParser extends QueryParser {

        //Only make terms that are this length fuzzy
        private static final int MIN_FIELD_LENGTH_TO_MAKE_FUZZY = 4;
        private static final float FUZZY_SIMILARITY = 0.7f;

        //Reduce boost of wildcard matches compared to fuzzy /exact matches
        private static final float WILDCARD_BOOST_REDUCER = 0.8f;

        public DisjunctionQueryParser(String defaultField, org.apache.lucene.analysis.Analyzer analyzer) {
            super(LuceneVersion.LUCENE_VERSION, defaultField, analyzer);

        }


        protected Map<String, DismaxAlias> aliases = new HashMap<String, DismaxAlias>(3);

        //Field to DismaxAlias
        public void addAlias(String field, DismaxAlias dismaxAlias) {
            aliases.put(field, dismaxAlias);
        }

        protected org.apache.lucene.search.Query getFuzzyQuery(java.lang.String field, java.lang.String termStr, float minSimilarity)
                throws org.apache.lucene.queryParser.ParseException {
            FuzzyQuery fq = (FuzzyQuery) super.getFuzzyQuery(field, termStr, minSimilarity);
            //so that fuzzy queries term do not get an advantage over exact matches just because the query term is rarer
            fq.setRewriteMethod(new MultiTermQuery.TopTermsBoostOnlyBooleanQueryRewrite(100));
            return fq;
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

                    if (!quoted && queryText.length() >= MIN_FIELD_LENGTH_TO_MAKE_FUZZY) {
                        querySub = getFieldQuery(f, queryText, quoted);
                        queryWildcard = getWildcardQuery(((TermQuery) querySub).getTerm().field(), ((TermQuery) querySub).getTerm().text() + '*');
                        querySub = getFuzzyQuery(((TermQuery) querySub).getTerm().field(), ((TermQuery) querySub).getTerm().text(), FUZZY_SIMILARITY);
                    } else {
                        querySub = getFieldQuery(f, queryText, quoted);
                    }

                    if (querySub != null) {
                        //if query was quoted but doesn't generate a phrase query we reject it
                        if (
                                (quoted == false) ||
                                        (querySub instanceof PhraseQuery)
                                ) {
                            //If Field has a boost
                            if (a.getFields().get(f) != null) {
                                querySub.setBoost(a.getFields().get(f));
                            }
                            q.add(querySub);
                            ok = true;
                        }
                    }

                    if (queryWildcard != null) {
                        if (a.getFields().get(f) != null) {
                            queryWildcard.setBoost(a.getFields().get(f)*WILDCARD_BOOST_REDUCER);
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