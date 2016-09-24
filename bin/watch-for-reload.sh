#!/bin/bash

WATCH_FILE=${@:1:1}
PROG=${@:2:1}
CURRENT_TS=0

# Start the main program
cd $JETTY_HOME
echo $PROG "${@:3}" 
$PROG "${@:3}" &


while [ 1 ]
do
    rsync rsync://search@indexer:/data/$INDEXES_VERSION/ --list-only > /tmp/data-list
    if [ $? != "0" ]
    then
	echo "=============== Failed to get list of data sets. sleeping"
	sleep 5
	continue
    fi
    TS=`cat /tmp/data-list | colrm 1 46 | grep -i . | sort -r | head -1`
    echo "timestamp $TS current $CURRENT_TS"
    rm /tmp/data-list

    if [ "$CURRENT_TS" == "$TS" ]
    then
	echo "=============== No new data available."
        sleep 10
        continue
    fi

    # Sync over the indexes
    mkdir -p $HOME_SEARCH/data/$TS
    rsync -rv rsync://search@indexer:/data/$INDEXES_VERSION/$TS $HOME_SEARCH/data/$TS
    if [ $? != "0" ]
    then
	echo "=============== Failure during sync of dataset. Starting over again."
	sleep 5
	continue
    fi

    CURRENT_TS=$TS

    sleep 3

#    rm -f "$WATCH_FILE"
#    while [ ! -e "$WATCH_FILE" ]; do
#        sleep 1
#    done
#    rm -f "$WATCH_FILE"
#    
#    # this has got to be my favorite command EVAR
#    echo "Kill the search server... \ø/"
#    killall -9 java
#  
#    # Start the main program
#    cd $JETTY_HOME
#    echo $PROG "${@:3}" 
#    $PROG "${@:3}" &
done   
