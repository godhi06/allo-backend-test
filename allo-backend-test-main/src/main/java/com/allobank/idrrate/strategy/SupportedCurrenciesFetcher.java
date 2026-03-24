package com.allobank.idrrate.strategy;

import com.allobank.idrrate.exception.ExternalApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

/**
 * Strategy implementation for fetching all supported currencies.
 * 
 * This strategy fetches /currencies from the Frankfurter API
 * and returns the list of supported currency symbols with their names.
 */
@Component
public class SupportedCurrenciesFetcher implements IDRDataFetcher {

    private static final Logger log = LoggerFactory.getLogger(SupportedCurrenciesFetcher.class);
    private static final String RESOURCE_TYPE = "supported_currencies";

    private final WebClient webClient;

    public SupportedCurrenciesFetcher(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public String getResourceType() {
        return RESOURCE_TYPE;
    }

    @Override
    public Object fetchData() {
        int maxRetries = 3;
        int retryDelay = 2000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("Fetching supported currencies from Frankfurter API (attempt {}/{}).", attempt, maxRetries);
                Map<String, String> currencies = webClient.get()
                        .uri("/currencies")
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
                        .block();

                if (currencies == null) {
                    throw new ExternalApiException("Received null response from Frankfurter API for currencies");
                }

                log.info("✓ Successfully fetched {} supported currencies", currencies.size());
                return currencies;

            } catch (WebClientResponseException e) {
                log.warn("Attempt {}/{} failed with HTTP {}: {}", attempt, maxRetries, e.getStatusCode(), e.getMessage());
            } catch (ExternalApiException e) {
                log.warn("Attempt {}/{} failed: {}", attempt, maxRetries, e.getMessage());
            } catch (Exception e) {
                log.warn("Attempt {}/{} failed with unexpected error: {}", attempt, maxRetries, e.getMessage());
            }

            if (attempt < maxRetries) {
                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        throw new ExternalApiException("Failed to fetch supported currencies after " + maxRetries + " attempts");
    }
}
