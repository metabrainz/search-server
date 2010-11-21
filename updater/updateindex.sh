#!/bin/bash

MAIN_DB_HOST=localhost
INDEXES_DIR=../index/data
SERVLET_HOST=localhost:8080

LIVE_UPDATE_REPO=http://test.musicbrainz.org:82/pub/musicbrainz/data/replication/

SEARCH_UPDATER_JAR=target/updater-2.0-SNAPSHOT-jar-with-dependencies.jar
LOCK_FILE=/tmp/.mb-search-updater

if [ -e $LOCK_FILE ]; then
    echo "Index updater is already running, existing..."
    exit
fi

if [ ! -e $SEARCH_UPDATER_JAR ]; then
    echo "JAR '$SEARCH_UPDATER_JAR' not found..."
    echo "You should maybe run 'mvn package'..."
    exit
fi

touch $LOCK_FILE
java -Xmx512M -jar $SEARCH_UPDATER_JAR --db-host $MAIN_DB_HOST --replication-repository $LIVE_UPDATE_REPO --indexes-dir $INDEXES_DIR "$@"
	
if [ -n "$SERVLET_HOST" ] ; then
    wget --quiet --spider http://$SERVLET_HOST/?reload
fi
rm $LOCK_FILE
