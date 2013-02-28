package org.musicbrainz.search.servlet;

import java.util.concurrent.Callable;

// Used by doAllSearch()
class CallableSearch implements Callable<Results>
{

    private final SearchServer searchServer;
    private final String query;
    private final Integer offset;
    private final Integer limit;

    public CallableSearch(SearchServer searchServer, String query, Integer offset, Integer limit)
    {
        this.searchServer = searchServer;
        this.query = query;
        this.offset = offset;
        this.limit = limit;
    }

    @Override
    public Results call() throws Exception
    {
        return searchServer.search(query, offset, limit);
    }

}
