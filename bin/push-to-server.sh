#!/bin/bash

#!/bin/sh
set -e

SEARCH_SYNC_PORT=7000

HOST="$1"
echo "start copy!"
(cd $2 ; tar cvO .) | nc $HOST $SEARCH_SYNC_PORT
