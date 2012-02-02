package org.musicbrainz.search.servlet;

import java.util.Map;

public class DismaxAlias {
    public DismaxAlias() {

    }

    private float tie;
    //Field Boosts
    private Map<String, AliasField> fields;

    public float getTie() {
        return tie;
    }

    public void setTie(float tie) {
        this.tie = tie;
    }

    public Map<String, AliasField> getFields() {
        return fields;
    }

    public void setFields(Map<String, AliasField> fields) {
        this.fields = fields;
    }

    static class AliasField {
        private boolean isFuzzy;
        private float boost;

        public AliasField(boolean isFuzzy, float boost) {
            this.isFuzzy=isFuzzy;
            this.boost=boost;
        }

        public boolean isFuzzy() {
            return isFuzzy;
        }

        public void setFuzzy(boolean fuzzy) {
            isFuzzy = fuzzy;
        }

        public float getBoost() {
            return boost;
        }

        public void setBoost(float boost) {
            this.boost = boost;
        }
    }
}