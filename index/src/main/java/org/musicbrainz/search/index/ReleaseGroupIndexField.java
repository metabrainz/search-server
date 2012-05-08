/* Copyright (c) 2009 Aurélien Mino
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the MusicBrainz project nor the names of the
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.musicbrainz.search.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Field;
import org.musicbrainz.search.analysis.*;

/**
 * Fields created in Lucene Search Index
 */
public enum ReleaseGroupIndexField implements IndexField {

	ID		    		("_id",				Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
	ARTIST_ID		    ("arid",			Field.Store.NO,		Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    ARTIST              ("artist",          Field.Store.NO,		Field.Index.ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    ARTIST_CREDIT       ("artistcredit",    Field.Store.YES,    Field.Index.NO),
    ARTIST_NAME         ("artistname",		Field.Store.NO,		Field.Index.ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    ARTIST_NAMECREDIT   ("creditname",	    Field.Store.NO,		Field.Index.ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    COMMENT		        ("comment",		    Field.Store.YES,	Field.Index.ANALYZED),
    NUM_RELEASES        ("releases",	    Field.Store.NO,	    Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    RELEASE             ("release", 		Field.Store.YES,	Field.Index.ANALYZED, new TitleWithPosGapAnalyzer()),
    RELEASE_ID		    ("reid",		    Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    RELEASEGROUP_ID	    ("rgid",			Field.Store.YES,	Field.Index.NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
	RELEASEGROUP	    ("releasegroup",	Field.Store.YES,	Field.Index.ANALYZED, new TitleAnalyzer()),
    RELEASEGROUP_ACCENT ("releasegroupaccent",Field.Store.NO,   Field.Index.ANALYZED, new MusicbrainzKeepAccentsAnalyzer()),
    TAG		            ("tag",		        Field.Store.YES,	Field.Index.ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    TAGCOUNT            ("tagcount",	    Field.Store.YES,	Field.Index.NO),
    TYPE			    ("type",			Field.Store.YES,	Field.Index.ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    RELEASESTATUS       ("status",			Field.Store.YES,	Field.Index.ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    PRIMARY_TYPE        ("primarytype",		Field.Store.YES,	Field.Index.ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    SECONDARY_TYPE      ("secondarytype",   Field.Store.YES,	Field.Index.ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),

    ;

    private String name;
	private Field.Store store;
	private Field.Index index;
    private Analyzer analyzer;

	private ReleaseGroupIndexField(String name, Field.Store store, Field.Index index) {
		this.name = name;
		this.store = store;
		this.index = index;
	}

    private ReleaseGroupIndexField(String name, Field.Store store, Field.Index index, Analyzer analyzer) {
        this(name, store, index);
        this.analyzer = analyzer;
    }

    public String getName() {
		return name;
	}

	public Field.Store getStore() {
		return store;
	}

	public Field.Index getIndex() {
		return index;
	}

    public Analyzer getAnalyzer() {
        return analyzer;
    }


}