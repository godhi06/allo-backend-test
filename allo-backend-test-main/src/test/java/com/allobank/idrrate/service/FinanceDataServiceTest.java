package com.allobank.idrrate.service;

import com.allobank.idrrate.exception.DataNotLoadedException;
import com.allobank.idrrate.exception.ResourceNotFoundException;
import com.allobank.idrrate.store.InMemoryDataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FinanceDataService Unit Tests")
class FinanceDataServiceTest {

    private InMemoryDataStore dataStore;
    private FinanceDataService service;

    @BeforeEach
    void setUp() {
        dataStore = new InMemoryDataStore();
        service = new FinanceDataService(dataStore);
    }

    @Test
    @DisplayName("Should return data for valid resource type")
    void shouldReturnDataForValidResourceType() {
        Map<String, String> testData = Map.of("USD", "United States Dollar");
        dataStore.put("supported_currencies", testData);
        dataStore.seal();

        Object result = service.getData("supported_currencies");
        assertNotNull(result);
        assertEquals(testData, result);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for invalid resource type")
    void shouldThrowExceptionForInvalidResourceType() {
        dataStore.seal();

        assertThrows(ResourceNotFoundException.class,
                () -> service.getData("invalid_type"));
    }

    @Test
    @DisplayName("Should throw DataNotLoadedException when store is not sealed")
    void shouldThrowExceptionWhenStoreNotSealed() {
        assertThrows(DataNotLoadedException.class,
                () -> service.getData("supported_currencies"));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when data not found for valid type")
    void shouldThrowExceptionWhenDataNotFound() {
        dataStore.seal();

        assertThrows(ResourceNotFoundException.class,
                () -> service.getData("latest_idr_rates"));
    }
}
