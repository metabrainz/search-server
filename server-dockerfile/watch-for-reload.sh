#!/bin/bash

WATCH_FILE=${@:1:1}
PROG=${@:2:1}

# Start the main program
$PROG "${@:3}" &

rm -f "$WATCH_FILE"
while [ ! -e "$WATCH_FILE" ]; do
    sleep 1
done
rm -f "$WATCH_FILE"

# this has got to be my favorite command EVAR
killall -9 java
