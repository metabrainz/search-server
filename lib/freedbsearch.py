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

import sys, os, re
import PyLucene
import search

class FreeDBSearch(search.TextSearch):

   '''
   This class derives from TextSearch and outputs HTML for lucene query hits
   '''

   def __init__(self, index):
       search.TextSearch.__init__(self, index)
       self.useMultiFields(True)
       self.setDefaultMultiFields(['artist', 'title'])

   def asHTML(self, hits, maxHits, offset):
       '''
       Output a freedb search result as HTML
       '''
       
       out = u'<div><table class="searchresults">'
       out += u'<tr class="searchresultsheader"><td>Score</td><td>Title</td>'
       out += u'<td>Artist</td><td>Tracks</td><td>Discid</td><td>Year</td><td>Action</td></tr>'
       for i in xrange(offset, min(hits.length(), maxHits + offset)):
           doc = hits.doc(i)
           out += u'<tr class="searchresults%s">' % search.oddeven[i % 2]
           out += u"<td>%d</td>" % int(hits.score(i) * 100)
           out += u"<td>%s</td>" % self.escape(doc.get('title'))
           out += u"<td>%s</td>" % self.escape(doc.get('artist'))
           out += u"<td>%s</td>" % self.escape(doc.get('tracks'))
           out += u"<td>%s / " % self.escape(doc.get('cat'))
           out += u"%s</a></td>" % self.escape(doc.get('discid'))
           out += u"<td>%s</td>" % self.escape(doc.get('year'))
           out += u'<td><a href="/freedb/import.html?catid=%s/%s">import</a></td>' % (self.escape(doc.get('cat')), 
                                                                      self.escape(doc.get('discid')))
           out += u"</tr>"
       out += u"</table></div>"
       return out

   def asXML(self, hits, maxHits, offset):
       '''
       Output an freedb search result as XML -- except we don't support that. :-)
       '''

       return u""
