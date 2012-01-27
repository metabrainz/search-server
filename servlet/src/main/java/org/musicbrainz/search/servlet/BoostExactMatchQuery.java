package org.musicbrainz.search.servlet;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.CustomScoreProvider;
import org.apache.lucene.search.function.CustomScoreQuery;
import org.musicbrainz.search.index.ArtistIndexField;

import java.io.IOException;

/**
 * This is applied as an additional query and boosts the score of docs if the original field
 * matches exactly the value of the query for the specified field. This is useful to ensure
 * accents are considered when finding the best match because accents are ignored during the
 * matching process.
 */
public class BoostExactMatchQuery extends CustomScoreQuery {

    private String userQuery;
    private String compareField;

    public BoostExactMatchQuery(Query query, String userQuery, String compareField) {
        super(query);
        this.userQuery = userQuery;
        this.compareField=compareField;
    }

    public CustomScoreProvider getCustomScoreProvider(IndexReader r) throws IOException {
        return new ExactMatcherBooster(r);
    }

    private class ExactMatcherBooster extends CustomScoreProvider {

        public ExactMatcherBooster(IndexReader r) throws IOException {
            super(r);

        }

        public float customScore(int docNo, float subQueryScore, float valSrcSource) {

            try {
                org.apache.lucene.document.Document doc = this.reader.document(docNo);
                if (userQuery.equals(doc.getValues(compareField)[0])) {
                    return subQueryScore * (1.1f);
                } else {
                    return subQueryScore;
                }
            } catch (Exception ex) {
                return subQueryScore;
            }
        }
    }
}
