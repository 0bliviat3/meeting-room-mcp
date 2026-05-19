package com.psnm.mcp.meetingroom.config;

import com.psnm.mcp.meetingroom.client.BackendApiClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(BackendApiProperties.class)
public class BackendApiConfig {

    @Bean
    public RestClient restClient(BackendApiProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    @Bean
    public BackendApiClient backendApiClient(RestClient restClient, BackendApiProperties properties) {
        return new BackendApiClient(restClient, properties.getReferer());
    }
}