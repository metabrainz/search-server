package org.musicbrainz.search.update;

import org.kohsuke.args4j.Option;
import org.musicbrainz.search.index.IndexOptions;

public class LiveDataFeedIndexUpdaterOptions extends IndexOptions {

	private static LiveDataFeedIndexUpdaterOptions instance = null;
	
	private LiveDataFeedIndexUpdaterOptions() {}	
	
	public static LiveDataFeedIndexUpdaterOptions getInstance() {
		if (instance == null) {
			instance = new LiveDataFeedIndexUpdaterOptions();
		} 
		return instance;
	}
	
    @Option(name="--replication-repository", usage="The base path of replication packets. (default: http://ftp.musicbrainz.org/pub/musicbrainz/data/replication/)")
    private String repositoryPath = "http://ftp.musicbrainz.org/pub/musicbrainz/data/replication/";
    public String getRepositoryPath() { return repositoryPath; }

    @Option(name="--verbose", usage="More verbosity")
    private boolean verbose = false;
    public boolean isVerbose() { return verbose; }
	
}
