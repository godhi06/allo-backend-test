package com.allobank.idrrate.strategy;

import com.allobank.idrrate.config.SpreadConfig;
import com.allobank.idrrate.dto.FrankfurterLatestResponse;
import com.allobank.idrrate.dto.LatestIdrRatesData;
import com.allobank.idrrate.exception.ExternalApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Strategy implementation for fetching the latest IDR exchange rates.
 * 
 * This strategy fetches /latest?base=IDR from the Frankfurter API
 * and calculates the USD_BuySpread_IDR using a personalized spread factor 
 * derived from the GitHub username.
 */
@Component
public class LatestIdrRatesFetcher implements IDRDataFetcher {

    private static final Logger log = LoggerFactory.getLogger(LatestIdrRatesFetcher.class);
    private static final String RESOURCE_TYPE = "latest_idr_rates";

    private final WebClient webClient;
    private final SpreadConfig spreadConfig;

    public LatestIdrRatesFetcher(WebClient webClient, SpreadConfig spreadConfig) {
        this.webClient = webClient;
        this.spreadConfig = spreadConfig;
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
                log.info("Fetching latest IDR rates from Frankfurter API (attempt {}/{})...", attempt, maxRetries);

                FrankfurterLatestResponse response = webClient.get()
                        .uri("/latest?base=IDR")
                        .retrieve()
                        .bodyToMono(FrankfurterLatestResponse.class)
                        .block();

                if (response == null || response.rates() == null) {
                    throw new ExternalApiException("Received null response from Frankfurter API for latest IDR rates");
                }

                Double rateUsd = response.rates().get("USD");
                if (rateUsd == null) {
                    throw new ExternalApiException("USD rate not found in latest IDR rates response");
                }

                double spreadFactor = spreadConfig.getSpreadFactor();
                double usdBuySpreadIdr = (1.0 / rateUsd) * (1.0 + spreadFactor);

                log.info("✓ Calculated USD_BuySpread_IDR: {} (spread factor: {}, username: {})",
                        usdBuySpreadIdr, spreadFactor, spreadConfig.getGithubUsername());

                return new LatestIdrRatesData(
                        response.base(),
                        response.date(),
                        response.rates(),
                        usdBuySpreadIdr,
                        spreadFactor,
                        spreadConfig.getGithubUsername()
                );

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

        throw new ExternalApiException("Failed to fetch latest IDR rates after " + maxRetries + " attempts");
    }
}
