package com.psnm.mcp.meetingroom.config;

import com.psnm.mcp.meetingroom.client.BackendApiClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(BackendApiProperties.class)
public class BackendApiConfig {

    @Bean
    public RestClient restClient(BackendApiProperties properties) {
        OkHttp3ClientHttpRequestFactory factory = new OkHttp3ClientHttpRequestFactory();
        // Configure timeouts from properties
        if (properties.getConnectTimeout() != null && !properties.getConnectTimeout().isEmpty()) {
            Duration connectTimeout = Duration.parse(properties.getConnectTimeout());
            factory.setConnectTimeout((int) connectTimeout.toMillis());
        }
        if (properties.getReadTimeout() != null && !properties.getReadTimeout().isEmpty()) {
            Duration readTimeout = Duration.parse(properties.getReadTimeout());
            factory.setReadTimeout((int) readTimeout.toMillis());
        }
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(factory)
                .build();
    }

    @Bean
    public BackendApiClient backendApiClient(RestClient restClient, BackendApiProperties properties) {
        return new BackendApiClient(restClient, properties.getReferer());
    }
}