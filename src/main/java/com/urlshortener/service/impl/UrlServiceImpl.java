package com.urlshortener.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.urlshortener.cache.UrlCacheService;
import com.urlshortener.dto.CreateUrlRequest;
import com.urlshortener.dto.PagedResponse;
import com.urlshortener.dto.UpdateUrlRequest;
import com.urlshortener.dto.UrlResponse;
import com.urlshortener.entity.Url;
import com.urlshortener.entity.UrlStats;
import com.urlshortener.exception.DuplicateAliasException;
import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.exception.UrlExpiredException;
import com.urlshortener.mapper.UrlMapper;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.repository.UrlStatsRepository;
import com.urlshortener.service.UrlService;
import com.urlshortener.util.Base62Encoder;
import com.urlshortener.util.RandomCodeGenerator;
import com.urlshortener.util.UrlValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlServiceImpl implements UrlService {

    private static final int MAX_COLLISION_RETRIES = 5;

    private final UrlRepository urlRepository;
    private final UrlStatsRepository urlStatsRepository;
    private final UrlMapper urlMapper;
    private final UrlCacheService urlCacheService;

    @Override
    @Transactional
    public UrlResponse createShortUrl(CreateUrlRequest request) {
        UrlValidator.validate(request.getOriginalUrl());

        String resolvedAlias = null;
        if (request.getCustomAlias() != null && !request.getCustomAlias().isBlank()) {
            if (urlRepository.existsByShortCode(request.getCustomAlias())) {
                throw new DuplicateAliasException(
                        "Custom alias '" + request.getCustomAlias() + "' is already in use");
            }
            resolvedAlias = request.getCustomAlias();
        }

        String placeholder = "TMP-" + RandomCodeGenerator.generate(16);
        Url url = Url.builder()
                .shortCode(placeholder)
                .originalUrl(request.getOriginalUrl())
                .customAlias(request.getCustomAlias())
                .expirationDate(request.getExpirationDate())
                .isActive(true)
                .build();
        url = urlRepository.save(url);

        String finalShortCode = resolvedAlias != null
                ? resolvedAlias
                : generateDeterministicCode(url.getId());

        url.setShortCode(finalShortCode);
        url = urlRepository.save(url);

        UrlStats stats = UrlStats.builder()
                .url(url)
                .totalClicks(0L)
                .build();
        urlStatsRepository.save(stats);

        UrlResponse response = urlMapper.toResponse(url);
        urlCacheService.put(url.getShortCode(), response);

        log.info("Created short URL id={} shortCode={}", url.getId(), url.getShortCode());
        return response;
    }

    private String generateDeterministicCode(Long id) {
        String candidate = Base62Encoder.encode(id);
        int attempt = 0;
        while (urlRepository.existsByShortCode(candidate) && attempt < MAX_COLLISION_RETRIES) {
            candidate = Base62Encoder.encode(id) + RandomCodeGenerator.generate(2);
            attempt++;
        }
        if (urlRepository.existsByShortCode(candidate)) {
            throw new IllegalStateException(
                    "Unable to generate unique short code after " + MAX_COLLISION_RETRIES + " attempts");
        }
        return candidate;
    }

    @Override
    @Transactional(readOnly = true)
    public UrlResponse getUrlById(Long id) {
        Url url = urlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found with id: " + id));
        return urlMapper.toResponse(url);
    }

    @Override
    @Transactional(readOnly = true)
    public UrlResponse resolveShortUrl(String shortCode) {
        Optional<UrlResponse> cached = urlCacheService.get(shortCode);
        if (cached.isPresent()) {
            UrlResponse cachedResponse = cached.get();
            validateNotExpired(cachedResponse.getExpirationDate(), shortCode);
            return cachedResponse;
        }

        Url url = urlRepository.findByShortCodeAndIsActiveTrue(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("Short URL not found: " + shortCode));

        validateNotExpired(url.getExpirationDate(), shortCode);

        UrlResponse response = urlMapper.toResponse(url);
        urlCacheService.put(shortCode, response);
        return response;
    }

    private void validateNotExpired(LocalDateTime expirationDate, String shortCode) {
        if (expirationDate != null && expirationDate.isBefore(LocalDateTime.now())) {
            throw new UrlExpiredException("Short URL has expired: " + shortCode);
        }
    }

    @Override
    @Transactional
    public UrlResponse updateUrl(Long id, UpdateUrlRequest request) {
        Url url = urlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found with id: " + id));

        if (request.getOriginalUrl() != null && !request.getOriginalUrl().isBlank()) {
            UrlValidator.validate(request.getOriginalUrl());
            url.setOriginalUrl(request.getOriginalUrl());
        }
        if (request.getExpirationDate() != null) {
            url.setExpirationDate(request.getExpirationDate());
        }
        if (request.getIsActive() != null) {
            url.setIsActive(request.getIsActive());
        }

        url = urlRepository.save(url);
        urlCacheService.evict(url.getShortCode());

        log.info("Updated URL id={}, cache evicted for shortCode={}", id, url.getShortCode());
        return urlMapper.toResponse(url);
    }

    @Override
    @Transactional
    public void deleteUrl(Long id) {
        Url url = urlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found with id: " + id));

        String shortCode = url.getShortCode();
        urlRepository.deleteById(id);
        urlCacheService.evict(shortCode);

        log.info("Deleted URL id={}, cache evicted for shortCode={}", id, shortCode);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UrlResponse> getAllUrls(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Url> urlPage = urlRepository.findByIsActiveTrue(pageable);

        List<UrlResponse> content = urlPage.getContent().stream()
                .map(urlMapper::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.<UrlResponse>builder()
                .content(content)
                .page(urlPage.getNumber())
                .size(urlPage.getSize())
                .totalElements(urlPage.getTotalElements())
                .totalPages(urlPage.getTotalPages())
                .last(urlPage.isLast())
                .build();
    }
}