package com.allobank.idrrate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * DTO for historical IDR to USD data.
 */
public record HistoricalIdrUsdData(
        @JsonProperty("base") String base,
        @JsonProperty("target") String target,
        @JsonProperty("start_date") String startDate,
        @JsonProperty("end_date") String endDate,
        @JsonProperty("rates") Map<String, Map<String, Double>> rates
) {}
