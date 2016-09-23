#!/bin/bash

DATA_DIR=/home/search/data
SEARCH_SYNC_PORT=7000

while [ 1 ]
do
    echo "Waiting to receive new indexes"
    mkdir -p $DATA_DIR/new
    nc -dl $SEARCH_SYNC_PORT | tar -x -C $DATA_DIR/new
    echo "Received new indexes"
done
