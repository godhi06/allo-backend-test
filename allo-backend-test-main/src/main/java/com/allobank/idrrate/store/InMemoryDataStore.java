package com.allobank.idrrate.store;

import com.allobank.idrrate.exception.DataNotLoadedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thread-safe, immutable in-memory store for pre-fetched IDR data.
 * 
 * Data is loaded once at application startup via the DataLoaderRunner
 * and becomes immutable (read-only) after that point.
 * 
 * Uses a ConcurrentHashMap for thread-safe reads and an AtomicBoolean
 * to track the loaded state, ensuring safe concurrent access from
 * multiple request threads.
 */
@Component
public class InMemoryDataStore {

    private static final Logger log = LoggerFactory.getLogger(InMemoryDataStore.class);

    private final ConcurrentHashMap<String, Object> dataMap = new ConcurrentHashMap<>();
    private final AtomicBoolean loaded = new AtomicBoolean(false);

    /**
     * Stores data for a specific resource type.
     * Should only be called during application startup by DataLoaderRunner.
     *
     * @param resourceType the resource type key
     * @param data the fetched and transformed data
     */
    public void put(String resourceType, Object data) {
        if (loaded.get()) {
            throw new IllegalStateException("Data store is already sealed. Cannot modify after initialization.");
        }
        dataMap.put(resourceType, data);
        log.debug("Stored data for resource type: {}", resourceType);
    }

    /**
     * Seals the data store, making it immutable.
     * Called after all data has been loaded during startup.
     */
    public void seal() {
        loaded.set(true);
        log.info("In-memory data store sealed with {} resource types: {}", dataMap.size(), dataMap.keySet());
    }

    /**
     * Retrieves data for a specific resource type from the store.
     *
     * @param resourceType the resource type key
     * @return the stored data
     * @throws DataNotLoadedException if the store has not been sealed yet
     */
    public Object get(String resourceType) {
        if (!loaded.get()) {
            throw new DataNotLoadedException("Data has not been loaded yet. Please wait for application startup to complete.");
        }
        return dataMap.get(resourceType);
    }

    /**
     * Returns an unmodifiable view of all stored data.
     *
     * @return unmodifiable map of resource type to data
     * @throws DataNotLoadedException if the store has not been sealed yet
     */
    public Map<String, Object> getAll() {
        if (!loaded.get()) {
            throw new DataNotLoadedException("Data has not been loaded yet. Please wait for application startup to complete.");
        }
        return Collections.unmodifiableMap(dataMap);
    }

    /**
     * Checks whether the data store has been loaded and sealed.
     *
     * @return true if data is loaded and ready to serve
     */
    public boolean isLoaded() {
        return loaded.get();
    }

    /**
     * Returns the number of resource types stored.
     */
    public int size() {
        return dataMap.size();
    }
}
