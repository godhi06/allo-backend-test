package com.allobank.idrrate.runner;

import com.allobank.idrrate.store.InMemoryDataStore;
import com.allobank.idrrate.strategy.IDRDataFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ApplicationRunner that fetches all IDR-related data from the Frankfurter API
 * exactly once on application startup and loads it into the in-memory store.
 *
 * This approach is preferred over @PostConstruct because:
 * 1. It runs after the full application context is ready (all beans wired).
 * 2. It provides access to ApplicationArguments for potential runtime configuration.
 * 3. It guarantees the Spring context (including FactoryBean-created WebClient) is fully initialized.
 */
@Component
public class DataLoaderRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoaderRunner.class);

    private final List<IDRDataFetcher> fetchers;
    private final InMemoryDataStore dataStore;

    public DataLoaderRunner(List<IDRDataFetcher> fetchers, InMemoryDataStore dataStore) {
        this.fetchers = fetchers;
        this.dataStore = dataStore;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("=== Starting data ingestion from Frankfurter API ===");

        int successCount = 0;
        int failureCount = 0;

        for (IDRDataFetcher fetcher : fetchers) {
            try {
                log.info("Fetching resource: {}", fetcher.getResourceType());
                Object data = fetcher.fetchData();
                dataStore.put(fetcher.getResourceType(), data);
                log.info("Successfully loaded resource: {}", fetcher.getResourceType());
                successCount++;
            } catch (Exception e) {
                log.warn("⚠️  Failed to fetch resource '{}': {}. Application will continue with cached/empty data.", 
                        fetcher.getResourceType(), e.getMessage());
                failureCount++;
            }
        }

        dataStore.seal();
        log.info("=== Data ingestion complete. {}/{} resources loaded successfully. {} failed. Store sealed. ===", 
                successCount, fetchers.size(), failureCount);
    }
}
