#!/bin/sh

#docker volume create --driver local --name search-indexer-data
# later add: --detach
HOSTNAME=`hostname`
docker run \
    --hostname $HOSTNAME \
    --restart unless-stopped \
    --publish 62001:8080 \
    --env DEPLOY_ENV="prod" \
    --env SERVICE_8080_NAME="search-server" \
    --env SERVICE_8080_CHECK_TCP="true" \
    --env SERVICE_8080_CHECK_INTERVAL="15s" \
    --env SERVICE_8080_CHECK_TIMEOUT="3s" \
    searchserver_server
