package com.psnm.mcp.meetingroom.config;

import com.psnm.mcp.meetingroom.client.BackendApiClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(BackendApiProperties.class)
public class BackendApiConfig {

    @Bean
    public RestClient restClient(BackendApiProperties properties) {
        // Configure timeout settings properly
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        
        // Parse and set connect timeout from properties
        if (properties.getConnectTimeout() != null && !properties.getConnectTimeout().isEmpty()) {
            try {
                Duration connectTimeout = Duration.parse(properties.getConnectTimeout());
                factory.setConnectTimeout((int) connectTimeout.toMillis());
            } catch (Exception e) {
                // Use default if parsing fails
                factory.setConnectTimeout(5000); // 5 seconds default
            }
        } else {
            factory.setConnectTimeout(5000); // 5 seconds default
        }
        
        // Parse and set read timeout from properties  
        if (properties.getReadTimeout() != null && !properties.getReadTimeout().isEmpty()) {
            try {
                Duration readTimeout = Duration.parse(properties.getReadTimeout());
                factory.setReadTimeout((int) readTimeout.toMillis());
            } catch (Exception e) {
                // Use default if parsing fails
                factory.setReadTimeout(15000); // 15 seconds default
            }
        } else {
            factory.setReadTimeout(15000); // 15 seconds default
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