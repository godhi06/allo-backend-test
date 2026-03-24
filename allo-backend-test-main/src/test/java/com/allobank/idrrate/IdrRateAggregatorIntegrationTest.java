package com.allobank.idrrate;

import com.allobank.idrrate.store.InMemoryDataStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests that verify:
 * 1. The ApplicationRunner successfully loads data on startup
 * 2. The InMemoryDataStore is sealed with all 3 resource types
 * 3. The REST endpoint serves data correctly from the store
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Application Integration Tests")
class IdrRateAggregatorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InMemoryDataStore dataStore;

    @Test
    @DisplayName("Data store should be loaded and sealed after startup")
    void dataStoreShouldBeLoadedAfterStartup() {
        assertTrue(dataStore.isLoaded(), "Data store should be sealed after startup");
        assertEquals(3, dataStore.size(), "Data store should contain 3 resource types");
    }

    @Test
    @DisplayName("Data store should contain all three resource types")
    void dataStoreShouldContainAllResourceTypes() {
        assertNotNull(dataStore.get("latest_idr_rates"), "latest_idr_rates should be loaded");
        assertNotNull(dataStore.get("historical_idr_usd"), "historical_idr_usd should be loaded");
        assertNotNull(dataStore.get("supported_currencies"), "supported_currencies should be loaded");
    }

    @Test
    @DisplayName("GET /api/finance/data/latest_idr_rates should return 200")
    void shouldReturnLatestIdrRates() throws Exception {
        mockMvc.perform(get("/api/finance/data/latest_idr_rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceType").value("latest_idr_rates"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.base").value("IDR"))
                .andExpect(jsonPath("$.data.USD_BuySpread_IDR").isNumber())
                .andExpect(jsonPath("$.data.spread_factor").isNumber())
                .andExpect(jsonPath("$.data.github_username").value("godhi06"));
    }

    @Test
    @DisplayName("GET /api/finance/data/historical_idr_usd should return 200")
    void shouldReturnHistoricalIdrUsd() throws Exception {
        mockMvc.perform(get("/api/finance/data/historical_idr_usd"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceType").value("historical_idr_usd"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.base").value("IDR"))
                .andExpect(jsonPath("$.data.target").value("USD"));
    }

    @Test
    @DisplayName("GET /api/finance/data/supported_currencies should return 200")
    void shouldReturnSupportedCurrencies() throws Exception {
        mockMvc.perform(get("/api/finance/data/supported_currencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceType").value("supported_currencies"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("GET /api/finance/data/invalid should return 404")
    void shouldReturn404ForInvalidResource() throws Exception {
        mockMvc.perform(get("/api/finance/data/invalid_resource"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());
    }
}
