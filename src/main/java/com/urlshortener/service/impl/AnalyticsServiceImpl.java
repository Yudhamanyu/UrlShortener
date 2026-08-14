package com.urlshortener.service.impl;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.urlshortener.dto.AnalyticsResponse;
import com.urlshortener.dto.DailyClickResponse;
import com.urlshortener.entity.Url;
import com.urlshortener.entity.UrlAnalytics;
import com.urlshortener.entity.UrlStats;
import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.repository.UrlAnalyticsRepository;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.repository.UrlStatsRepository;
import com.urlshortener.service.AnalyticsService;
import com.urlshortener.service.GeoIpService;
import com.urlshortener.util.UserAgentParser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private final UrlRepository urlRepository;
    private final UrlAnalyticsRepository urlAnalyticsRepository;
    private final UrlStatsRepository urlStatsRepository;
    private final GeoIpService geoIpService;

    @Override
    @Async("analyticsExecutor")
    @Transactional
    public void recordClick(String shortCode, String ipAddress, String userAgent, String referrer) {
        try {
            Url url = urlRepository.findByShortCode(shortCode).orElse(null);
            if (url == null) {
                log.warn("Attempted to record click for unknown shortCode={}", shortCode);
                return;
            }

            UserAgentParser.ParsedUserAgent parsed = UserAgentParser.parse(userAgent);
            String country = geoIpService.resolveCountry(ipAddress);

            UrlAnalytics analytics = UrlAnalytics.builder()
                    .url(url)
                    .clickedAt(LocalDateTime.now())
                    .ipAddress(ipAddress)
                    .country(country)
                    .browser(parsed.browser())
                    .os(parsed.os())
                    .deviceType(parsed.deviceType())
                    .referrer(referrer)
                    .build();
            urlAnalyticsRepository.save(analytics);

            updateStats(url);

            log.info("Recorded click shortCode={} country={} browser={} os={} device={}",
                    shortCode, country, parsed.browser(), parsed.os(), parsed.deviceType());
        } catch (Exception e) {
            log.error("Failed to record click for shortCode={}", shortCode, e);
        }
    }

    private void updateStats(Url url) {
        UrlStats stats = urlStatsRepository.findByUrlIdForUpdate(url.getId())
                .orElseGet(() -> UrlStats.builder()
                        .url(url)
                        .totalClicks(0L)
                        .build());

        LocalDateTime now = LocalDateTime.now();
        stats.setTotalClicks(stats.getTotalClicks() + 1);
        if (stats.getFirstVisit() == null) {
            stats.setFirstVisit(now);
        }
        stats.setLastVisit(now);
        urlStatsRepository.save(stats);
    }

    @Override
    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("Short URL not found: " + shortCode));

        UrlStats stats = urlStatsRepository.findByUrlId(url.getId())
                .orElse(UrlStats.builder().totalClicks(0L).build());

        List<DailyClickResponse> dailyClicks = urlAnalyticsRepository.countDailyClicksByUrlId(url.getId())
                .stream()
                .map(this::toDailyClickResponse)
                .collect(Collectors.toList());

        Map<String, Long> browserBreakdown = toBreakdownMap(
                urlAnalyticsRepository.countByBrowserForUrlId(url.getId()));
        Map<String, Long> osBreakdown = toBreakdownMap(
                urlAnalyticsRepository.countByOsForUrlId(url.getId()));
        Map<String, Long> deviceBreakdown = toBreakdownMap(
                urlAnalyticsRepository.countByDeviceTypeForUrlId(url.getId()));
        Map<String, Long> referrerBreakdown = toBreakdownMap(
                urlAnalyticsRepository.countByReferrerForUrlId(url.getId()));

        return AnalyticsResponse.builder()
                .shortCode(url.getShortCode())
                .originalUrl(url.getOriginalUrl())
                .totalClicks(stats.getTotalClicks())
                .firstVisit(stats.getFirstVisit())
                .lastVisit(stats.getLastVisit())
                .dailyClicks(dailyClicks)
                .browserBreakdown(browserBreakdown)
                .osBreakdown(osBreakdown)
                .deviceBreakdown(deviceBreakdown)
                .referrerBreakdown(referrerBreakdown)
                .build();
    }

    private DailyClickResponse toDailyClickResponse(Object[] row) {
        LocalDate date = toLocalDate(row[0]);
        Long clicks = ((Number) row[1]).longValue();
        return DailyClickResponse.builder().date(date).clicks(clicks).build();
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }
        return null;
    }

    private Map<String, Long> toBreakdownMap(List<Object[]> rows) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String key = row[0] != null ? row[0].toString() : "Unknown";
            Long value = ((Number) row[1]).longValue();
            map.merge(key, value, Long::sum);
        }
        return map;
    }
}