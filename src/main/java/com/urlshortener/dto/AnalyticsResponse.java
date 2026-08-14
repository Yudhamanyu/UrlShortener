package com.urlshortener.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsResponse {

    private String shortCode;
    private String originalUrl;
    private Long totalClicks;
    private LocalDateTime firstVisit;
    private LocalDateTime lastVisit;
    private List<DailyClickResponse> dailyClicks;
    private Map<String, Long> browserBreakdown;
    private Map<String, Long> osBreakdown;
    private Map<String, Long> deviceBreakdown;
    private Map<String, Long> referrerBreakdown;
}