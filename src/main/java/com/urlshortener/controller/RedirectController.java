package com.urlshortener.controller;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.urlshortener.dto.UrlResponse;
import com.urlshortener.service.AnalyticsService;
import com.urlshortener.service.UrlService;
import com.urlshortener.util.IpAddressResolver;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final UrlService urlService;
    private final AnalyticsService analyticsService;

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode, HttpServletRequest request) {
        UrlResponse url = urlService.resolveShortUrl(shortCode);

        String ipAddress = IpAddressResolver.resolve(request);
        String userAgent = request.getHeader("User-Agent");
        String referrer = request.getHeader("Referer");
        analyticsService.recordClick(shortCode, ipAddress, userAgent, referrer);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(url.getOriginalUrl()))
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .build();
    }
}