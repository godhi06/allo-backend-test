package com.allobank.idrrate.controller;

import com.allobank.idrrate.exception.DataNotLoadedException;
import com.allobank.idrrate.exception.GlobalExceptionHandler;
import com.allobank.idrrate.exception.ResourceNotFoundException;
import com.allobank.idrrate.service.FinanceDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FinanceController Unit Tests")
class FinanceControllerTest {

    @Mock
    private FinanceDataService financeDataService;

    @InjectMocks
    private FinanceController financeController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(financeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should return 200 with data for valid resource type")
    void shouldReturn200ForValidResourceType() throws Exception {
        Map<String, String> currencies = Map.of("IDR", "Indonesian Rupiah");
        when(financeDataService.getData("supported_currencies")).thenReturn(currencies);

        mockMvc.perform(get("/api/finance/data/supported_currencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceType").value("supported_currencies"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.IDR").value("Indonesian Rupiah"));

        verify(financeDataService, times(1)).getData("supported_currencies");
    }

    @Test
    @DisplayName("Should return 404 for unsupported resource type")
    void shouldReturn404ForUnsupportedResourceType() throws Exception {
        when(financeDataService.getData("invalid_type"))
                .thenThrow(new ResourceNotFoundException("Unsupported resource type: 'invalid_type'"));

        mockMvc.perform(get("/api/finance/data/invalid_type"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should return 503 when data not loaded yet")
    void shouldReturn503WhenDataNotLoaded() throws Exception {
        when(financeDataService.getData("latest_idr_rates"))
                .thenThrow(new DataNotLoadedException("Data has not been loaded yet."));

        mockMvc.perform(get("/api/finance/data/latest_idr_rates"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.success").value(false));
    }
}
