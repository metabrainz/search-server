#!/usr/bin/env python

import sys
import os
import subprocess
import shutil
import time
import datetime
import errno
import json

SEARCH_SERVER_LIST_FILE = "/code/consul_indexer_config.ini"
SEARCH_SERVER_RESTART_SERVER_DELAY = 900
SEARCH_SERVER_CHILL_TIME_BETWEEN_RUNS = 900

RECORDING_INDEX_MAX_AGE = 60 * 60 * 24


def mkdir_p(path):
    try:
        os.makedirs(path)
    except OSError as e:
        if e.errno == errno.EEXIST and os.path.isdir(path):
            pass
        else:
            raise


def lastest_index_dir(indexes_dir, version, index_type):
    topdir = os.path.join(indexes_dir, version)
    dirs = [f for f in os.listdir(topdir) if os.path.isdir(os.path.join(topdir, f))]
    dirs.sort(reverse=True)
    latest = dirs[0]
    return os.path.join(topdir, latest, "%s_index" % index_type)


def check_age_of_index(indexes_dir, version, index_type):
    try:
        index_dir = lastest_index_dir(indexes_dir, version, index_type)
    except (OSError, IndexError):
        return int(time.time())

    # check to see when the index dir was last modified
    try:
        ts = int(os.path.getmtime(index_dir))
    except OSError:
        return int(time.time())

    return int(time.time()) - ts


def copy_index(indexes_dir, version, index_type, dest):
    try:
        index_dir = lastest_index_dir(indexes_dir, version, index_type)
        mkdir_p(dest)
        subprocess.check_call(["cp", "-rav", index_dir, os.path.join(dest, "%s_index" % index_type)])
    except (IndexError, OSError, subprocess.CalledProcessError) as e:
        print "Cannot copy index from %s to %s: " % (index_dir, dest) + str(e)
        sys.exit(-10)


def read_config_file():
    data = {}
    with open(SEARCH_SERVER_LIST_FILE, "r") as f:
        for line in f.readlines():
            kv = line.strip().split("=")
            k = kv[0]
            if len(kv) == 1:
                v = ""
            else:
                v = kv[1]
            if v.endswith(","):
                v = v[:-1]
            data[k] = v

    return data


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
        sys.exit(-2)

    build_index_bin = os.path.join(search_home, "bin", "build-indexes.sh")

    indexes_dir = os.path.join(search_home, "data")
    in_prog_dir = os.path.join(indexes_dir, "in-progress")

    try:
        subprocess.check_call(["rsync", "--config=/etc/rsyncd.conf", "--daemon"])
    except subprocess.CalledProcessError as e:
        print "Cannot start rsync daemon: " + str(e)
        sys.exit(-3)

    default_index_list = [
        'annotation',
        'area',
        'artist',
        'cdstub',
        'editor',
        'event',
        'instrument',
        'label',
        'place',
        'release',
        'releasegroup',
        'series',
        'tag',
        'url',
        'work',
    ]

    while True:
        try:
            shutil.rmtree(in_prog_dir)
        except OSError as e:
            print "Failed to clean up in-progress dir. ignoring. %s: %s" % (in_prog_dir, e)

        try:
            mkdir_p(in_prog_dir)
        except OSError as e:
            print "Failed to create in-progress dir %s: %s" % (in_prog_dir, e)
            sys.exit(-4)

        os.chdir(in_prog_dir)
        datadir = os.path.join(in_prog_dir, "data")

        index_list = list(default_index_list)
        if check_age_of_index(indexes_dir, indexes_version, "recording") > RECORDING_INDEX_MAX_AGE:
            index_list.append('recording')
        else:
            copy_index(indexes_dir, indexes_version, "recording", datadir)

        config_data = read_config_file()
        os.environ['POSTGRES_HOST'] = config_data['pg_host']
        os.environ['POSTGRES_PORT'] = config_data['pg_port']
        os.environ['POSTGRES_DB'] = config_data['pg_database']
        os.environ['POSTGRES_USER'] = config_data['pg_user']
        os.environ['POSTGRES_PASSWD'] = config_data['pg_passwd']

        try:
            subprocess.check_call([build_index_bin, ",".join(index_list)])
        except OSError as e:
            print "Cannot build indexes: " + str(e)
            sys.exit(-5)
        except subprocess.CalledProcessError as e:
            print "Cannot build indexes: " + str(e)
            sys.exit(-6)

        os.chdir(search_home)

        # re-read the config file, it may have changed since the last call
        config_data = read_config_file()
        delay = 0
        sys.stderr.write("Starting restarts at %s\n" % (datetime.datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d %H:%M:%S.%f')))
        for server in config_data['search-servers'].split(","):
            if not server:
                continue
            restart_file = os.path.join(datadir, "restart-" + server)
            with open(restart_file, "w") as f:
                restart = int(time.time() + delay)
                f.write("%d\n" % restart)
                sys.stderr.write("write restart timestamp %s for %s\n" % (datetime.datetime.fromtimestamp(restart).strftime('%Y-%m-%d %H:%M:%S.%f'), server))
                delay += SEARCH_SERVER_RESTART_SERVER_DELAY

        rotate(indexes_version, datadir, indexes_dir)

        # Wait for search servers to sync and rotate, then start the next run
        sys.stderr.write("%s: Sleep for %d seconds, because I'm lazy and I need a break.\n" % (time.time(), SEARCH_SERVER_CHILL_TIME_BETWEEN_RUNS))
        time.sleep(SEARCH_SERVER_CHILL_TIME_BETWEEN_RUNS)


def rotate(version, new_set, indexes):
    ts = int(time.time())
    dest = os.path.join(indexes, version)
    try:
        mkdir_p(dest)
    except OSError as e:
        print "Failed to create dest dir %s: %s" % (dest, e)
        sys.exit(-7)

    dest = os.path.join(dest, str(ts))
    try:
        os.rename(new_set, dest)
    except OSError as e:
        print "Failed to move new set to indexes dir %s: %s" % (dest, e)
        sys.exit(-8)

    indexes_dir = os.path.join(indexes, version)

    # Remove older data sets
    dirs = [f for f in os.listdir(indexes_dir) if os.path.isdir(os.path.join(indexes_dir, f))]
    dirs.sort(reverse=True)
    for dir in dirs[2:]:
        index_dir = os.path.join(indexes_dir, dir)
        print "Remove old %s" % index_dir
        shutil.rmtree(index_dir)


if __name__ == "__main__":
    main()
