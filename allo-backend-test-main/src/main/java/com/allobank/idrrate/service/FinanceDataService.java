package com.allobank.idrrate.service;

import com.allobank.idrrate.exception.ResourceNotFoundException;
import com.allobank.idrrate.store.InMemoryDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Service layer responsible for retrieving pre-loaded finance data
 * from the in-memory store.
 *
 * The strategy selection (map-based lookup) is handled at the data loading phase.
 * This service simply serves the cached, immutable data from the store.
 */
@Service
public class FinanceDataService {

    private static final Logger log = LoggerFactory.getLogger(FinanceDataService.class);

    private static final Set<String> VALID_RESOURCE_TYPES = Set.of(
            "latest_idr_rates",
            "historical_idr_usd",
            "supported_currencies"
    );

    private final InMemoryDataStore dataStore;

    public FinanceDataService(InMemoryDataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Retrieves data for the given resource type from the in-memory store.
     *
     * @param resourceType the requested resource type
     * @return the pre-loaded data object
     * @throws ResourceNotFoundException if the resource type is not valid or data not found
     */
    public Object getData(String resourceType) {
        if (!VALID_RESOURCE_TYPES.contains(resourceType)) {
            throw new ResourceNotFoundException(
                    "Unsupported resource type: '" + resourceType + "'. " +
                    "Valid types are: " + VALID_RESOURCE_TYPES);
        }

        Object data = dataStore.get(resourceType);
        if (data == null) {
            throw new ResourceNotFoundException(
                    "No data found for resource type: '" + resourceType + "'");
        }

        log.debug("Serving data for resource type: {}", resourceType);
        return data;
    }
}
