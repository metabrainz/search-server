package org.musicbrainz.search;

import java.util.EnumMap;


public class SearchServerFactory {

    static private EnumMap<ResourceType, SearchServer> search = new EnumMap<ResourceType, SearchServer>(ResourceType.class);

    public static void init(String indexDir) throws Exception {
        search.put(ResourceType.ARTIST, new ArtistSearch(indexDir));
        search.put(ResourceType.LABEL, new LabelSearch(indexDir));
        search.put(ResourceType.RELEASE, new ReleaseSearch(indexDir));
        search.put(ResourceType.RELEASE_GROUP, new ReleaseGroupSearch(indexDir));
        search.put(ResourceType.TRACK, new TrackSearch(indexDir));
        search.put(ResourceType.FREEDB, new FreeDBSearch(indexDir));
    }

    /**
     * Get Search Server
     *
     * @param resourceType
     * @return
     */
    public static SearchServer getSearchServer(ResourceType resourceType) {
        return search.get(resourceType);
    }

    

    public static void close() {
        for (SearchServer searchServer : search.values()) {
            searchServer.close();
        }
        search.clear();

    }

}
