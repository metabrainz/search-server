/* Copyright (c) 2009 Lukas Lalinsky
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

package org.musicbrainz.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocCollector;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.NIOFSDirectory;
import org.musicbrainz.search.analysis.StandardUnaccentAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class SearchServer {

    final Logger log = Logger.getLogger(SearchServer.class.getName());

    private Analyzer analyzer;
    private Map<ResourceType, IndexSearcher> searchers;         //Maps resource to index
    private Map<ResourceType, String> defaultSearchFields;      //Maps resource to default search field

    public SearchServer(String indexDir) throws IOException {
        analyzer = new StandardUnaccentAnalyzer();

        searchers = new HashMap<ResourceType, IndexSearcher>();
        searchers.put(ResourceType.ARTIST, new IndexSearcher(IndexReader.open(new NIOFSDirectory(new File(indexDir + "/artist_index/"), null), true)));
        searchers.put(ResourceType.LABEL, new IndexSearcher(IndexReader.open(new NIOFSDirectory(new File(indexDir + "/label_index/"), null), true)));
        searchers.put(ResourceType.RELEASE, new IndexSearcher(IndexReader.open(new NIOFSDirectory(new File(indexDir + "/release_index/"), null), true)));
        searchers.put(ResourceType.TRACK, new IndexSearcher(IndexReader.open(new NIOFSDirectory(new File(indexDir + "/track_index/"), null), true)));
        searchers.put(ResourceType.RELEASE_GROUP, new IndexSearcher(IndexReader.open(new NIOFSDirectory(new File(indexDir + "/releasegroup_index/"), null), true)));
        searchers.put(ResourceType.FREEDB, new IndexSearcher(IndexReader.open(new NIOFSDirectory(new File(indexDir + "/freedb_index/"), null), true)));
        setupDefaultSearchFields();
    }

    public SearchServer(Map<ResourceType, IndexSearcher> searchers) {
        analyzer = new StandardUnaccentAnalyzer();
        this.searchers = searchers;
        setupDefaultSearchFields();
    }

    private void setupDefaultSearchFields() {
        defaultSearchFields = new HashMap<ResourceType, String>();
        defaultSearchFields.put(ResourceType.ARTIST, ArtistIndexField.ARTIST.getName());
        defaultSearchFields.put(ResourceType.LABEL, LabelIndexField.LABEL.getName());
        defaultSearchFields.put(ResourceType.RELEASE, ReleaseIndexField.RELEASE.getName());
        defaultSearchFields.put(ResourceType.TRACK, TrackIndexField.TRACK.getName());
        defaultSearchFields.put(ResourceType.RELEASE_GROUP, ReleaseGroupIndexField.RELEASEGROUP.getName());
        //TODO: support multi default search fields and make artist & title default fields
        defaultSearchFields.put(ResourceType.FREEDB, FreeDBIndexField.ARTIST.getName());
    }

    public void close() {
    }

    public Results search(ResourceType resourceType, String query, int offset, int limit) throws IOException {
        //log.info("Searching for '" + query + "' in the " + indexName + " index.");
        IndexSearcher searcher = searchers.get(resourceType);
        QueryParser parser = new QueryParser(defaultSearchFields.get(resourceType), analyzer);
        TopDocCollector collector = new TopDocCollector(offset + limit);
        try {
            searcher.search(parser.parse(query), collector);
        }
        catch (ParseException e) {
        }
        Results results = new Results();
        results.offset = offset;
        results.totalHits = collector.getTotalHits();
        TopDocs topDocs = collector.topDocs();
        ScoreDoc docs[] = topDocs.scoreDocs;
        float maxScore = topDocs.getMaxScore();
        for (int i = offset; i < docs.length; i++) {
            Result result = new Result();
            result.score = docs[i].score / maxScore;
            result.doc = new MbDocument(searcher.doc(docs[i].doc));
            results.results.add(result);
        }
        return results;
    }

}
