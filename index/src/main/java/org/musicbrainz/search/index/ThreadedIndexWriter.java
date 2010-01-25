/*
 * MusicBrainz Search Server
 * Copyright (C) 2010  Paul Taylor

 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package org.musicbrainz.search.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadedIndexWriter extends IndexWriter {
    private ExecutorService threadPool;
    private Analyzer defaultAnalyzer;

    private class Job implements Runnable {
        Document doc;
        Analyzer analyzer;

        public Job(Document doc, Analyzer analyzer) {
            this.doc = doc;
            this.analyzer = analyzer;
        }

        public void run() {
            try {

                ThreadedIndexWriter.super.addDocument(doc, analyzer);
            } catch (IOException ioe) {
                ioe.printStackTrace(System.err);
            }
        }
    }

    public ThreadedIndexWriter(Directory dir, Analyzer a,
                               boolean create,
                               int numThreads,
                               int maxQueueSize,
                               IndexWriter.MaxFieldLength mfl)

            throws  IOException

    {
        super(dir, a, create, mfl);
        defaultAnalyzer = a;
        threadPool = new ThreadPoolExecutor(
                numThreads, numThreads, 0,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(maxQueueSize, false),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void addDocument(Document doc) {
        threadPool.execute(new Job(doc, defaultAnalyzer));
    }

    public void addDocument(Document doc, Analyzer a) {
        threadPool.execute(new Job(doc,  a));
    }

    public void updateDocument(Term term, Document doc) {
        throw new UnsupportedOperationException();

    }

    public void updateDocument(Term term, Document doc, Analyzer a) {

        throw new UnsupportedOperationException();
    }

    public void close() throws  IOException {
        finish();
        super.close();
    }

    public void close(boolean doWait) throws IOException {
        finish();

        super.close(doWait);
    }

    public void rollback() throws IOException {
        finish();
        super.rollback();
    }

    private void finish() {
        threadPool.shutdown();
        while (true) {
            try {
                if (threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)) {
                    break;
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(ie);
            }
        }
    }
}