package com.allobank.idrrate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * DTO representing the response from Frankfurter /latest endpoint.
 */
public record FrankfurterLatestResponse(
        @JsonProperty("amount") double amount,
        @JsonProperty("base") String base,
        @JsonProperty("date") String date,
        @JsonProperty("rates") Map<String, Double> rates
) {}
