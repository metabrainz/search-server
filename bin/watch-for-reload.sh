#!/bin/bash

SEARCH_HOME=/home/search
WATCH_FILE=${@:1:1}
PROG=${@:2:1}

# Start the main program
cd $JETTY_HOME
echo $PROG "${@:3}" 
$PROG "${@:3}" &

# Start the index receiver
/receive-indexes.sh &

while [ 1 ]
do
    rm -f "$WATCH_FILE"
    while [ ! -e "$WATCH_FILE" ]; do
        sleep 1
    done
    rm -f "$WATCH_FILE"
    
    cd $SEARCH_HOME/data
    if [[ -d "new" ]]; then
        /smart-rotate.py cur new old
    fi
    
    # this has got to be my favorite command EVAR
    echo "Kill the search server... \ø/"
    killall -9 java
  
    # Start the main program
    cd $JETTY_HOME
    echo $PROG "${@:3}" 
    $PROG "${@:3}" &
done   
