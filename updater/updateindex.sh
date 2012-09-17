#!/bin/bash

############################################################
#                       SETTINGS                           #
############################################################

# Local slave database settings
DB_HOST=localhost
DB_NAME=musicbrainz
DB_USER=musicbrainz
DB_PASSWORD=musicbrainz

# Location of search indexes
INDEXES_DIR=/home/search/indexdata

# URL of the search server that need to be notified once indexes has been updated
#SERVLET_HOST=localhost:8080

# Indexes that will be updated
INDEXES=artist,releasegroup,label,tag,annotation,work,release,recording

############################################################
#                       SCRIPT                             #
############################################################

SEARCH_UPDATER_JAR=`dirname "$0"`/target/updater-2.0-SNAPSHOT-jar-with-dependencies.jar
LOCK_FILE=/tmp/.mb-search-updater.lock

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
java -Xmx512M -jar $SEARCH_UPDATER_JAR --indexes $INDEXES --db-host $DB_HOST --db-name $DB_NAME --db-user $DB_USER --db-password $DB_PASSWORD --indexes-dir $INDEXES_DIR "$@"

if [ -n "$SERVLET_HOST" ] ; then
    wget --quiet --spider http://$SERVLET_HOST/?reload
fi
rm $LOCK_FILE

