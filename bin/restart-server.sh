#!/bin/sh
set -e

SEARCH_SYNC_PORT=7000

HOST="$1"
echo "push RELOAD file"

cd /tmp
touch RELOAD
tar cvO RELOAD | nc $HOST $SEARCH_SYNC_PORT
rm RELOAD
cd -

exit 0
