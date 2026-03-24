package com.allobank.idrrate.strategy;

import com.allobank.idrrate.config.SpreadConfig;
import com.allobank.idrrate.dto.LatestIdrRatesData;
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

@DisplayName("LatestIdrRatesFetcher Unit Tests")
class LatestIdrRatesFetcherTest {

    private MockWebServer mockWebServer;
    private LatestIdrRatesFetcher fetcher;
    private SpreadConfig spreadConfig;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        // Username "godhi06": g=103, o=111, d=100, h=104, i=105, 0=48, 6=54 => sum=625
        // Spread Factor = (625 % 1000) / 100000.0 = 0.00625
        spreadConfig = new SpreadConfig("godhi06");
        fetcher = new LatestIdrRatesFetcher(webClient, spreadConfig);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("Should return correct resource type")
    void shouldReturnCorrectResourceType() {
        assertEquals("latest_idr_rates", fetcher.getResourceType());
    }

    @Test
    @DisplayName("Should fetch and transform latest IDR rates with correct spread calculation")
    void shouldFetchAndTransformLatestIdrRates() {
        String jsonResponse = """
                {
                    "amount": 1.0,
                    "base": "IDR",
                    "date": "2024-12-20",
                    "rates": {
                        "USD": 0.0000625,
                        "EUR": 0.0000590,
                        "GBP": 0.0000498
                    }
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        Object result = fetcher.fetchData();

        assertNotNull(result);
        assertInstanceOf(LatestIdrRatesData.class, result);

        LatestIdrRatesData data = (LatestIdrRatesData) result;
        assertEquals("IDR", data.base());
        assertEquals("2024-12-20", data.date());
        assertEquals(3, data.rates().size());
        assertEquals(0.0000625, data.rates().get("USD"));

        // Verify spread calculation:
        // USD_BuySpread_IDR = (1 / 0.0000625) * (1 + 0.00625) = 16000 * 1.00625 = 16100.0
        double expectedSpread = (1.0 / 0.0000625) * (1.0 + 0.00625);
        assertEquals(expectedSpread, data.usdBuySpreadIdr(), 0.01);
        assertEquals(0.00625, data.spreadFactor(), 0.00001);
        assertEquals("godhi06", data.githubUsername());
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

    @Test
    @DisplayName("Should throw ExternalApiException when USD rate is missing")
    void shouldThrowExceptionWhenUsdRateMissing() {
        String jsonResponse = """
                {
                    "amount": 1.0,
                    "base": "IDR",
                    "date": "2024-12-20",
                    "rates": {
                        "EUR": 0.0000590,
                        "GBP": 0.0000498
                    }
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        assertThrows(ExternalApiException.class, () -> fetcher.fetchData());
    }
}
