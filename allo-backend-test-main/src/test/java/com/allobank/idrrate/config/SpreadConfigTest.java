package com.allobank.idrrate.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SpreadConfig Unit Tests")
class SpreadConfigTest {

    @Test
    @DisplayName("Should calculate correct spread factor for 'godhi06'")
    void shouldCalculateSpreadFactorForGodhi06() {
        // g=103, o=111, d=100, h=104, i=105, 0=48, 6=54 => sum=625
        // Spread Factor = (625 % 1000) / 100000.0 = 0.00625
        SpreadConfig config = new SpreadConfig("godhi06");

        assertEquals("godhi06", config.getGithubUsername());
        assertEquals(0.00625, config.getSpreadFactor(), 0.00001);
    }

    @Test
    @DisplayName("Should convert username to lowercase before calculating")
    void shouldConvertToLowercase() {
        SpreadConfig configUpper = new SpreadConfig("GODHI06");
        SpreadConfig configLower = new SpreadConfig("godhi06");

        assertEquals(configLower.getSpreadFactor(), configUpper.getSpreadFactor(), 0.00001);
    }

    @Test
    @DisplayName("Spread factor should be between 0.00000 and 0.00999")
    void spreadFactorShouldBeInRange() {
        SpreadConfig config = new SpreadConfig("godhi06");
        double factor = config.getSpreadFactor();

        assertTrue(factor >= 0.0 && factor < 0.01,
                "Spread factor should be between 0.00000 and 0.00999, but was: " + factor);
    }
}
