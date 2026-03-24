package com.allobank.idrrate.controller;

import com.allobank.idrrate.dto.ApiResponse;
import com.allobank.idrrate.service.FinanceDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for serving aggregated finance data.
 *
 * Exposes a single endpoint that dynamically resolves the requested resource type.
 * No if/else or switch logic is used — the strategy pattern handles resource dispatch
 * at the data loading level, and this controller simply serves cached data.
 */
@RestController
@RequestMapping("/api/finance")
public class FinanceController {

    private static final Logger log = LoggerFactory.getLogger(FinanceController.class);

    private final FinanceDataService financeDataService;

    public FinanceController(FinanceDataService financeDataService) {
        this.financeDataService = financeDataService;
    }

    /**
     * GET /api/finance/data/{resourceType}
     *
     * Returns pre-loaded data for the requested resource type.
     * Valid resource types: latest_idr_rates, historical_idr_usd, supported_currencies
     *
     * @param resourceType the type of resource to retrieve
     * @return unified API response with the requested data
     */
    @GetMapping("/data/{resourceType}")
    public ResponseEntity<ApiResponse> getData(@PathVariable String resourceType) {
        log.info("Received request for resource type: {}", resourceType);
        Object data = financeDataService.getData(resourceType);
        return ResponseEntity.ok(ApiResponse.success(resourceType, data));
    }
}
