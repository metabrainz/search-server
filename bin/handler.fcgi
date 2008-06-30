#!/usr/bin/env python

from flup.server.fcgi_fork import WSGIServer
from search_app import search_app

WSGIServer(search_app, bindAddress = '/tmp/mbsearch.fcgi.sock').run() 
