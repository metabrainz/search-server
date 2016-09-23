#!/usr/bin/env python

import sys
import os
import shutil

if len(sys.argv) < 4:
    print "Usage: smart-rotate.py <cur> <new> <old>"
    sys.exit(-1)

cur = sys.argv[1]
new = sys.argv[2]
old = sys.argv[3]

if not os.path.exists(cur):
    try:
        os.mkdir(cur)
    except OSError, e:
        print "Failed to create missing cur dir %s: %s" % (cur, e)
        sys.exit(-1)

if not os.path.exists(old):
    try:
        os.mkdir(old)
    except OSError, e:
        print "Failed to create missing old dir %s: %s" % (old, e)
        sys.exit(-1)

if not os.path.exists(new):
    print "new path %s does not exist!" % new
    sys.exit(-1)

dirs = [ f for f in os.listdir(new) if os.path.isdir(os.path.join(new,f)) ]
for dir in dirs:
    if dir == '.' or dir == '..':
        continue

    # if an index exits in the new path and the current path, save it in old
    if os.path.exists(os.path.join(new, dir)) and os.path.exists(os.path.join(cur, dir)):
        try:
            shutil.rmtree(os.path.join(old, dir))
            print "remove %s" % os.path.join(old, dir)
        except OSError, e:
            if e.errno != 2:
                raise

        try:
            shutil.move(os.path.join(cur, dir), os.path.join(old, dir))
            print "backup %s -> %s" % (os.path.join(cur, dir), os.path.join(old, dir))
        except shutil.Error:
            print "Cannot backup %s -> %s: %s" % (os.path.join(cur, dir), os.path.join(old, dir), e)
            sys.exit(-1)

    try:
        shutil.move(os.path.join(new, dir), os.path.join(cur, dir))
        print "move %s -> %s" % (os.path.join(new, dir), os.path.join(cur, dir))
    except shutil.Error:
        print "Cannot move %s -> %s: %s" % (os.path.join(new, dir), os.path.join(cur, dir))
        sys.exit(-1)

sys.exit(0)
