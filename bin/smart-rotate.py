#!/usr/bin/env python

import sys
import os
import shutil
import time
import errno

if len(sys.argv) < 4:
    print "Usage: smart-rotate.py <version> <new> <indexes dir>"
    sys.exit(-1)

version = sys.argv[1]
new_set = sys.argv[2]
indexes = sys.argv[3]
ts = int(time.time())
dest = os.path.join(indexes, version)

try:
    os.makedirs(dest)
except OSError, e:
    if e.errno != errno.EEXIST:
        print "Failed to create dest dir %s: %s" % (dest, e)
        sys.exit(-1)

dest = os.path.join(dest, str(ts))
try:
    os.rename(new_set, dest)
except OSError, e:
    print "Failed to move new set to indexes dir %s: %s" % (dest, e)
    sys.exit(-1)

indexes_dir = os.path.join(indexes, version)

# Remove older data sets
dirs = [ f for f in os.listdir(indexes_dir) if os.path.isdir(os.path.join(indexes_dir,f)) ]
dirs.sort(reverse=True)
for dir in dirs[2:]:
    index_dir = os.path.join(indexes_dir, dir)
    print "Remove old %s" % index_dir
    shutil.rmtree(index_dir)

sys.exit(0)

    # if an index exits in the new path and the current path, save it in old
#    if os.path.exists(os.path.join(new, dir)) and os.path.exists(os.path.join(cur, dir)):
#        try:
#            shutil.rmtree(os.path.join(old, dir))
#            print "remove %s" % os.path.join(old, dir)
#        except OSError, e:
#            if e.errno != 2:
#                raise
#
#        try:
#            shutil.move(os.path.join(cur, dir), os.path.join(old, dir))
#            print "backup %s -> %s" % (os.path.join(cur, dir), os.path.join(old, dir))
#        except shutil.Error:
#            print "Cannot backup %s -> %s: %s" % (os.path.join(cur, dir), os.path.join(old, dir), e)
#            sys.exit(-1)
#
#    try:
#        shutil.move(os.path.join(new, dir), os.path.join(cur, dir))
#        print "move %s -> %s" % (os.path.join(new, dir), os.path.join(cur, dir))
#    except shutil.Error:
#        print "Cannot move %s -> %s: %s" % (os.path.join(new, dir), os.path.join(cur, dir))
#        sys.exit(-1)

