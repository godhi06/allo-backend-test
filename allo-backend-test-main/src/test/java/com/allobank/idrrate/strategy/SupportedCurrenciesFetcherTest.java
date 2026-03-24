package com.allobank.idrrate.strategy;

import com.allobank.idrrate.exception.ExternalApiException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SupportedCurrenciesFetcher Unit Tests")
class SupportedCurrenciesFetcherTest {

    private MockWebServer mockWebServer;
    private SupportedCurrenciesFetcher fetcher;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        fetcher = new SupportedCurrenciesFetcher(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("Should return correct resource type")
    void shouldReturnCorrectResourceType() {
        assertEquals("supported_currencies", fetcher.getResourceType());
    }

    @Test
    @DisplayName("Should fetch and return supported currencies map")
    @SuppressWarnings("unchecked")
    void shouldFetchAndReturnCurrencies() {
        String jsonResponse = """
                {
                    "AUD": "Australian Dollar",
                    "BGN": "Bulgarian Lev",
                    "BRL": "Brazilian Real",
                    "IDR": "Indonesian Rupiah",
                    "USD": "United States Dollar"
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        Object result = fetcher.fetchData();

        assertNotNull(result);
        assertInstanceOf(Map.class, result);

        Map<String, String> currencies = (Map<String, String>) result;
        assertEquals(5, currencies.size());
        assertEquals("Indonesian Rupiah", currencies.get("IDR"));
        assertEquals("United States Dollar", currencies.get("USD"));
    }

    @Test
    @DisplayName("Should throw ExternalApiException on server error")
    void shouldThrowExceptionOnServerError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        assertThrows(ExternalApiException.class, () -> fetcher.fetchData());
    }

    @Test
    @DisplayName("Should throw ExternalApiException on 404 response")
    void shouldThrowExceptionOn404() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("Not Found"));

        assertThrows(ExternalApiException.class, () -> fetcher.fetchData());
    }
}
