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

class CDStubSearch(search.TextSearch):
   '''
   This class derives from TextSearch and outputs HTML for lucene query hits
   '''

   def __init__(self, index):
       search.TextSearch.__init__(self, index)
       self.useMultiFields(True)
       self.setDefaultMultiFields(['artist', 'title'])

   def asHTML(self, hits, maxHits, offset):
       '''
       Output an release search result as HTML
       '''

       rel = self.rel
       out = u'<div><table class="searchresults">'
       out += u'<tr class="searchresultsheader"><td>Score</td><td>CD</td><td>Artist</td><td>Tracks</td></tr>'
       for i in xrange(offset, min(hits.length(), maxHits + offset)):
           doc = hits.doc(i)

           title = doc.get('title') or u''
           artist = doc.get('artist') or u''
           discid = doc.get('discid') or u''
           tracks = doc.get('tracks') or u'0'

           out += u'<tr class="searchresults%s">' % self.escape(search.oddeven[i % 2])
           out += u"<td>%d</td>" % int(hits.score(i) * 100)
           out += u"<td><a href=\"/show/cdstub/index.html?discid=%s\">%s</a></td>" % (self.escape(discid), self.escape(title))
           out += u"<td>%s</td>" % self.escape(artist)
           out += u'<td>%s</td>' % (self.escape(tracks))
           out += u"</tr>"
       out += u"</table></div>"
       return out

   def asXML(self, hits, maxHits, offset):
       '''
       Output an release search result as XML
       '''

       return ""

if __name__ == "__main__":
    s = CDStubSearch(sys.argv[1])
    print s.search(sys.argv[2], 10, 'release') 
