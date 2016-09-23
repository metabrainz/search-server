#!/bin/bash

SEARCH_HOME=/home/search
WATCH_FILE=${@:1:1}
PROG=${@:2:1}

# Start the main program
$PROG "${@:3}" &

# Start the index receiver
/receive-indexes.sh &

rm -f "$WATCH_FILE"
while [ ! -e "$WATCH_FILE" ]; do
    sleep 1
done
rm -f "$WATCH_FILE"

cd $SEACH_HOME/data
if [[ ! -d "new" ]]; then
    /smart-rotate.py cur new old
fi

# this has got to be my favorite command EVAR
killall -9 java
