package com.urlshortener.service;

import com.urlshortener.dto.AnalyticsResponse;

public interface AnalyticsService {

    void recordClick(String shortCode, String ipAddress, String userAgent, String referrer);

    AnalyticsResponse getAnalytics(String shortCode);
}