package com.psnm.mcp.meetingroom.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("backend.api")
public class BackendApiProperties {
    private String baseUrl;
    private String referer;
    private String connectTimeout;
    private String readTimeout;

    // Getters and setters
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(String connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public String getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(String readTimeout) {
        this.readTimeout = readTimeout;
    }
}