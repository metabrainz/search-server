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
import search

TYPE_MAPPING = (u'unknown', u'person', 'group')

def replaceType(m):
   try:
        return u"type:" + TYPE_MAPPING[int(m.group(1))]
   except IndexError:
        return u""

class ArtistSearch(search.TextSearch):

   '''
   This class derives from TextSearch and outputs HTML for lucene query hits
   '''

   def __init__(self, index):
       search.TextSearch.__init__(self, index)
       self.setDefaultField('artist')
       self.setPrefixes(('artist', 'sortname', 'alias', 'begin', 'end', 'type', 'arid', 'comment'))

   def mangleQuery(self, query):
       query = query.replace("artype", "type")
       query = re.sub("type:(\d)", replaceType, query)
       return query

   def asHTML(self, hits, count, offset):
       '''
       Output an artist search result as HTML
       '''
       
       rel = self.rel
       
       out = u'<div><table class="searchresults">'
       out += u'<tr class="searchresultsheader"><td>Score</td><td>Artist</td><td>Sortname</td><td>Type</td><td>Begin</td><td>End</td>'
       if rel: out += u'<td>Rel</td>'
       out += u'</tr>'
       
       for i, doc in enumerate(hits):
           artist = doc.get('artist') or u''
           sortname = doc.get('sortname') or u''
           comment = doc.get('comment') or u''
           begin = doc.get('begin') or u''
           end = doc.get('end') or u''
           arid = doc.get('arid')
           artype = doc.get('type') or u''

           out += u'<tr class="searchresults%s">' % search.oddeven[i % 2]
           out += u"<td>%d</td>" % doc['_score']
           out += u"<td><span class=\"linkartist-icon\"><a href=\"/artist/%s.html\">%s</a></span>" % \
                   (self.escape(arid), self.escape(artist))
           if comment: out += " (%s)" % self.escape(comment)
           out += u"</td><td>%s</td>" % self.escape(sortname)
           out += u"<td>%s</td>" % self.escape(artype)
           out += u"<td>%s</td><td>%s</td>" % (self.escape(begin), self.escape(end))
           if rel: out += u"<td><a href=\"/show/artist/relationships.html?artistid=%s&amp;addrel=1\">rel</a></td>" % self.escape(arid)
           out += u"</tr>"
       out += u"</table></div>"
       return out

   def asXML(self, hits, count, offset):
       '''
       Output an artist search result as XML
       '''

       out = '<artist-list count="%d" offset="%d">' % (count, offset)
       for i, doc in enumerate(hits):
           artist = doc.get('artist') or u''
           sortname = doc.get('sortname') or u''
           artype = doc.get('type') or u''
           begin = doc.get('begin') or u''
           end = doc.get('end') or u''
           comment = doc.get('comment') or u''

           out += u'<artist id="%s"' % self.escape(doc.get('arid'))
           if artype: out += u' type="%s"' % artype.title()
           out += u' ext:score="%d"' % doc['_score']
           out += u'><name>%s</name>' % self.escape(artist)
           if sortname:
               out += u"<sort-name>%s</sort-name>" % self.escape(sortname)
           if begin or end:
               out += u'<life-span'
               if begin: out += u' begin="%s"' % self.escape(begin)
               if end: out += u' end="%s"' % self.escape(end)
               out += u'/>'
           if comment:
               out += u"<disambiguation>%s</disambiguation>" % self.escape(comment)
           out += u"</artist>"
       out += u"</artist-list>"
       return out

if __name__ == "__main__":
    s = ArtistSearch(sys.argv[1])
    print s.search(sys.argv[2], 10, 'artist') 
