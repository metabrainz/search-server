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

TYPE_MAPPING =  (u'album', u'single', u'ep', u'compilation', u'soundtrack', u'spokenword',
                 u'interview', u'audiobook', u'live', u'remix', u'other')

def replaceType(m):
   try:
        return u"type:" + TYPE_MAPPING[int(m.group(1)) - 1]
   except IndexError:
        return u""

class ReleaseGroupSearch(search.TextSearch):
   '''
   This class derives from TextSearch and outputs HTML for lucene query hits
   '''

   def __init__(self, index):
       search.TextSearch.__init__(self, index)
       self.setDefaultField('releasegroup')
       self.useMultiFields(False)

   def mangleQuery(self, query):
       query = re.sub("type:(\d+)", replaceType, query)
       return query

   def asHTML(self, hits, maxHits, offset):
       '''
       Output an release search result as HTML
       '''

       rel = self.rel

       out = u'<div><table class="searchresults">'
       out += u'<tr class="searchresultsheader"><td>Score</td><td>Release Group</td><td>Artist</td>'
       out += u'<td>Type</td></tr>'
       for i in xrange(offset, min(hits.length(), maxHits + offset)):
           doc = hits.doc(i)

           rgid = doc.get('rgid') or u''
           releasegroup = doc.get('releasegroup') or u''
           arid = doc.get('arid') or u''
           artist = doc.get('artist') or u''
           type = doc.get('type') or u''

           out += u'<tr class="searchresults%s">' % self.escape(search.oddeven[i % 2])
           out += u"<td>%d</td>" % int(hits.score(i) * 100)
           out += u"<td><a href=\"/release-group/%s.html\">%s</a></td>" % \
                  (self.escape(rgid), self.escape(releasegroup))
           out += u"<td><a href=\"/artist/%s.html\">%s</a></td>" % \
                  (self.escape(arid), self.escape(artist))
           out += u'<td align="center">%s</td>' % (type)
           out += u"</tr>"
       out += u"</table></div>"
       return out

   def asXML(self, hits, maxHits, offset):
       '''
       Output an release search result as XML
       '''

       out = '<release-group-list count="%d" offset="%d">' % (hits.length(), offset)
       for i in xrange(offset, min(hits.length(), maxHits + offset)):
           doc = hits.doc(i)

           rgid = doc.get('rgid') or u'';
           releasegroup = doc.get('releasegroup') or u'';
           arid = doc.get('arid') or u'';
           artist = doc.get('artist') or u'';
           type = doc.get('type') or u'';

           out += u'<release-group id="%s"' % self.escapeAttr(rgid)
           if type: out += u' type="%s"' % self.escape(type.title())
           out += u' ext:score="%d"' % int(hits.score(i) * 100)
           out += u'><title>%s</title>' % self.escape(releasegroup)

           out += u'<artist id="%s"><name>' % self.escapeAttr(arid)
           out += u'%s</name></artist>' % self.escape(artist)
           out += u'</release-group>'
       out += u"</release-group-list>"
       return out

if __name__ == "__main__":
    s = ReleaseGroupSearch(sys.argv[1])
    print s.search(sys.argv[2], 10, 'release') 
