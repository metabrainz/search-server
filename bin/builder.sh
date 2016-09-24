#!/bin/bash

echo "$0 starting"

SEARCH_HOME=/home/search
# TODO: use command line args for server list
SEARCH_PUSH_TO=$SEARCH_SERVERS
SEARCH_PUSH_TO_DELAY=300

SEARCH_REC_HOUR=1
SEARCH_REC_MAX_HOUR=12
SEARCH_REC_THRESHOLD=$((12*3600))

SEARCH_FREEDB_DAY=2
SEARCH_FREEDB_HOUR=4
SEARCH_FREEDB_MAX_HOUR=12
SEARCH_FREEDB_THRESHOLD=86400 # 24 hours

mkdir -p $SEARCH_HOME/data

while true; do
    echo "SEARCH_HOME=$SEARCH_HOME SEARCH_PUSH_TO=$SEARCH_PUSH_TO"
    cd $SEARCH_HOME || exit 1
    
    REC_TIMESTAMP_FILE="$SEARCH_HOME/recording_build_timestamp"
    
    FREEDB_TIMESTAMP_FILE="$SEARCH_HOME/freedb_build_timestamp"
    
    if [ ! -f $FREEDB_TIMESTAMP_FILE ]; then
    	date +%s > $FREEDB_TIMESTAMP_FILE
    fi
    if [ ! -f $REC_TIMESTAMP_FILE ]; then
    	date +%s > $REC_TIMESTAMP_FILE
    fi
    
    REC_LAST_BUILD=`cat $REC_TIMESTAMP_FILE`
    FREEDB_LAST_BUILD=`cat $FREEDB_TIMESTAMP_FILE`
    NOW=`date +%s`
    DAY=`date +%d`
    HOUR=`date +%H`
    REC_ELAPSED=`expr $NOW - $REC_LAST_BUILD`
    FREEDB_ELAPSED=`expr $NOW - $FREEDB_LAST_BUILD`
    
    echo "clean up old files"
    ! rm -rf data/cur/*
    
    run_and_show_errors() {
    	echo "Running $@"
    	if ! "$@"
    	then
    		RC=$?
    		echo "FAIL '$@' (rc=$RC)"
    		return 1
    	else
    		echo "PASS '$@'"
    		return 0
    	fi
    }
    
    
    if [ "$FREEDB_ELAPSED" -ge "$SEARCH_FREEDB_THRESHOLD" ] && 
       [ "$DAY" -eq "$SEARCH_FREEDB_DAY" ] &&
       [ "$HOUR" -ge "$SEARCH_FREEDB_HOUR" ] &&
       [ "$HOUR" -le "$SEARCH_FREEDB_MAX_HOUR" ]; then
    	echo "Building freedb index. Last index was started $FREEDB_ELAPSED seconds ago"
    	date +%s > $FREEDB_TIMESTAMP_FILE
    	run_and_show_errors /build-indexes.sh freedb
    else
    	echo "Not building freedb index. ($FREEDB_ELAPSED of $SEARCH_FREEDB_THRESHOLD seconds since last freedb build)"
    fi
    
    if [ "$REC_ELAPSED" -ge "$SEARCH_REC_THRESHOLD" ] && [ "$HOUR" -ge "$SEARCH_REC_HOUR" ] && [ "$HOUR" -le "$SEARCH_REC_MAX_HOUR" ]; then
    	echo "Building recording index. Last index was started $REC_ELAPSED seconds ago"
    	START=$(date +%s)
    	run_and_show_errors /build-indexes.sh recording 
        if [ $? -eq 0 ];
    	then
    		echo "$START" > $REC_TIMESTAMP_FILE
    	fi
    else
    	echo "Not building recording index. ($REC_ELAPSED of $SEARCH_REC_THRESHOLD seconds since last recording build)"
    fi
    
    /build-indexes.sh area,artist,releasegroup,release,label,cdstub,tag,work,annotation,place,url,series,editor,event,instrument
    if [ $? -ne 0 ]
    then
        echo "Building indexes failed. Sticking head in sand for a while, hoping problem goes away."
	sleep 5
	continue
    fi
    
    for h in $SEARCH_PUSH_TO
    do
    	run_and_show_errors /push-to-server.sh $h $SEARCH_HOME/data/new/data ; eval "${h}_rc=$?"
    done

    echo "Wait 5 seconds post push."
    sleep 5
    
    for h in $SEARCH_PUSH_TO
    do
    	if eval "[ \$${h}_rc = 0 ]" ; then
    		run_and_show_errors /restart-server.sh $h
    		r=$?
    		eval "${h}_rc=$r"
    		# The restart causes the load on the target server to spike for a few minutes. Delaying
    		# 5 minutes here allows the server to cache the index into ram and return to normal performance.
    		# This prevents us from degading overall search performance every 3 hours.
    		sleep $SEARCH_PUSH_TO_DELAY 
    	fi
    done
    
    # Rotate index files
    echo "saved generated indexes"
    types=("area_index" "artist_index" "releasegroup_index" "release_index" "label_index" "cdstub_index" "tag_index" "work_index" "annotation_index" "place_index" "url_index" "series_index" "editor_index" "instrument_index" "freedb_index" "recording_index" "event_index")
    for type in "${types[@]}"
    do
      if [ -e data/cur/$type ]; then
    	if [ -e data/old/$type ]; then
    		echo "rm old/$type"
    		rm -rf data/old/$type
    	fi
    	echo "mv cur/$type old/$type"
    	mv data/cur/$type data/old/$type
      fi
    done
    
    echo "$0 completed"
    sleep 5
done
    
# eof
