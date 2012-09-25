#!/bin/bash

############################################################
#                       SETTINGS                           #
############################################################

source `dirname "$0"`/updateindex.cfg

############################################################
#                       SCRIPT                             #
############################################################

SEARCH_UPDATER_JAR=`dirname "$0"`/target/updater-2.0-SNAPSHOT-jar-with-dependencies.jar

# If lock file is not provided through env variable LOCK_FILE, set it to a default value
if [ -z "$LOCK_FILE" ]; then
    LOCK_FILE=/tmp/.mb-search-updater.lock
fi

# Check that thre's no concurrent run
if [ -e $LOCK_FILE ]; then
    echo "Index updater is already running, existing..."
    exit
fi

# Check presence of the updater jar
if [ ! -e $SEARCH_UPDATER_JAR ]; then
    echo "JAR '$SEARCH_UPDATER_JAR' not found..."
    echo "You should maybe run 'mvn package'..."
    exit
fi

touch $LOCK_FILE

# Run the updater: if $INDEXES is set, use for specifying indexes to update
if [ -z "$INDEXES" ]; then
    java -Xmx512M -jar $SEARCH_UPDATER_JAR --db-host $DB_HOST --db-name $DB_NAME --db-user $DB_USER --db-password $DB_PASSWORD --indexes-dir $INDEXES_DIR "$@"
else
    java -Xmx512M -jar $SEARCH_UPDATER_JAR --indexes $INDEXES --db-host $DB_HOST --db-name $DB_NAME --db-user $DB_USER --db-password $DB_PASSWORD --indexes-dir $INDEXES_DIR "$@"
fi

# Notify the search servlet that indexes have changed
if [ -n "$SERVLET_HOST" ] ; then
    wget --quiet --spider $SERVLET_HOST/?reload
fi

rm $LOCK_FILE

