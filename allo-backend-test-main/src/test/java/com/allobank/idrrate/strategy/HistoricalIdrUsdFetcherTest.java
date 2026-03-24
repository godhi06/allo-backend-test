package com.allobank.idrrate.strategy;

import com.allobank.idrrate.dto.HistoricalIdrUsdData;
import com.allobank.idrrate.exception.ExternalApiException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HistoricalIdrUsdFetcher Unit Tests")
class HistoricalIdrUsdFetcherTest {

    private MockWebServer mockWebServer;
    private HistoricalIdrUsdFetcher fetcher;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        fetcher = new HistoricalIdrUsdFetcher(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("Should return correct resource type")
    void shouldReturnCorrectResourceType() {
        assertEquals("historical_idr_usd", fetcher.getResourceType());
    }

    @Test
    @DisplayName("Should fetch and transform historical IDR/USD data")
    void shouldFetchAndTransformHistoricalData() {
        String jsonResponse = """
                {
                    "amount": 1.0,
                    "base": "IDR",
                    "start_date": "2024-01-01",
                    "end_date": "2024-01-05",
                    "rates": {
                        "2024-01-02": {"USD": 0.0000645},
                        "2024-01-03": {"USD": 0.0000643},
                        "2024-01-04": {"USD": 0.0000641},
                        "2024-01-05": {"USD": 0.0000640}
                    }
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        Object result = fetcher.fetchData();

        assertNotNull(result);
        assertInstanceOf(HistoricalIdrUsdData.class, result);

        HistoricalIdrUsdData data = (HistoricalIdrUsdData) result;
        assertEquals("IDR", data.base());
        assertEquals("USD", data.target());
        assertEquals("2024-01-01", data.startDate());
        assertEquals("2024-01-05", data.endDate());
        assertNotNull(data.rates());
        assertEquals(4, data.rates().size());
        assertEquals(0.0000645, data.rates().get("2024-01-02").get("USD"));
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
