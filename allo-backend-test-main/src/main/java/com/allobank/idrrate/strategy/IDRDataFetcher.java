package com.allobank.idrrate.strategy;

/**
 * Strategy interface for fetching and transforming IDR-related data 
 * from the Frankfurter Exchange Rate API.
 * 
 * Each concrete implementation handles a specific resource type:
 * - latest_idr_rates
 * - historical_idr_usd
 * - supported_currencies
 */
public interface IDRDataFetcher {

    /**
     * Returns the resource type key this strategy handles.
     */
    String getResourceType();

    /**
     * Fetches data from the Frankfurter API and returns a transformed result.
     * 
     * @return the transformed data object
     * @throws com.allobank.idrrate.exception.ExternalApiException if the external API call fails
     */
    Object fetchData();
}
