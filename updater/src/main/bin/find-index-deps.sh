BASE=../../../../search-server-index/src/main/java/org/musicbrainz/search/index
grep -E "(FROM|JOIN)+" $BASE/$1Index.java | sed -e 's/^.*\(FROM\|JOIN\) \([a-z_]*\)$/\2/' #| sort -u
grep -E "(FROM|JOIN)+" $BASE/$1Index.java | sed -e 's/^.*\(FROM\|JOIN\) \([a-z_]*\).*$/\2/' | sort | uniq -c \
	| grep -vE '(artist_name|label_name|track_name|release_name|work_name)' \
	| grep -vE '(track_raw|release_raw|cdtoc_raw|replication_control)' \
	| grep -vE '(artist_type|label_type|country|gender|language|script|medium_format|release_group_type|work_type|release_status)' \
	| grep -vE '(artist_credit|replication_control)'
