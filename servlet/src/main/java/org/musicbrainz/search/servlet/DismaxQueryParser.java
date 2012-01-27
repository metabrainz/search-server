package org.musicbrainz.search.servlet;

import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.index.ReleaseIndexField;

import java.util.HashMap;
import java.util.Map;

public class DismaxQueryParser {

    public static String IMPOSSIBLE_FIELD_NAME = "\uFFFC\uFFFC\uFFFC";
    private DisjunctionQueryParser dqp;

    public DismaxQueryParser(org.apache.lucene.analysis.Analyzer analyzer) {
        dqp = new DisjunctionQueryParser(IMPOSSIBLE_FIELD_NAME, analyzer);
    }

    public Query parse(String query) throws org.apache.lucene.queryParser.ParseException {

        Query q0     = dqp.parse(DismaxQueryParser.IMPOSSIBLE_FIELD_NAME+":("+query+")");
        Query phrase = dqp.parse(DismaxQueryParser.IMPOSSIBLE_FIELD_NAME+":(\""+query+"\")");
        if (phrase instanceof DisjunctionMaxQuery) {
            BooleanQuery bq = new BooleanQuery(true);
            bq.add(q0, BooleanClause.Occur.MUST);
            bq.add(phrase, BooleanClause.Occur.SHOULD);
            System.out.println(bq);
            return bq;
        }
        else {
            System.out.println(q0);
            return q0;
        }

    }

    public void addAlias(String field, DismaxAlias dismaxAlias) {
                dqp.addAlias(field, dismaxAlias);
    }

    static class DisjunctionQueryParser extends QueryParser {


        public DisjunctionQueryParser(String defaultField, org.apache.lucene.analysis.Analyzer analyzer) {
                super(LuceneVersion.LUCENE_VERSION, defaultField, analyzer);
            }


        protected Map<String, DismaxAlias> aliases = new HashMap<String, DismaxAlias>(3);

        //Field to DismaxAlias
        public void addAlias(String field, DismaxAlias dismaxAlias) {
            aliases.put(field, dismaxAlias);
        }

        protected Query getFieldQuery(String field, String queryText, boolean quoted) {
            //If field is an alias
            if (aliases.containsKey(field)) {

                DismaxAlias a = aliases.get(field);
                DisjunctionMaxQuery q = new DisjunctionMaxQuery(a.getTie());
                boolean ok = false;

                for (String f : a.getFields().keySet()) {

                    //if query can be created for this field and text
                    Query sub = getFieldQuery(f, queryText, quoted);
                    if(quoted==true)
                    {
                        System.out.println("field:"+f+":"+queryText+":"+sub.getClass());
                    }
                    if (sub != null) {
                        //if query was quoted but doesnt generate a phrase query we reject unless it is a keyword field
                        if(
                                (quoted==false) ||
                                (sub instanceof PhraseQuery) ||
                                (f.equals(ReleaseIndexField.CATALOG_NO.getName()))
                          )
                        {
                            //If Field has a boost
                            if (a.getFields().get(f) != null) {
                                sub.setBoost(a.getFields().get(f));
                            }
                            q.add(sub);
                            ok = true;
                        }
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
        public DismaxAlias()
        {

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