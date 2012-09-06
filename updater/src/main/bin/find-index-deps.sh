BASE=../../../../search-server-index/src/main/java/org/musicbrainz/search/index
ENTITY=$1
grep -E "(FROM|JOIN)+" $BASE/${ENTITY}Index.java | sed -e 's/^.*\(FROM\|JOIN\)\s\+\([a-z_]*\)$/\2/' #| sort -u
grep -E "(FROM|JOIN)+" $BASE/${ENTITY}Index.java | sed -e 's/^.*\(FROM\|JOIN\)\s\+\([a-z_]*\).*$/\2/' | sort | uniq -c \
	| grep -viE "\b${ENTITY}\b" \
	| grep -viE '\b(artist_name|label_name|track_name|release_name|work_name)\b' \
	| grep -viE '\b(track_raw|release_raw|cdtoc_raw)\b' \
	| grep -viE '\b(artist_type|label_type|release_group_primary_type|release_group_secondary_type|work_type)\b' \
	| grep -viE '\b(country|gender|language|script|medium_format|release_status)\b' \
	| grep -viE '\b(link|link_type)\b' \
	| grep -viE '\b(puid|tag)\b' \
	| grep -viE '\b(replication_control)\b'
