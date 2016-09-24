#!/usr/bin/env python

import sys
import os
import subprocess
import shutil
import time
import errno

def main():
    try:
        search_home = os.environ['SEARCH_HOME']
    except AttributeError:
        print "Environment var SEARCH_HOME must be set for this script."
        sys.exit(-1)
    
    try:
        indexes_version = os.environ['INDEXES_VERSION']
    except AttributeError:
        print "Environment var INDEXES_VERSION must be set for this script."
        sys.exit(-1)
    
    in_prog_dir = os.path.join(search_home, "data", "in-progress")
    indexes_dir = os.path.join(search_home, "data")
    
    try:
        subprocess.check_call(["rsync", "--config=/etc/rsyncd.conf", "--daemon"])
    except subprocess.CalledProcessError as e:
        print "Cannot start rsync daemon: " + str(e)
        sys.exit(-2)
    
    while True:
        try:
            os.makedirs(in_prog_dir)
        except OSError, e:
            if e.errno != errno.EEXIST:
                print "Failed to create in-progress dir %s: %s" % (in_progress_dir, e)
                sys.exit(-1)
    
        os.chdir(in_prog_dir)
    
        print os.path.join(search_home, "bin", "build-indexes.sh")
        try:
            subprocess.check_call([os.path.join(search_home, "bin", "build-indexes.sh"), 
                "area,artist,cdstub,instrument,label,place,editor,event,release,releasegroup,cdstub,annotation,series,work,tag,url"])
        except OSError as e:
            print "Cannot build indexes: " + str(e)
            sys.exit(-3)
        except subprocess.CalledProcessError as e:
            print "Cannot build indexes: " + str(e)
            sys.exit(-3)
    
        os.chdir(search_home)
        rotate(indexes_version, os.path.join(in_prog_dir, "data"), indexes_dir)

def rotate(version, new_set, indexes):
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

if __name__ == "__main__":
    main()
