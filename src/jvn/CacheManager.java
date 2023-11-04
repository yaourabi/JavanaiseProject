package jvn;

import java.util.LinkedHashMap;
import java.util.Map;

public class CacheManager<K, V> extends LinkedHashMap<K, V> {
    private static final int MAX_CACHE_SIZE = 100; // Maximum number of items in the cache

    // loadFactor: the map will be resized when it's 75% full
    // if accessOrder is true, the most recently accessed elements are moved to the end of the map
    public CacheManager() {
        super(MAX_CACHE_SIZE, 0.75f, true); // true for access order, false for insertion order
    }
    // removeEldestEntry removes old mappings automatically when new mappings are added to the map.
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > MAX_CACHE_SIZE;
        // size() is provided by LinkedHashMap
    }
}
