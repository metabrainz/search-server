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

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.musicbrainz.search.index.ReleaseIndexField;

import java.util.HashMap;
import java.util.Map;

public class ReleaseDismaxSearch extends ReleaseSearch {

    private DismaxSearcher dismaxSearcher;

    protected void initDismaxSearcher() {
        Map<String, DismaxAlias.AliasField> fieldBoosts = new HashMap<String, DismaxAlias.AliasField>(6);
        fieldBoosts.put(ReleaseIndexField.RELEASE_ACCENT.getName(), new DismaxAlias.AliasField(false, 1.4f));
        fieldBoosts.put(ReleaseIndexField.RELEASE.getName(), new DismaxAlias.AliasField(true, 1.2f));
        fieldBoosts.put(ReleaseIndexField.BARCODE.getName(), new DismaxAlias.AliasField(false, 1.2f));
        fieldBoosts.put(ReleaseIndexField.CATALOG_NO.getName(), new DismaxAlias.AliasField(false, 1.2f));
        fieldBoosts.put(ReleaseIndexField.ARTIST.getName(), new DismaxAlias.AliasField(true, 1f));
        fieldBoosts.put(ReleaseIndexField.ARTIST_NAMECREDIT.getName(), new DismaxAlias.AliasField(true, 1f));
        fieldBoosts.put(ReleaseIndexField.LABEL.getName(), new DismaxAlias.AliasField(true, 0.8f));

        DismaxAlias dismaxAlias = new DismaxAlias();
        dismaxAlias.setFields(fieldBoosts);
        dismaxAlias.setTie(0.1f);
        dismaxSearcher = new ReleaseDismaxSearcher(dismaxAlias);
    }


    /**
     * Standard Search
     *
     * @param searcher
     * @throws Exception
     */
    public ReleaseDismaxSearch(IndexSearcher searcher) throws Exception {
        super(searcher);
        initDismaxSearcher();
    }


    /**
     * User By Search All
     *
     * @param searcher
     * @param query
     * @param offset
     * @param limit
     * @throws Exception
     */
    public ReleaseDismaxSearch(IndexSearcher searcher, String query, int offset, int limit) throws Exception {
        super(searcher, query, offset, limit);
        initDismaxSearcher();
    }

    protected Query parseQuery(String userQuery) throws ParseException {
        return dismaxSearcher.parseQuery(userQuery, analyzer);
    }
}
