package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.musicbrainz.search.LuceneVersion;

import java.util.HashMap;
import java.util.Map;

public class DismaxQueryParser {

    public static String IMPOSSIBLE_FIELD_NAME = "\uFFFC\uFFFC\uFFFC";
    protected DisjunctionQueryParser dqp;

    protected DismaxQueryParser() {
    }

    public DismaxQueryParser(org.apache.lucene.analysis.Analyzer analyzer) {
        dqp = new DisjunctionQueryParser(IMPOSSIBLE_FIELD_NAME, analyzer);
    }

    /**
     * Create query consists of disjunction queries for each term fields combo, and then
     * a phrase search for each field as long as the original query is more than one term
     *
     * @param query
     * @return
     * @throws org.apache.lucene.queryParser.ParseException
     *
     */
    public Query parse(String query) throws org.apache.lucene.queryParser.ParseException {

        Query term = dqp.parse(DismaxQueryParser.IMPOSSIBLE_FIELD_NAME + ":(" + query + ")");
        Query phrase = dqp.parse(DismaxQueryParser.IMPOSSIBLE_FIELD_NAME + ":\"" + query + "\"");
        return buildTopQuery(term, phrase);
    }

    /**
     * If a phrase query was built then we create a boolean query that requires something to match in
     * the term query, under normal circumstances if nothing matches the term query nothing will match the phrase
     * query
     *
     * @param term
     * @param phrase
     * @return
     */
    protected Query buildTopQuery(Query term, Query phrase) {
        if (phrase instanceof DisjunctionMaxQuery) {
            BooleanQuery bq = new BooleanQuery(true);
            bq.add(term, BooleanClause.Occur.MUST);
            bq.add(phrase, BooleanClause.Occur.SHOULD);
            return bq;
        } else {
            return term;
        }
    }


    public void addAlias(String field, DismaxAlias dismaxAlias) {
        dqp.addAlias(field, dismaxAlias);
    }

    static class DisjunctionQueryParser extends QueryParser {

        //Only make terms that are this length fuzzy
        protected static final int MIN_FIELD_LENGTH_TO_MAKE_FUZZY = 4;
        protected static final float FUZZY_SIMILARITY = 0.5f;

        //Reduce boost of wildcard/fuzzy matches compared to exact matches
        protected static final float WILDCARD_BOOST_REDUCER = 0.8f;

        //Reduce phrase query scores otherwise there is too much difference between a document that matches on
        //phrase and one that doesn't quite.
        protected static final float PHRASE_BOOST_REDUCER = 0.2f;


        public DisjunctionQueryParser(String defaultField, org.apache.lucene.analysis.Analyzer analyzer) {
            super(LuceneVersion.LUCENE_VERSION, defaultField, analyzer);
        }


        protected Map<String, DismaxAlias> aliases = new HashMap<String, DismaxAlias>(3);

        //Field to DismaxAlias
        public void addAlias(String field, DismaxAlias dismaxAlias) {
            aliases.put(field, dismaxAlias);
        }

        protected boolean checkQuery(DisjunctionMaxQuery q, Query querySub, boolean quoted, DismaxAlias a, String f) {
            if (querySub != null) {
                //if query was quoted but doesn't generate a phrase query we reject it
                if ((quoted == false) || (querySub instanceof PhraseQuery)) {
                    //Reduce phrase because will have matched both parts giving far too much score differential
                    if (quoted == true) {
                        querySub.setBoost(PHRASE_BOOST_REDUCER);
                    }
                    else {
                        querySub.setBoost(a.getFields().get(f).getBoost());
                    }
                    q.add(querySub);
                    return true;
                }
            }
            return false;
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
                    Query queryFuzzy = null;

                    DismaxAlias.AliasField af = a.getFields().get(f);
                    if (!quoted && queryText.length() >= MIN_FIELD_LENGTH_TO_MAKE_FUZZY) {
                        querySub = getFieldQuery(f, queryText, quoted);
                        if (querySub instanceof TermQuery) {

                            if(af.isFuzzy()) {
                                queryWildcard = getWildcardQuery(((TermQuery) querySub).getTerm().field(), ((TermQuery) querySub).getTerm().text() + '*');
                                queryFuzzy = getFuzzyQuery(((TermQuery) querySub).getTerm().field(), ((TermQuery) querySub).getTerm().text(), FUZZY_SIMILARITY);
                                queryFuzzy.setBoost(af.getBoost() * WILDCARD_BOOST_REDUCER);
                                q.add(queryFuzzy);
                                queryWildcard.setBoost(af.getBoost() * WILDCARD_BOOST_REDUCER);
                                q.add(queryWildcard);
                            }
                        }
                    } else {
                        querySub = getFieldQuery(f, queryText, quoted);
                    }

                    if (checkQuery(q, querySub, quoted, a, f) && ok == false) {
                        ok = true;
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

}