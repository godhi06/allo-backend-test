package com.allobank.idrrate.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SpreadConfig Unit Tests")
class SpreadConfigTest {

    @Test
    @DisplayName("Should calculate correct spread factor for 'ardhi'")
    void shouldCalculateSpreadFactorForArdhi() {
        // a=97, r=114, d=100, h=104, i=105 => sum=520
        // Spread Factor = (520 % 1000) / 100000.0 = 0.00520
        SpreadConfig config = new SpreadConfig("ardhi");

        assertEquals("ardhi", config.getGithubUsername());
        assertEquals(0.00520, config.getSpreadFactor(), 0.00001);
    }

    @Test
    @DisplayName("Should convert username to lowercase before calculating")
    void shouldConvertToLowercase() {
        SpreadConfig configUpper = new SpreadConfig("ARDHI");
        SpreadConfig configLower = new SpreadConfig("ardhi");

        assertEquals(configLower.getSpreadFactor(), configUpper.getSpreadFactor(), 0.00001);
    }

    @Test
    @DisplayName("Spread factor should be between 0.00000 and 0.00999")
    void spreadFactorShouldBeInRange() {
        SpreadConfig config = new SpreadConfig("ardhi");
        double factor = config.getSpreadFactor();

        assertTrue(factor >= 0.0 && factor < 0.01,
                "Spread factor should be between 0.00000 and 0.00999, but was: " + factor);
    }
}
