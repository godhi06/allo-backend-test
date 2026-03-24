package com.allobank.idrrate.strategy;

import com.allobank.idrrate.dto.FrankfurterHistoricalResponse;
import com.allobank.idrrate.dto.HistoricalIdrUsdData;
import com.allobank.idrrate.exception.ExternalApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Strategy implementation for fetching historical IDR to USD exchange rates.
 * 
 * This strategy fetches /2024-01-01..2024-01-05?from=IDR&to=USD from the Frankfurter API.
 */
@Component
public class HistoricalIdrUsdFetcher implements IDRDataFetcher {

    private static final Logger log = LoggerFactory.getLogger(HistoricalIdrUsdFetcher.class);
    private static final String RESOURCE_TYPE = "historical_idr_usd";

    private final WebClient webClient;

    public HistoricalIdrUsdFetcher(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public String getResourceType() {
        return RESOURCE_TYPE;
    }

    @Override
    public Object fetchData() {
        int maxRetries = 3;
        int retryDelay = 2000; // 2 seconds
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("Fetching historical IDR to USD data (attempt {}/{})...", attempt, maxRetries);
                
                FrankfurterHistoricalResponse response = webClient.get()
                        .uri("/2023-12-29..2024-01-05?from=IDR&to=USD")
                        .retrieve()
                        .bodyToMono(FrankfurterHistoricalResponse.class)
                        .block();

                if (response == null || response.rates() == null) {
                    throw new ExternalApiException("Received null response from Frankfurter API for historical data");
                }

                log.info("✓ Successfully fetched historical IDR/USD data from {} to {}",
                        response.startDate(), response.endDate());

                return new HistoricalIdrUsdData(
                        response.base(),
                        "USD",
                        response.startDate(),
                        response.endDate(),
                        response.rates()
                );

            } catch (WebClientResponseException e) {
                log.warn("Attempt {}/{} failed with HTTP {}: {}", attempt, maxRetries, e.getStatusCode(), e.getMessage());
                
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    log.error("All retry attempts exhausted. Final error: {} {}", e.getStatusCode(), e.getMessage());
                    throw new ExternalApiException("Failed to fetch historical IDR/USD data after " + maxRetries + " attempts: " + e.getMessage(), e);
                }
            } catch (ExternalApiException e) {
                if (attempt < maxRetries) {
                    log.warn("Attempt {}/{} failed: {}", attempt, maxRetries, e.getMessage());
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    throw e;
                }
            } catch (Exception e) {
                log.warn("Attempt {}/{} failed with unexpected error: {}", attempt, maxRetries, e.getMessage());
                
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    log.error("All retry attempts exhausted. Unexpected error: {}", e.getMessage());
                    throw new ExternalApiException("Unexpected error fetching historical IDR/USD data after " + maxRetries + " attempts: " + e.getMessage(), e);
                }
            }
        }
        
        throw new ExternalApiException("Failed to fetch historical IDR/USD data after all retry attempts");
    }
}
