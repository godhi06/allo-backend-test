package com.allobank.idrrate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * DTO representing the response from Frankfurter time-series endpoint.
 */
public record FrankfurterHistoricalResponse(
        @JsonProperty("amount") double amount,
        @JsonProperty("base") String base,
        @JsonProperty("start_date") String startDate,
        @JsonProperty("end_date") String endDate,
        @JsonProperty("rates") Map<String, Map<String, Double>> rates
) {}
