#!/usr/bin/env python

from flup.server.fcgi_fork import WSGIServer; 
from cgi import FieldStorage;
import time
import sys

sys.path.append("../lib")

import labelsearch
import artistsearch
import releasesearch
import tracksearch
import annotationsearch
import freedbsearch

ar_search = None
re_search = None
tr_search = None
an_search = None
fd_search = None
la_search = None

def search(environ, start_response):
    global ar_search
    global re_search
    global tr_search
    global an_search
    global fd_search
    global la_search

    try:
        indexDir = environ['INDEXDIR']
    except KeyError:
        start_response('403 BAD REQUEST', [('Content-Type', 'text/plain')])
        return "INDEXDIR environment variable not set. Search server misconfigured.\n"

    args = FieldStorage(environ=environ)
    query = ""
    maxHits = -1 
    fmt = ''
    type = ''
    tport = 0
    dur = 0
    mbt = 0
    rel = 0
    offset = 0

    # Parse arguments
    query = args.getvalue('query')
    if not query:
        start_response('403 BAD REQUEST', [('Content-Type', 'text/plain')])
        return "query argument is missing"

    maxHits = int(args.getvalue('max', 0))
    fmt = args.getvalue('fmt', 'html')
    tport = int(args.getvalue('tport', 0))
    dur = int(args.getvalue('dur', 0))
    mbt = int(args.getvalue('mbt', 0))
    rel = int(args.getvalue('rel', 0))
    offset = int(args.getvalue('offset', 0))
    type = args.getvalue('type')
    if isinstance(type, list): type = type[0]

    # if we don't have a tagger port, don't color code track lengths
    if not tport: dur = 0

    searchobj = None
    import search
    try:
	if type == 'artist':
	    if not ar_search:
		ar_search = artistsearch.ArtistSearch(indexDir + "/artist_index")
	    searchobj = ar_search

	elif type == 'release':
	    if not re_search:
		re_search = releasesearch.ReleaseSearch(indexDir + "/release_index")
	    re_search.setTaggerPort(tport)
	    re_search.setDuration(dur)
	    searchobj = re_search

	elif type == 'track':
	    if not tr_search:
		tr_search = tracksearch.TrackSearch(indexDir + "/track_index")
	    tr_search.setMBT(mbt)
	    tr_search.setTaggerPort(tport)
	    tr_search.setDuration(dur)
	    searchobj = tr_search
	
	elif type == 'annotation':
	    if not an_search:
		an_search = annotationsearch.AnnotationSearch(indexDir + "/annotation_index")
	    searchobj = an_search

	elif type == 'freedb':
	    if not fd_search:
		fd_search = freedbsearch.FreeDBSearch(indexDir + "/freedb_index")
	    searchobj = fd_search

	elif type == 'label':
	    if not la_search:
		la_search = labelsearch.LabelSearch(indexDir + "/label_index")
	    searchobj = la_search

	else:
	    start_response('403 BAD REQUEST', [('Content-Type', 'text/plain')])
	    return "invalid resource requested. %s must be one of artist/release/track/label/annotation.\n" % type
    except search.NoSuchIndexError, msg:
	start_response('500 INTERNAL SERVER ERROR', [('Content-Type', 'text/plain')])
	return "Cannot find indexes. Server misconfigured: %s\n" % msg

    ret = 0
    content = ""

    searchobj.setShowRelationshipLink(rel);
    try:
        content = searchobj.search(query, maxHits, offset, fmt)
    except search.QueryError, text:
        text = str(text)
        text += "\n"
        start_response('400 BAD REQUEST', [('Content-Type', 'text/plain')])
        return text.encode('utf-8', 'replace')
    except search.SearchError:
        start_response('500 INTERNAL SERVER ERROR', [('Content-Type', 'text/plain')])
        return "internal server error\n"
    except search.NoResultsError:
        start_response('404 NOT FOUND', [('Content-Type', 'text/plain')])
        return "zero search hits\n"
    
    start_response('200 OK', [('Content-Type', 'text/%s' % fmt)])
    return content

#if __name__ == '__main__':
#    from wsgiref import simple_server
#    httpd = simple_server.WSGIServer(('',8080),simple_server.WSGIRequestHandler)
#    httpd.set_app(search)
#    httpd.serve_forever()
#else:
WSGIServer(search, bindAddress = '/tmp/mbsearch.fcgi.sock').run() 
