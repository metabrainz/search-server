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
STATUS_MAPPING = (u'official', u'promotion', u'bootleg', u'pseudo-release')

def replaceType(m):
   try:
        return u"type:" + TYPE_MAPPING[int(m.group(1)) - 1]
   except IndexError:
        return u""

def replaceStatus(m):
   try:
        return u"status:" + STATUS_MAPPING[int(m.group(1)) - 1]
   except IndexError:
        return u""

class ReleaseSearch(search.TextSearch):
   '''
   This class derives from TextSearch and outputs HTML for lucene query hits
   '''

   def __init__(self, index):
       search.TextSearch.__init__(self, index)
       self.setDefaultField('release')
       self.useMultiFields(False)

   def mangleQuery(self, query):
       query = re.sub("type:(\d+)", replaceType, query)
       query = re.sub("status:(\d)", replaceStatus, query)
       query = re.sub("barcode:0+", "barcode:", query)
       return query

   def asHTML(self, hits, maxHits, offset):
       '''
       Output an release search result as HTML
       '''

       rel = self.rel

       out = u'<div><table class="searchresults">'
       out += u'<tr class="searchresultsheader"><td>Score</td><td>Album</td><td>Artist</td><td>Tracks</td>'
       out += u'<td style="white-space: nowrap">CD ids</td><td>Date</td><td>Type</td><td style="white-space: nowrap">Lang/script</td>'
       if self.tport: 
           out += u"<td>Tagger</td>"
       elif rel: 
           out += u"<td>Rel</td>"
       out += u"</tr>"
       for i in xrange(offset, min(hits.length(), maxHits + offset)):
           doc = hits.doc(i)

           artist = doc.get('artist') or u'';
           arid = doc.get('arid') or u'';
           album = doc.get('release') or u'';
           reid = doc.get('reid') or u'';
           type = doc.get('type') or u'';
           tracks = doc.get('tracks') or u'';
           cdids = doc.get('discids') or u'';
           lang = doc.get('lang') or u''
           script = doc.get('script') or u''

           if lang and not script: script = u'?'
           if not lang and script: lang = u'?'

           dates = doc.getFields('date') or [];
	   dates = [d.stringValue() for d in dates]
           countries = doc.getFields('country') or [];
	   countries = [c.stringValue() for c in countries]

           out += u'<tr class="searchresults%s">' % self.escape(search.oddeven[i % 2])
           out += u"<td>%d</td>" % int(hits.score(i) * 100)
           out += u"<td><a href=\"/release/%s.html\">%s</a></td>" % \
                  (self.escape(reid), self.escape(album))
           out += u"<td><a href=\"/artist/%s.html\">%s</a></td>" % \
                  (self.escape(arid), self.escape(artist))
           out += u'<td align="center">%s</td><td align="center">%s</td>' % (self.escape(tracks), self.escape(cdids))
           out += u"<td>"
           for date, country in zip(dates, countries):
               out += "%s %s " % (self.escape(country), self.escape(date))
           out += u"</td>"
           out += u'<td align="center">%s</td>' % (self.escape(type))
           if lang or script: 
               out += u'<td align="center">%s / %s</td>' % (self.escape(lang), self.escape(script).lower())
           else:
               out += u"<td></td>"
           if self.tport: out += u"<td>%s</td>" % self.taggerLink(self.tport, reid)
           elif rel: out += u"<td><a href=\"/show/release/relationships.html?releaseid=%s&amp;addrel=1\">rel</a></td>" % self.escape(reid)
           out += u"</tr>"
       out += u"</table></div>"
       return out

   def asXML(self, hits, maxHits, offset):
       '''
       Output an release search result as XML
       '''

       out = '<release-list count="%d" offset="%d">' % (hits.length(), offset)
       for i in xrange(offset, min(hits.length(), maxHits + offset)):
           doc = hits.doc(i)

           artist = doc.get('artist') or u''
           arid = doc.get('arid') or u''
           album = doc.get('release') or u''
           reid = doc.get('reid') or u''
           type = doc.get('type') or u''
           status = doc.get('status') or u''
           tracks = doc.get('tracks') or u''
           cdids = doc.get('discids') or u''
           asin = doc.get('asin') or u''
           lang = doc.get('lang') or u''
           script = doc.get('script') or u''

           countries = doc.getValues('country')
           dates = doc.getValues('date')
           labels = doc.getValues('label')
           catnos = doc.getValues('catno')
           barcodes = doc.getValues('barcode')

           if status: type = (type + (u" %s" % status)).strip()

           out += u'<release id="%s"' % self.escapeAttr(reid)
           if type: out += u' type="%s"' % self.escapeAttr(type.title())
           out += u' ext:score="%d"' % int(hits.score(i) * 100)
           out += u'><title>%s</title>' % self.escape(album)

           if lang or script:
               out += u'<text-representation'
               if lang: out += u' language="%s"' % self.escapeAttr(lang.upper())
               if script: out += u' script="%s"' % self.escapeAttr(script)
               out += u'/>'
           if asin and asin != u"          ": out += u'<asin>%s</asin>' % self.escape(asin.upper())

           out += u'<artist id="%s"><name>' % self.escapeAttr(arid)
           out += u"%s" % self.escape(artist)
           out += u"</name></artist>"
           if dates and len(dates):
               out += u'<release-event-list>'
               for country, date, label, catno, barcode in zip(countries, dates, labels, catnos, barcodes):
                   out += u'<event date="%s"'  % self.escapeAttr(date.strip())
                   out += u' country="%s"' % self.escapeAttr(country.strip().upper())
                   out += u' label="%s"' % self.escapeAttr(label.strip())
                   out += u' catno="%s"' % self.escapeAttr(catno.strip())
                   out += u' barcode="%s"' % self.escapeAttr(barcode.strip())
                   out += u'/>'
               out += u'</release-event-list>'
           if cdids: out += u'<disc-list count="%s"/>' % self.escapeAttr(cdids)
           if tracks: out += u'<track-list count="%s"/>' % self.escapeAttr(tracks)
           out += u'</release>'
       out += u"</release-list>"
       return out

if __name__ == "__main__":
    s = ReleaseSearch(sys.argv[1])
    print s.search(sys.argv[2], 10, 'release') 
