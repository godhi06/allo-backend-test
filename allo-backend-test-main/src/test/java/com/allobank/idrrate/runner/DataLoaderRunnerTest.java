package com.allobank.idrrate.runner;

import com.allobank.idrrate.store.InMemoryDataStore;
import com.allobank.idrrate.strategy.IDRDataFetcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataLoaderRunner Unit Tests")
class DataLoaderRunnerTest {

    @Test
    @DisplayName("Should load all fetchers and seal data store")
    void shouldLoadAllFetchersAndSealStore() {
        // Setup mock fetchers
        IDRDataFetcher fetcher1 = mock(IDRDataFetcher.class);
        when(fetcher1.getResourceType()).thenReturn("latest_idr_rates");
        when(fetcher1.fetchData()).thenReturn(Map.of("base", "IDR"));

        IDRDataFetcher fetcher2 = mock(IDRDataFetcher.class);
        when(fetcher2.getResourceType()).thenReturn("historical_idr_usd");
        when(fetcher2.fetchData()).thenReturn(Map.of("base", "IDR"));

        IDRDataFetcher fetcher3 = mock(IDRDataFetcher.class);
        when(fetcher3.getResourceType()).thenReturn("supported_currencies");
        when(fetcher3.fetchData()).thenReturn(Map.of("IDR", "Indonesian Rupiah"));

        InMemoryDataStore dataStore = new InMemoryDataStore();
        DataLoaderRunner runner = new DataLoaderRunner(List.of(fetcher1, fetcher2, fetcher3), dataStore);

        ApplicationArguments args = mock(ApplicationArguments.class);
        runner.run(args);

        // Verify all fetchers were called
        verify(fetcher1).fetchData();
        verify(fetcher2).fetchData();
        verify(fetcher3).fetchData();

        // Verify store is sealed and has data
        assertTrue(dataStore.isLoaded());
        assertEquals(3, dataStore.size());
        assertNotNull(dataStore.get("latest_idr_rates"));
        assertNotNull(dataStore.get("historical_idr_usd"));
        assertNotNull(dataStore.get("supported_currencies"));
    }

    @Test
    @DisplayName("Should throw RuntimeException when a fetcher fails")
    void shouldThrowExceptionWhenFetcherFails() {
        IDRDataFetcher failingFetcher = mock(IDRDataFetcher.class);
        when(failingFetcher.getResourceType()).thenReturn("latest_idr_rates");
        when(failingFetcher.fetchData()).thenThrow(new RuntimeException("API down"));

        InMemoryDataStore dataStore = new InMemoryDataStore();
        DataLoaderRunner runner = new DataLoaderRunner(List.of(failingFetcher), dataStore);

        ApplicationArguments args = mock(ApplicationArguments.class);
        assertThrows(RuntimeException.class, () -> runner.run(args));
        assertFalse(dataStore.isLoaded());
    }
}
