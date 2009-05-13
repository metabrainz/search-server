#!/usr/bin/env python
#---------------------------------------------------------------------------
#
#   lucene search server -- The MusicBrainz text search back end
#   
#   Copyright (C) Robert Kaye 2006
#   
#   This file is part of lucene search server.
#
#   pimpmytunes is free software; you can redistribute it and/or modify
#   it under the terms of the GNU General Public License as published by
#   the Free Software Foundation; either version 2 of the License, or
#   (at your option) any later version.
#
#   pimpmytunes is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#   GNU General Public License for more details.
#
#   You should have received a copy of the GNU General Public License
#   along with pimpmytunes; if not, write to the Free Software
#   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
#
#---------------------------------------------------------------------------

import sys, os
import PyLucene
from unac import unac
from analyzers.unaccent import StandardUnaccentAnalyzer
import time

# TODO: 
# auto index update

MAX_HITS = 25

htmlArtistType =  (u'', u'person', u'group', u'project')
htmlAlbumType =   (u'', u'album', u'single', u'EP', u'comp.', u'soundtrk', 
                        u'spokenword', u'interview', u'audiobook', u'live', u'remix', u'other')
htmlAlbumStatus = (u'', u'official', u'promo.', u'bootleg', u'pseudorelease')
htmlLabelType =   (u'', u'distributor', u'holding', u'production', 
                   u'original production', u'bootleg production', u'reissue production')

oddeven = [u'even', u'odd']

class QueryError(Exception):
    pass

class SearchError(Exception):
    pass

class NoResultsError(Exception):
    pass


class TextSearch(object):
    '''
    This class carries out HTML/XML searches
    '''
 
    def __init__(self, indexName):
 
        self.tport = 0
        self.duration = 0
        self.mbt = 0
        self.rel = 0
        self.offset = 0

        self.useMulti = True
        self.defaultField = "artist"
        self.defaultMultiFields = ["artist", "album", "track"]
        self.fields = [] 
        self.analyzer = self.getAnalyzer()
        try:
            self.index = PyLucene.IndexSearcher(PyLucene.FSDirectory.getDirectory(indexName, False))
        except ValueError:
            raise indexsearch.NoSuchIndexError

    def close(self):
        self.index.close();
 
    def getAnalyzer(self):
        '''
        Return the Lucene analyzer object to use with this index
        This function must be overridden by deriving classes.
        '''
        return StandardUnaccentAnalyzer()

    def setFields(self, fields): 
        ''' 
        This function sets the actual fields the caller wants searched. (not the default fields) 
        ''' 
        self.fields = fields 
 
    def useMultiFields(self, multi):
        ''' 
        If set, use lucene's MultiFieldQueryParser
        '''
        self.useMulti = multi
 
    def setDefaultField(self, default):
        ''' 
        If set, use lucene's QueryParser for searching on one default field
        '''
        self.defaultField = default
 
    def setDefaultMultiFields(self, fields):
        '''
        The multi fields searched when the users specifies no multi fields
        '''
        self.defaultMultiFields = fields
 
    def escape(self, text):
        '''
        Escape XML/HTML entities and convert output to utf-8
        '''
        return text.replace(u'&', u'&amp;').replace(u'<', u'&lt;').replace(u'>', u'&gt;')
 
    def unEscapeUUID(self, uuid):
        '''
        Convert a dashless UUID to a standard UUID with dashes.
        '''
        return "%s-%s-%s-%s-%s" % (uuid[0:8], uuid[8:12], uuid[12:16], uuid[16:20], uuid[20:32])

    def setDuration(self, dur):
        '''
        If a duration is set, the search can color code track lengths
        '''
        self.dur = dur
     
    def getTrackLenClass(self, matchDur):
        '''
        This function returns the tlen css class based on the duration of a found match. This
        way the track durations can be color coded in the HTML output.
        '''
        if not matchDur or not self.dur: return ""
        diff = abs(self.dur - matchDur)
        if diff < 5000: return "good"
        if diff < 15000: return "ok"
        return "bad"
 
    def setTaggerPort(self, tport):
        '''
        To emit tagger links we need to know the local port number to connect to.
        '''
        self.tport = tport
    
    def setShowRelationshipLink(self, rel):
        '''
        The type of link (if any) to display under the Relationship column
        '''
        self.rel = int(rel);

    def taggerLink(self, port, mbid):
        '''
        generate the HTML for a tagger link based on the tagger port and the release id
        '''
        out = u'<a href="http://127.0.0.1:%d/openalbum?id=%s&amp;t=%d" ' % (port, mbid, time.time())
        out += u' target="hiddeniframe" title="Open in Tagger" border="0"><img  '
        out += u' src="/images/mblookup-tagger.png" border="0" alt="Open in tagger"></a>'
        return out

    def setMBT(self, mbt):
        '''
        Set if we need to emit old style tagger links.
        '''
        self.mbt = mbt

    def mbtLink(self, trackid, releaseid):
        '''
        Emit an old style tagger link
        '''
        out = u'<a href="tag:%s:%s">' % (trackid, releaseid)
        out += u'<img src="/images/mblookup-tag.gif" alt="Tag" title="Tag the current track" '
        out += u'height="13" width="28" align="middle" border="0">'
        return out

    def mangleQuery(self, query):
        '''
        For backwards compatibility, filter a query before passing it to lucene. Returns
        filtered query. e.g. type:1 -> type:person (for artists)
        '''
        return query

    def queryIndex(self, query):
        '''
        Carry out a search, and return the hits
        '''

        if not query: raise QueryError(u"No query was sent")

        # remove accents from the search query
        try:
            query = unac.unac_string(self.mangleQuery(unicode(query, 'utf-8')))
        except UnicodeDecodeError:
            raise QueryError(u"Unicode decode problem: Invalid utf-8 characters passed to search query.")
            
        parsedQuery = None
        if self.useMulti:
            fields = [] 
            if len(self.fields): 
                fields = self.fields 
            else: 
                fields = self.defaultMultiFields 
            try:
                parsedQuery = PyLucene.MultiFieldQueryParser(fields, self.analyzer).parse(query)
            except Exception, msg:
                text = unicode(msg)
                raise QueryError(text)
        else:
            try:
                parsedQuery = PyLucene.QueryParser(self.defaultField, self.analyzer).parse(query)
            except Exception, msg:
                text = unicode(msg)
                raise QueryError(text)

        hits = []
        err = ''
        try:
            hits = self.index.search(parsedQuery);
        except Exception, msg:
            text = str(msg)
            raise QueryError(text)

        if not hits: raise NoResultsError()
 
        return hits
 
    def log_error(self, msg):
	log = open("/tmp/slow_queries.txt", "a")
	if log:
	    log.write(msg)
	    log.flush()
            log.close()
 
    def search(self, query, maxHits, offset, type='xml'):
        if maxHits < 1: maxHits = MAX_HITS
        self.offset = offset
        hits = self.queryIndex(query);
        redirect = ""
        if len(hits) == 1:
           doc = hits.doc(0)
           redirect = doc.get('trid')
           if not redirect: redirect = doc.get('reid')
           if not redirect: redirect = doc.get('arid')

        if type == 'html':
            # This comment will be used by the MB server to determine the number of hits returned
            stats = u"<!--\nhits=%d\noffset=%d\n" % (len(hits), offset)
            if redirect: stats += u"redirect=%s\n" % redirect
            stats += u"-->"
            return stats.encode('utf-8') + self.asHTML(hits, maxHits, offset).encode('utf-8')
        else:
            return self.asXML(hits, maxHits, offset).encode('utf-8')
