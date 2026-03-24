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
        log.info("Fetching historical IDR to USD data from Frankfurter API...");
        try {
            FrankfurterHistoricalResponse response = webClient.get()
                    .uri("/2024-01-01..2024-01-05?from=IDR&to=USD")
                    .retrieve()
                    .bodyToMono(FrankfurterHistoricalResponse.class)
                    .block();

            if (response == null || response.rates() == null) {
                throw new ExternalApiException("Received null response from Frankfurter API for historical data");
            }

            log.info("Successfully fetched historical IDR/USD data from {} to {}",
                    response.startDate(), response.endDate());

            return new HistoricalIdrUsdData(
                    response.base(),
                    "USD",
                    response.startDate(),
                    response.endDate(),
                    response.rates()
            );

        } catch (WebClientResponseException e) {
            log.error("External API error when fetching historical data: {} {}", e.getStatusCode(), e.getMessage());
            throw new ExternalApiException("Failed to fetch historical IDR/USD data: " + e.getMessage(), e);
        } catch (ExternalApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching historical data: {}", e.getMessage());
            throw new ExternalApiException("Unexpected error fetching historical IDR/USD data: " + e.getMessage(), e);
        }
    }
}
