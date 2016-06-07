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
	
    @Option(name="--replication-repository", usage="The base path of replication packets. (default: https://metabrainz.org/api/musicbrainz/)")
    private String repositoryPath = "https://metabrainz.org/api/musicbrainz/";
    public String getRepositoryPath() { return repositoryPath; }

    @Option(name="--replication-access-token", usage="Access token obtained after signing up at https://metabrainz.org")
    private String accessToken = "";
    public String getAccessToken() { return accessToken; }

    @Option(name="--verbose", usage="More verbosity")
    private boolean verbose = false;
    public boolean isVerbose() { return verbose; }
	
}
