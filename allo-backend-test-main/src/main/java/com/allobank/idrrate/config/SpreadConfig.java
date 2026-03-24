package com.allobank.idrrate.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class holding spread-related properties.
 */
@Configuration
public class SpreadConfig {

    private final String githubUsername;
    private final double spreadFactor;

    public SpreadConfig(@Value("${spread.github-username}") String githubUsername) {
        this.githubUsername = githubUsername.toLowerCase();
        this.spreadFactor = calculateSpreadFactor(this.githubUsername);
    }

    /**
     * Calculates the spread factor based on the sum of Unicode (ASCII) values
     * of all characters in the GitHub username.
     * 
     * Formula: Spread Factor = (Sum of Unicode Values % 1000) / 100000.0
     */
    private double calculateSpreadFactor(String username) {
        int sum = 0;
        for (char c : username.toCharArray()) {
            sum += (int) c;
        }
        return (sum % 1000) / 100000.0;
    }

    public String getGithubUsername() {
        return githubUsername;
    }

    public double getSpreadFactor() {
        return spreadFactor;
    }
}
