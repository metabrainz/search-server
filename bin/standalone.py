#! /usr/bin/env python
# To use the standalone server, queries must be formatted as:
#
#    http://localhost:8001/ws/1/artist/?query=love&type=artist&fmt=xml
#
#    fmt can be 'xml' or 'html' (default)
#    type must one of artist, release, track, annotation, label or freedb
#
# The environment variable INDEXDIR must also be set to where the index files are kept.

import getopt, sys, os
from wsgiref.simple_server import make_server, demo_app
from search_app import search_app

def usage():
    print "Usage: %s [-p port] [-i indexdir]"
    print 
    print "Run the search_server from the command line. Not for production environments!"
    print 
    print "Option:"
    print "  -p  port number to run on"
    print "  -i  the directory where indexes are kept"
    sys.exit(-1)

# Parse the command line args
opts = None
args = None
try:
    opts, args = getopt.getopt(sys.argv[1:], "v:p:i:")
except:
    usage()

port = 8001
indexDir = None
for key, value in opts:
    if key == "-h": usage()
    if key == "-p": port = int(value)
    if key == "-i": indexDir = value

if not indexDir:
    try:
	indexDir = os.environ["INDEXDIR"]
    except KeyError:
        pass

    if not indexDir:
	print "The index directory needs to be set in the environment",
	print "variable INDEXDIR or using the -i option."
	sys.exit(-1)

# Set the environment variable so that the search_app can find it
os.environ["INDEXDIR"]=indexDir

httpd = make_server('', port, search_app)
print "Serving HTTP on port %d..." % port

httpd.serve_forever()
