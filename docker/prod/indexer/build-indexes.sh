#!/bin/sh

# This script expects the following evn vars to be set
#      POSTGRES_HOST
#      POSTGRES_PORT
#      POSTGRES_DB
#      POSTGRES_USER
#      POSTGRES_PASSWD

SEARCH_HOME=/home/search
SEARCH_JAVA_OPTS_INDEXER="-Xmx512M"
JAR=/home/search/index.jar

echo $JAVA_HOME/bin/java \
	$SEARCH_JAVA_OPTS_INDEXER \
	-jar "$JAR" \
	--db-host "$POSTGRES_HOST" \
	--db-name "$POSTGRES_DB" \
	--db-user "$POSTGRES_USER" \
	--db-password "$POSTGRES_PASSWD" \
	--indexes "$@"
exec $JAVA_HOME/bin/java \
	$SEARCH_JAVA_OPTS_INDEXER \
	-jar "$JAR" -t \
	--db-host "$POSTGRES_HOST" \
	--db-name "$POSTGRES_DB" \
	--db-user "$POSTGRES_USER" \
	--db-password "$POSTGRES_PASSWD" \
	--indexes "$@"
