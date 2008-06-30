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
import xapian
from unac import unac
from escape_ideographic import addSpacesToIdeographicStrings
import time
import re

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

class NoSuchIndexError(Exception):
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

	self.escapeTermBoost = re.compile(u"\^[0-9]*\.?[0-9]+")
	self.dotsRe = re.compile("((?:\w\.){2,})")
	self.uuidRe = re.compile("[a-z0-9]{8}[:-][a-z0-9]{4}[:-][a-z0-9]{4}[:-][a-z0-9]{4}[:-][a-z0-9]{12}")

        self.defaultField = u''
        try:
            self.index = xapian.Database(indexName)
        except xapian.Error, msg:
	    text = str(msg)
            raise NoSuchIndexError(text)

        self.en = enquire = xapian.Enquire(self.index)
        self.weight = xapian.BM25Weight(0, 0, 1, .5, .5);
        self.en.set_weighting_scheme(self.weight)
        self.en.set_docid_order(xapian.Enquire.DONT_CARE)
        self.en.set_sort_by_relevance_then_value(0, False)

        self.qp = xapian.QueryParser()
        self.qp.set_database(self.index)
        self.qp.set_stemming_strategy(xapian.QueryParser.STEM_NONE)

    def close(self):
	'''
	Close the index
	'''
        del self.index

    def setPrefixes(self, prefixes):
	'''
	Set the mapping of field prefixes
	'''
	for prefix in prefixes:
            self.qp.add_prefix(prefix, "X" + prefix.upper())
 
    def setDefaultField(self, default):
        ''' 
        If set, use lucene's QueryParser for searching on one default field
        '''
        self.defaultField = 'X' + default.upper()
 
    def escape(self, text):
        '''
        Escape XML/HTML entities and convert output to utf-8
        '''
        return text.replace(u'&', u'&amp;').replace(u'<', u'&lt;').replace(u'>', u'&gt;')
 
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

    def lowercaseQuery(self, query):
	'''
	Change the query to all lower case since Xapian seems to have some case sensitivity issues.
	But, don't change everything to lowercase -- AND, OR, NOT, XOR in uppercase must be preserved.
	'''

	out = []
	for word in query.split(u' '):
	    if word not in (u'AND', u'OR', u'NOT', u'XOR'): word = word.lower()
	    out.append(word)

        return u' '.join(out)

    def mangleQuery(self, query):
        '''
        For backwards compatibility, filter a query before passing it to lucene. Returns
        filtered query. e.g. type:1 -> type:person (for artists)
        '''
        return query

    def removeTermBoosting(self, query):
        '''
	Removes term boosting from old queries since Xapain doesn't support them
        '''
        return self.escapeTermBoost.sub("", query)

    def removeDots(self, query):
	'''
	Remove dots between characters so we can find R.E.M.
	'''

	bits = []
	index = 0
	for m in self.dotsRe.finditer(query):
	    bits.append(query[index:m.start()])
	    acronym = query[m.start():m.end()]
	    bits.append(acronym.replace(u".", u""))
	    index = m.end()

	if index < len(query):
	    bits.append(query[index:len(query)])

	return u''.join(bits)


    def removeApostrophe(self, query):
	'''
	Xapian considers ' as part of a word in order to not split things like "don't" and generate a bunch
	of t fragments. I can see that for text, but for searching names and titles, no so much.
	'''
	return query.replace(u"'", u'')

    def removeDashesFromMBIDs(self, query):

	bits = []
	index = 0
	for m in self.uuidRe.finditer(query):
	    bits.append(query[index:m.start()])
	    acronym = query[m.start():m.end()]
	    bits.append(acronym.replace(u"-", u""))
	    index = m.end()

	if index < len(query):
	    bits.append(query[index:len(query)])

	return u''.join(bits)

    def queryIndex(self, query, offset, maxHits):
        '''
        Carry out a search, and return the hits
        '''

        if not query: raise QueryError(u"No query was sent")

        try:
            query = unicode(query, 'utf-8')
	    #self.f = open("/tmp/log", "a")
	    #print >>self.f, "query: '%s'" % query.encode('utf-8', 'replace') 
	    #self.f.close()
            query = self.lowercaseQuery(query)
            query = self.mangleQuery(query)
            query = self.removeTermBoosting(query)
            query = self.removeDots(query)
            query = self.removeApostrophe(query)
	    query = self.removeDashesFromMBIDs(query)
            query = unac.unac_string(query)
            query = addSpacesToIdeographicStrings(query)
        except UnicodeDecodeError:
            raise QueryError(u"Unicode decode problem: Invalid utf-8 characters passed to search query.")

        try:
	    parsedQuery = self.qp.parse_query(query, 
			      	              xapian.QueryParser.FLAG_PHRASE | 
				              xapian.QueryParser.FLAG_BOOLEAN | 
				              xapian.QueryParser.FLAG_LOVEHATE |
				              xapian.QueryParser.FLAG_PURE_NOT,
				              self.defaultField)
        except xapian.Error, msg:
	    text = str(msg)
	    raise QueryError(text)

        try:
	    self.en.set_query(parsedQuery)
	    matches = self.en.get_mset(offset, maxHits)
        except Exception, msg:
            text = str(msg)
            raise SearchError(text)

        if not matches.get_matches_estimated(): raise NoResultsError()

        hits = []
        for match in matches:
	    data = match.document.get_data()
            data = unicode(data, 'utf-8')
            dataDict = {}
            for pair in data.split(u"\n"):
		if not pair: continue
		key, value = pair.split(u"=", 1)
		dataDict[key] = value

	    dataDict['_score'] = match.percent
	    hits.append(dataDict)
            
        return (hits, matches.get_matches_estimated())
 
    def log_error(self, msg):
        log = open("/tmp/slow_queries.txt", "a")
        if log:
            log.write(msg)
            log.flush()
            log.close()
 
    def search(self, query, maxHits, offset, type='xml'):
        if maxHits < 1: maxHits = MAX_HITS
        (hits, total) = self.queryIndex(query, offset, maxHits);
        redirect = ""
        if total == 1:
           doc = hits[0]
           redirect = doc.get('trid')
           if not redirect: redirect = doc.get('reid')
           if not redirect: redirect = doc.get('arid')

        if type == 'html':
            # This comment will be used by the MB server to determine the number of hits returned
            stats = u"<!--\nhits=%d\noffset=%d\n" % (total, offset)
            if redirect: stats += u"redirect=%s\n" % redirect
            stats += u"-->"
            return stats.encode('utf-8') + self.asHTML(hits, maxHits, offset).encode('utf-8')
        else:
            return self.asXML(hits, maxHits, offset).encode('utf-8')
