package com.psnm.mcp.meetingroom.config;

import com.psnm.mcp.meetingroom.client.BackendApiClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(BackendApiProperties.class)
public class BackendApiConfig {

    @Bean
    public RestClient restClient(BackendApiProperties properties) {
        Duration connectTimeout = Duration.ofMillis(parseTimeout(properties.getConnectTimeout()));
        Duration readTimeout = Duration.ofMillis(parseTimeout(properties.getReadTimeout()));
        
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(new OkHttp3ClientHttpRequestFactory())
                .build();
    }

    private long parseTimeout(String timeoutStr) {
        if (timeoutStr == null || timeoutStr.isEmpty()) {
            return 5000; // default 5s
        }
        
        // Remove 's' suffix if present
        String trimmed = timeoutStr.trim();
        if (trimmed.endsWith("s")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        
        try {
            return Long.parseLong(trimmed) * 1000; // convert seconds to milliseconds
        } catch (NumberFormatException e) {
            return 5000; // default 5s on parse error
        }
    }

    @Bean
    public BackendApiClient backendApiClient(RestClient restClient, BackendApiProperties properties) {
        return new BackendApiClient(restClient, properties.getReferer());
    }
}