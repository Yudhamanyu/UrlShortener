package com.urlshortener.cache;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.urlshortener.dto.UrlResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class UrlCacheService {

    private static final String KEY_PREFIX = "url:shortcode:";
    private static final Duration TTL = Duration.ofHours(2);

    private final RedisTemplate<String, Object> redisTemplate;

    public Optional<UrlResponse> get(String shortCode) {
        try {
            String key = buildKey(shortCode);
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof UrlResponse urlResponse) {
                log.debug("Cache HIT for shortCode={}", shortCode);
                return Optional.of(urlResponse);
            }
            log.debug("Cache MISS for shortCode={}", shortCode);
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Redis GET failed for shortCode={}, falling back to DB. Reason: {}",
                    shortCode, e.getMessage());
            return Optional.empty();
        }
    }

    public void put(String shortCode, UrlResponse urlResponse) {
        try {
            String key = buildKey(shortCode);
            redisTemplate.opsForValue().set(key, urlResponse, TTL);
            log.debug("Cached shortCode={} with TTL={}", shortCode, TTL);
        } catch (Exception e) {
            log.warn("Redis SET failed for shortCode={}. Reason: {}", shortCode, e.getMessage());
        }
    }

    public void evict(String shortCode) {
        try {
            String key = buildKey(shortCode);
            redisTemplate.delete(key);
            log.debug("Evicted cache for shortCode={}", shortCode);
        } catch (Exception e) {
            log.warn("Redis DELETE failed for shortCode={}. Reason: {}", shortCode, e.getMessage());
        }
    }

    private String buildKey(String shortCode) {
        return KEY_PREFIX + shortCode;
    }
}