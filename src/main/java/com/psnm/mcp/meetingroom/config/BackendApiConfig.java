package com.psnm.mcp.meetingroom.config;

import com.psnm.mcp.meetingroom.client.BackendApiClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(BackendApiProperties.class)
public class BackendApiConfig {

    @Bean
    public RestClient restClient(BackendApiProperties props) {
        return RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader("Referer", props.getReferer())
                .build();
    }

    @Bean
    public BackendApiClient backendApiClient(RestClient restClient) {
        return new BackendApiClient(restClient);
    }
}