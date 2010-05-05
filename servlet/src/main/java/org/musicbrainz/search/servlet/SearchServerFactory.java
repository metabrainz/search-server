package org.musicbrainz.search.servlet;

import java.util.EnumMap;


public class SearchServerFactory {

    static private EnumMap<ResourceType, SearchServer> search = new EnumMap<ResourceType, SearchServer>(ResourceType.class);

    public static void init(String indexDir,boolean useMMapDirectory) throws Exception {
        search.put(ResourceType.ARTIST, new ArtistSearch(indexDir, useMMapDirectory));
        search.put(ResourceType.LABEL, new LabelSearch(indexDir, useMMapDirectory));
        search.put(ResourceType.RELEASE, new ReleaseSearch(indexDir, useMMapDirectory));
        search.put(ResourceType.RELEASE_GROUP, new ReleaseGroupSearch(indexDir, useMMapDirectory));
        search.put(ResourceType.RECORDING, new RecordingSearch(indexDir, useMMapDirectory));
        search.put(ResourceType.WORK, new WorkSearch(indexDir, useMMapDirectory));
        search.put(ResourceType.ANNOTATION, new AnnotationSearch(indexDir, useMMapDirectory));
        search.put(ResourceType.FREEDB, new FreeDBSearch(indexDir, useMMapDirectory));
        search.put(ResourceType.CDSTUB, new CDStubSearch(indexDir, useMMapDirectory));
        search.put(ResourceType.TAG, new TagSearch(indexDir, useMMapDirectory));
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
