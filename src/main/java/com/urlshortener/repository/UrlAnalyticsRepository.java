package com.urlshortener.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.urlshortener.entity.UrlAnalytics;

@Repository
public interface UrlAnalyticsRepository extends JpaRepository<UrlAnalytics, Long> {

    Page<UrlAnalytics> findByUrlIdOrderByClickedAtDesc(Long urlId, Pageable pageable);

    long countByUrlId(Long urlId);

    @Query("SELECT FUNCTION('DATE', a.clickedAt) AS day, COUNT(a) AS clicks " +
            "FROM UrlAnalytics a " +
            "WHERE a.url.id = :urlId " +
            "GROUP BY FUNCTION('DATE', a.clickedAt) " +
            "ORDER BY FUNCTION('DATE', a.clickedAt) ASC")
    List<Object[]> countDailyClicksByUrlId(@Param("urlId") Long urlId);

    @Query("SELECT a.browser, COUNT(a) FROM UrlAnalytics a " +
            "WHERE a.url.id = :urlId GROUP BY a.browser")
    List<Object[]> countByBrowserForUrlId(@Param("urlId") Long urlId);

    @Query("SELECT a.os, COUNT(a) FROM UrlAnalytics a " +
            "WHERE a.url.id = :urlId GROUP BY a.os")
    List<Object[]> countByOsForUrlId(@Param("urlId") Long urlId);

    @Query("SELECT a.deviceType, COUNT(a) FROM UrlAnalytics a " +
            "WHERE a.url.id = :urlId GROUP BY a.deviceType")
    List<Object[]> countByDeviceTypeForUrlId(@Param("urlId") Long urlId);

    @Query("SELECT a.referrer, COUNT(a) FROM UrlAnalytics a " +
            "WHERE a.url.id = :urlId GROUP BY a.referrer")
    List<Object[]> countByReferrerForUrlId(@Param("urlId") Long urlId);

    List<UrlAnalytics> findByUrlIdAndClickedAtBetween(Long urlId, LocalDateTime start, LocalDateTime end);
}