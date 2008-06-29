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

oddeven = ['even', 'odd']

TYPE_MAPPING =  (u'album', u'single', u'ep', u'compilation', u'soundtrack', u'spokenword',
                 u'interview', u'audiobook', u'live', u'remix', u'other')

def replaceType(m):
   try:
        return u"type:" + TYPE_MAPPING[int(m.group(1)) - 1]
   except IndexError:
        return u""

class TrackSearch(search.TextSearch):
   '''
   This class derives from TextSearch and outputs HTML for lucene query hits
   '''

   def __init__(self, index):
       search.TextSearch.__init__(self, index)
       self.setDefaultField('track')
       self.setPrefixes(('artist', 'arid', 'reid', 'trid', 'release', 'track', 'tnum', 'tracks', 'dur', 'qdur', 'type'))

   def mangleQuery(self, query):
       query = re.sub("type:(\d+)", replaceType, query)
       return query

   def asHTML(self, hits, count, offset):
       '''
       Output an release search result as HTML
       '''

       rel = self.rel
       
       out = u'<div><table class="searchresults" id="TagLookupTrackResults">'
       out += u'<tr class="searchresultsheader"><td>Score</td><td>Artist</td>'
       out += u'<td>Release</td><td>Track</td><td>Num</td><td>Length</td><td>Tracks</td><td>Type</td>'
       if self.tport or self.mbt: 
           out += u"<td>Tagger</td>"
       elif rel: 
           out += u"<td>Rel</td>"
       out += u"</tr>"
       for i, doc in enumerate(hits):
           artist = doc.get('artist') or u''
           arid = doc.get('arid') or u'';
           reid = doc.get('reid') or u'';
           trid = doc.get('trid') or u'';
           album = doc.get('release') or u''
           track = doc.get('track') or u''
           tnum = doc.get('tnum') or u''
           tracks = int(doc.get('tracks') or u'0')
           dur = int(doc.get('dur') or u'0')
           type = doc.get('type') or u'';

           out += u'<tr class="searchresults%s">' % search.oddeven[i % 2]
           out += u"<td>%d</td>" % doc['_score']
           out += u'<td><span class="linkartist-icon"><a href=\"/artist/%s.html\">%s</a></span></td>' % \
                  (self.escape(arid), self.escape(artist))
           out += u'<td><span class="linkrelease-icon"><a href=\"/album/%s.html\">%s</a></span></td>' % \
                  (self.escape(reid), self.escape(album))
           out += u'<td><span class="linktrack-icon"><a href=\"/track/%s.html\">%s</a></span></td>' % \
                  (self.escape(trid), self.escape(track))
           out += u'<td align="center">%s</td>' % self.escape(tnum)
           out += u'<td align="center" class="tlen %s">' % self.getTrackLenClass(dur)
           if dur:
               out += u'%d:%02d' % (dur / 60000, (dur % 60000) / 1000)
           else:
               out += u"&nbsp;" 
           out += u"</td>"
           out += u'<td align="center">%d</td>' % tracks
           out += u"<td>%s</td>" % self.escape(type)

           if self.tport or self.mbt:
               out += u'<td style="white-space: nowrap">'
               if self.tport: out += self.taggerLink(self.tport, reid)
               if self.mbt: out += self.mbtLink(trid, reid)
               out += u"</td>"
           elif rel: out += u"<td><a href=\"/show/track/relationships.html?trackid=%s&amp;addrel=1\">rel</a></td>" % self.escape(trid)
           out += u"</tr>"
       out += u"</table></div>"
       return out

   def asXML(self, hits, count, offset):
       '''
       Output an release search result as XML
       '''

       out = '<track-list count="%d" offset="%d">' % (count, offset)
       for doc in hits:
           artist = doc.get('artist') or u''
           sortname = doc.get('sortname') or u''
           arid = doc.get('arid') or u''
           album = doc.get('release') or u''
           alid = doc.get('reid') or u''
           track = doc.get('track') or u''
           tracks = int(doc.get('tracks') or u'0')
           tnum = int(doc.get('tnum') or u'0')
           dur = doc.get('dur') or u''

           out += u'<track id="%s"' % self.escape(doc.get('trid'))
           out += u' ext:score="%d">' % doc['_score']
           out += u"<title>%s</title>" % self.escape(track)
           if dur: out += u"<duration>%s</duration>" % self.escape(dur)
           out += u'<artist id="%s"><name>%s</name>' % (self.escape(arid), self.escape(artist))
           out += u'<sortname>%s</sortname></artist>' % (self.escape(sortname))
           out += u'<release-list><release id="%s"><title>%s</title>' % \
                  (self.escape(alid), self.escape(album))
           if tnum:
               if tracks:
                   out += u'<track-list offset="%d" count="%d"/>' % (tnum - 1, tracks)
               else:
                   out += u'<track-list offset="%d"/>' % (tnum - 1)
           out += u'</release></release-list>'
           out += u"</track>"
       out += u"</track-list>"
       return out

if __name__ == "__main__":
    s = TrackSearch(sys.argv[1])
    print s.search(sys.argv[2], 10, 'track') 
