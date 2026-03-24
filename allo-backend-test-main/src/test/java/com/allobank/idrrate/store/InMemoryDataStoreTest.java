package com.allobank.idrrate.store;

import com.allobank.idrrate.exception.DataNotLoadedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InMemoryDataStore Unit Tests")
class InMemoryDataStoreTest {

    private InMemoryDataStore dataStore;

    @BeforeEach
    void setUp() {
        dataStore = new InMemoryDataStore();
    }

    @Test
    @DisplayName("Should store and retrieve data after sealing")
    void shouldStoreAndRetrieveData() {
        dataStore.put("test_key", "test_value");
        dataStore.seal();

        assertEquals("test_value", dataStore.get("test_key"));
    }

    @Test
    @DisplayName("Should throw DataNotLoadedException when getting data before seal")
    void shouldThrowExceptionWhenNotSealed() {
        dataStore.put("test_key", "test_value");

        assertThrows(DataNotLoadedException.class, () -> dataStore.get("test_key"));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when putting data after seal")
    void shouldThrowExceptionWhenPuttingAfterSeal() {
        dataStore.seal();

        assertThrows(IllegalStateException.class,
                () -> dataStore.put("test_key", "test_value"));
    }

    @Test
    @DisplayName("Should return unmodifiable map from getAll")
    void shouldReturnUnmodifiableMap() {
        dataStore.put("key1", "value1");
        dataStore.put("key2", "value2");
        dataStore.seal();

        Map<String, Object> all = dataStore.getAll();
        assertEquals(2, all.size());
        assertThrows(UnsupportedOperationException.class,
                () -> all.put("key3", "value3"));
    }

    @Test
    @DisplayName("Should return null for non-existent key")
    void shouldReturnNullForNonExistentKey() {
        dataStore.seal();

        assertNull(dataStore.get("non_existent"));
    }

    @Test
    @DisplayName("Should correctly report loaded state")
    void shouldCorrectlyReportLoadedState() {
        assertFalse(dataStore.isLoaded());
        dataStore.seal();
        assertTrue(dataStore.isLoaded());
    }

    @Test
    @DisplayName("Should correctly report size")
    void shouldCorrectlyReportSize() {
        assertEquals(0, dataStore.size());
        dataStore.put("key1", "value1");
        assertEquals(1, dataStore.size());
        dataStore.put("key2", "value2");
        assertEquals(2, dataStore.size());
    }
}
