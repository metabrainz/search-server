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

class AnnotationSearch(search.TextSearch):

   '''
   This class derives from TextSearch and outputs HTML for lucene query hits
   '''

   def __init__(self, index):
       search.TextSearch.__init__(self, index)
       self.useMultiFields(False)
       self.setDefaultField('text')
       self.types = ['dummy', 'artist', 'release'];

   def asHTML(self, hits, maxHits, offset):
       '''
       Output an annotation search result as HTML
       '''
       
       out = u'<div><table class="searchresults">'
       out += u'<tr class="searchresultsheader"><td>Score</td><td>Type</td><td>Name</td><td>Annotation</td>'
       out += u'</tr>'
       
       for i in xrange(offset, min(hits.length(), maxHits + offset)):
           doc = hits.doc(i)
           type = doc.get('type')
           text = doc.get('text')
           mbid = doc.get('mbid')
           name = doc.get('name')

           out += u'<tr class="searchresults%s">' % search.oddeven[i % 2]
           out += u"<td>%d</td>" % int(hits.score(i) * 100)
           out += u"<td>%s</td>" % self.escape(type)
           out += u"<td><a href=\"/%s/%s.html\">%s</a></td>" % (self.escape(type), 
                                                               self.escape(mbid), self.escape(name))
           out += u"<td>%%WIKIBEGIN%%%s%%WIKIEND%%</td>" % (self.escape(text)) 
           out += u"</tr>"
       out += u"</table></div>"
       return out

   def asXML(self, hits, maxHits, offset):
       '''
       Output an annotation search result as XML
       '''

       return ""
