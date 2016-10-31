#!/bin/bash

if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root" 1>&2
   exit 1
fi

#docker volume create --driver local --name search-indexer-data
# later add: --detach
HOSTNAME=`hostname`
source /root/docker-server-configs/scripts/constants.sh
docker run \
    --detach \
    --hostname $HOSTNAME \
    --restart unless-stopped \
    --publish 62001:8080 \
    --env DEPLOY_ENV="prod" \
    --env SERVICE_8080_NAME="search-server" \
    --env SERVICE_8080_CHECK_TCP="true" \
    --env SERVICE_8080_CHECK_INTERVAL="15s" \
    --env SERVICE_8080_CHECK_TIMEOUT="3s" \
    --env PRIVATE_IP="$PRIVATE_IP" \
    searchserver_server
