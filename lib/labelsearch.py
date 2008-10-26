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

TYPE_MAPPING = {
    u'unknown'        : u'Unknown', 
    u'distributor'    : u'Distributor', 
    u'holding'        : u'Holding', 
    u'production'     : u'Production', 
    u'orig. prod.'    : u'OriginalProduction', 
    u'reissue prod.'  : u'ReissueProduction', 
    u'publisher'      : u'Publisher',
    u'bootleg prod.'  : u'BootlegProduction',
}
TYPE_NUM_MAPPING = ( 
    u'unknown', u'distributor', u'holding', u'production', u'orig. prod.', 
    u'reissue prod.', u'publisher', u'bootleg prod.',
)

def replaceType(m):
   try:
        return u"type:" + TYPE_NUM_MAPPING[int(m.group(1))]
   except IndexError:
        return u""

class LabelSearch(search.TextSearch):

   '''
   This class derives from TextSearch and outputs HTML for lucene query hits
   '''

   def __init__(self, index):
       search.TextSearch.__init__(self, index)
       self.setDefaultField('label')
       self.setPrefixes(('label', 'sortname', 'alias', 'begin', 'end', 'type', 'laid', 'comment', 'code'))

   def mangleQuery(self, query):
       query = re.sub("type:(\d)", replaceType, query)
       return query

   def asHTML(self, hits, count, offset):
       '''
       Output a label search result as HTML
       '''
       
       rel = self.rel
       
       out = u'<div><table class="searchresults">'
       out += u'<tr class="searchresultsheader"><td>Score</td><td>Label</td><td>Sortname</td><td>Code</td><td>Type</td><td>Begin</td><td>End</td>'
       if rel: out += u'<td>Rel</td>'
       out += u'</tr>'
       
       for i, doc in enumerate(hits):
           label = doc.get('label') or u''
           sortname = doc.get('sortname') or u''
           comment = doc.get('comment') or u''
           code = doc.get('code') or u''
           begin = doc.get('begin') or u''
           end = doc.get('end') or u''
           laid = doc.get('laid') or u''
           type = doc.get('type') or u''

           out += u'<tr class="searchresults%s">' % search.oddeven[i % 2]
           out += u"<td>%d</td>" % doc['_score']
           out += u"<td><span class=\"linklabel-icon\"><a href=\"/label/%s.html\">%s</a></span>" % \
                   (self.escape(laid), self.escape(label))
           if comment: out += " (%s)" % self.escape(comment)
           out += u"</td><td>%s</td>" % self.escape(sortname)
           out += u"<td>%s</td>" % self.escape(code)
           out += u"<td>%s</td>" % self.escape(type)
           out += u"<td>%s</td><td>%s</td>" % (self.escape(begin), self.escape(end))
           if rel: out += u"<td><a href=\"/show/artist/relationships.html?labelid=%s&amp;addrel=1\">rel</a></td>" % self.escape(laid)
           out += u"</tr>"
       out += u"</table></div>"
       return out

   def asXML(self, hits, count, offset):
       '''
       Output an artist search result as XML
       '''

       out = '<label-list count="%d" offset="%d">' % (count, offset)
       for i, doc in enumerate(hits):
           label = doc.get('label') or u''
           sortname = doc.get('sortname') or u''
           type = doc.get('type') or u''
           code = doc.get('code') or u''
           try:
               type = TYPE_MAPPING[type]
           except KeyError:
               pass

           begin = doc.get('begin') or u''
           end = doc.get('end') or u''
           comment = doc.get('comment') or u''

           out += u'<label id="%s"' % self.escape(doc.get('laid'))
           if type: out += u' type="%s"' % type
           out += u' ext:score="%d"' % doc['_score']
           out += u'><name>%s</name>' % self.escape(label)
           if sortname:
               out += u"<sort-name>%s</sort-name>" % self.escape(sortname)
           if code:
               out += u"<label-code>%s</label-code>" % self.escape(code)
           if begin or end:
               out += u'<life-span'
               if begin: out += u' begin="%s"' % self.escape(begin)
               if end: out += u' end="%s"' % self.escape(end)
               out += u'/>'
           if comment:
               out += u"<disambiguation>%s</disambiguation>" % self.escape(comment)
           out += u"</label>"
       out += u"</label-list>"
       return out

if __name__ == "__main__":
    s = ArtistSearch(sys.argv[1])
    print s.search(sys.argv[2], 10, 'artist') 
