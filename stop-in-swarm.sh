#!/bin/bash

docker service rm search_indexer 
docker service rm search_server 

# Removing the overlay network each time causes problems. Seems ok to keep it around
# docker network rm search

exit 0
