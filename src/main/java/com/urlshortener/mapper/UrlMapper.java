package com.urlshortener.mapper;

import org.springframework.stereotype.Component;

import com.urlshortener.config.AppProperties;
import com.urlshortener.dto.UrlResponse;
import com.urlshortener.entity.Url;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UrlMapper {

    private final AppProperties appProperties;

    public UrlResponse toResponse(Url url) {
        if (url == null) {
            return null;
        }
        return UrlResponse.builder()
                .id(url.getId())
                .shortCode(url.getShortCode())
                .shortUrl(buildShortUrl(url.getShortCode()))
                .originalUrl(url.getOriginalUrl())
                .customAlias(url.getCustomAlias())
                .expirationDate(url.getExpirationDate())
                .isActive(url.getIsActive())
                .createdAt(url.getCreatedAt())
                .updatedAt(url.getUpdatedAt())
                .build();
    }

    private String buildShortUrl(String shortCode) {
        String base = appProperties.getBaseUrl();
        if (base.endsWith("/")) {
            return base + shortCode;
        }
        return base + "/" + shortCode;
    }
}