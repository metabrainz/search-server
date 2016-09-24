#!/bin/bash

if [ $SEARCH_HOME == '' ]; then
    echo "SEARCH_HOME is undefined. exiting."
    exit 1
fi
if [ $INDEXES_VERSION == '' ]; then
    echo "INDEXES_VERSION is undefined. exiting."
    exit 1
fi

IN_PROG_DIR=$SEARCH_HOME/data/in-progress
INDEXES_DIR=$SEARCH_HOME/data

#rsync --config=/etc/rsyncd.conf --daemon
rsync --log-file=/dev/stdout --config=/etc/rsyncd.conf --daemon

while [ 1 ] 
do

    mkdir -p $IN_PROG_DIR
    cd $IN_PROG_DIR
    $SEARCH_HOME/bin/build-indexes.sh url,area,release,editor
    cd $SEARCH_HOME
    $SEARCH_HOME/bin/smart-rotate.py $INDEXES_VERSION $IN_PROG_DIR $INDEXES_DIR

done
