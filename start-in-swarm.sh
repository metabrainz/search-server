#!/bin/bash

INDEXER_DATA=./indexer-data

# Build the containers
docker build -f Dockerfile-server .
docker build -f Dockerfile-indexer .

# IMPORTANT: The number of max allowable files needs to be increased. Production servers have a value of 51200.

# create the search overlay network
COUNT=`docker network ls | grep " search " | wc -l`
if [[ $COUNT == "0" ]]; then
    echo "creating search network..."
    docker network create --driver overlay search
else
    echo "search network exists"
fi

mkdir -p $INDEXER_DATA

# start the indexer
docker service create \
    --name search_indexer \
    --network search \
    -p 873:873 \
    --mount target=/home/search/data,source=`pwd`/$INDEXER_DATA,type=bind \
    --restart-condition any \
    -e SEARCH_HOME=/home/search \
    -e INDEXES_VERSION=1 \
    -e POSTGRES_HOST=10.0.2.15 \
    -e POSTGRES_PORT=5432 \
    -e POSTGRES_DB=musicbrainz_db \
    -e POSTGRES_USER=musicbrainz \
    -e POSTGRES_PASSWD=musicbrainz \
    searchserver_indexer

# start the server
docker service create \
    --name search_server \
    --network search \
    -p 8080:8080 \
    --restart-condition any \
    -e SEARCH_HOME=/home/search \
    -e INDEXES_VERSION=1 \
    -e RSYNC_SERVER=search_indexer \
    -e RSYNC_PASSWORD=search \
    searchserver_server
