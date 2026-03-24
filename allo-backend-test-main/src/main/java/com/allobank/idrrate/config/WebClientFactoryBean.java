package com.allobank.idrrate.config;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * Custom FactoryBean for creating and configuring the WebClient instance.
 * 
 * This approach encapsulates the full lifecycle of WebClient creation,
 * including externalized configuration for base URL and timeouts.
 * Using FactoryBean provides more control over bean instantiation 
 * compared to a simple @Bean method in a @Configuration class.
 */
@Component
public class WebClientFactoryBean implements FactoryBean<WebClient> {

    @Value("${frankfurter.api.base-url}")
    private String baseUrl;

    @Value("${frankfurter.api.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${frankfurter.api.read-timeout-ms:10000}")
    private int readTimeoutMs;

    @Override
    public WebClient getObject() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(readTimeoutMs))
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs);

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public Class<?> getObjectType() {
        return WebClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
