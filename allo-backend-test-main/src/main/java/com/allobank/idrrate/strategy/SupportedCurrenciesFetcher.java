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
        log.info("Fetching supported currencies from Frankfurter API...");
        try {
            Map<String, String> currencies = webClient.get()
                    .uri("/currencies")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
                    .block();

            if (currencies == null) {
                throw new ExternalApiException("Received null response from Frankfurter API for currencies");
            }

            log.info("Successfully fetched {} supported currencies", currencies.size());
            return currencies;

        } catch (WebClientResponseException e) {
            log.error("External API error when fetching currencies: {} {}", e.getStatusCode(), e.getMessage());
            throw new ExternalApiException("Failed to fetch supported currencies: " + e.getMessage(), e);
        } catch (ExternalApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching currencies: {}", e.getMessage());
            throw new ExternalApiException("Unexpected error fetching supported currencies: " + e.getMessage(), e);
        }
    }
}
