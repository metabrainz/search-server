#!/bin/sh

#docker volume create --driver local --name search-indexer-data
# later add: --detach
HOSTNAME=`hostname`
docker run \
    --hostname $HOSTNAME \
    --restart unless-stopped \
    --publish 62000:873 \
    --volume search-indexes:/home/search/data \
    --env DEPLOY_ENV="prod" \
    --env SERVICE_873_NAME="search-indexer" \
    --env SERVICE_873_CHECK_TCP="true" \
    --env SERVICE_873_CHECK_INTERVAL="15s" \
    --env SERVICE_873_CHECK_TIMEOUT="3s" \
    searchserver_indexer
