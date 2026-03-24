package com.allobank.idrrate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * DTO for the latest IDR rates response with the additional USD_BuySpread_IDR field.
 */
public record LatestIdrRatesData(
        @JsonProperty("base") String base,
        @JsonProperty("date") String date,
        @JsonProperty("rates") Map<String, Double> rates,
        @JsonProperty("USD_BuySpread_IDR") double usdBuySpreadIdr,
        @JsonProperty("spread_factor") double spreadFactor,
        @JsonProperty("github_username") String githubUsername
) {}
