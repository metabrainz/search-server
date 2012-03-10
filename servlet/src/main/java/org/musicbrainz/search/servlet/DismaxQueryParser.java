package org.musicbrainz.search.servlet;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.musicbrainz.search.LuceneVersion;

import java.io.IOException;
import java.util.*;

public class DismaxQueryParser {

    public static String IMPOSSIBLE_FIELD_NAME = "\uFFFC\uFFFC\uFFFC";
    protected DisjunctionQueryParser dqp;

    protected DismaxQueryParser() {
    }

    public DismaxQueryParser(org.apache.lucene.analysis.Analyzer analyzer) {
        dqp = new DisjunctionQueryParser(IMPOSSIBLE_FIELD_NAME, analyzer);
        //Change rewrite method used by prefix queries here
        //dqp.setMultiTermRewriteMethod(new MultiTermQuery.TopTermsBoostOnlyBooleanQueryRewrite(100));
        //dqp.setMultiTermRewriteMethod(ScoringRewrite.SCORING_BOOLEAN_QUERY_REWRITE);
        dqp.setMultiTermRewriteMethod(new MultiTermUseIdfOfSearchTerm(100));
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

        //Only make search terms that are this length fuzzy searchable and only match to terms that are also this length
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

        //Rewrite Method used by Prefix Search and Fuzzy Search, use idf of the original term
        MultiTermQuery.RewriteMethod fuzzyAndPrefixQueryRewriteMethod
                = new MultiTermUseIdfOfSearchTerm(100);

        protected boolean checkQuery(DisjunctionMaxQuery q, Query querySub, boolean quoted, DismaxAlias a, String f) {
            if (querySub != null) {
                //if query was quoted but doesn't generate a phrase query we reject it
                if ((!quoted) || (querySub instanceof PhraseQuery)) {
                    //Reduce phrase because will have matched both parts giving far too much score differential
                    if (quoted) {
                        querySub.setBoost(PHRASE_BOOST_REDUCER);
                    } else {
                        querySub.setBoost(a.getFields().get(f).getBoost());
                    }
                    q.add(querySub);
                    return true;
                }
            }
            return false;
        }

        @Override
        protected Query getFuzzyQuery(String field, String termStr, float minSimilarity) {
            Term t = new Term(field, termStr);
            FuzzyQuery fq = new FuzzyQuery(t, minSimilarity, MIN_FIELD_LENGTH_TO_MAKE_FUZZY);
            fq.setRewriteMethod(fuzzyAndPrefixQueryRewriteMethod);
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
                    Query queryFuzzy = null;

                    DismaxAlias.AliasField af = a.getFields().get(f);
                    if (!quoted && queryText.length() >= MIN_FIELD_LENGTH_TO_MAKE_FUZZY) {
                        querySub = getFieldQuery(f, queryText, quoted);
                        if (querySub instanceof TermQuery) {

                            if (af.isFuzzy()) {
                                Term t = ((TermQuery) querySub).getTerm();
                                queryWildcard = newPrefixQuery(new Term(t.field(), t.text()));
                                queryFuzzy = getFuzzyQuery(t.field(), t.text(), FUZZY_SIMILARITY);
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

        /**
         * Builds a new PrefixQuery instance
         * @param prefix Prefix term
         * @return new PrefixQuery instance
         */
        protected Query newPrefixQuery(Term prefix){
            PrefixQuery query = new PrefixQuery(prefix);
            query.setRewriteMethod(fuzzyAndPrefixQueryRewriteMethod);
            return query;
        }
    }

    public static class MultiTermUseIdfOfSearchTerm<Q extends Query> extends TopTermsRewrite<BooleanQuery> {

    //public static final class MultiTermUseIdfOfSearchTerm extends TopTermsRewrite<BooleanQuery> {
        private final Similarity similarity;

        /**
         * Create a TopTermsScoringBooleanQueryRewrite for
         * at most <code>size</code> terms.
         * <p>
         * NOTE: if {@link BooleanQuery#getMaxClauseCount} is smaller than
         * <code>size</code>, then it will be used instead.
         */
        public MultiTermUseIdfOfSearchTerm(int size) {
            super(size);
            this.similarity = new DefaultSimilarity();

        }

        @Override
        protected int getMaxSize() {
            return BooleanQuery.getMaxClauseCount();
        }

        @Override
        protected BooleanQuery getTopLevelQuery() {
            return new BooleanQuery(true);
        }

        @Override
        protected void addClause(BooleanQuery topLevel, Term term, float boost) {
            final Query tq = new ConstantScoreQuery(new TermQuery(term));
            tq.setBoost(boost);
            topLevel.add(tq, BooleanClause.Occur.SHOULD);
        }

        protected float getQueryBoost(final IndexReader reader, final MultiTermQuery query)
                throws IOException {
            float idf = 1f;
            float df;
            if (query instanceof PrefixQuery)
            {
                PrefixQuery fq = (PrefixQuery) query;
                df = reader.docFreq(fq.getPrefix());
                if(df>=1)
                {
                    //Same as idf value for search term, 0.5 acts as length norm
                    idf = (float)Math.pow(similarity.idf((int) df, reader.numDocs()),2) * 0.5f;
                }
            }
            return idf;
        }

        @Override
        public BooleanQuery rewrite(final IndexReader reader, final MultiTermQuery query) throws IOException {
            BooleanQuery  bq = (BooleanQuery)super.rewrite(reader, query);

            float idfBoost = getQueryBoost(reader, query);
            Iterator<BooleanClause> iterator = bq.iterator();
            while(iterator.hasNext())
            {
                BooleanClause next = iterator.next();
                next.getQuery().setBoost(next.getQuery().getBoost() * idfBoost);
            }
            return bq;
        }

    }
}