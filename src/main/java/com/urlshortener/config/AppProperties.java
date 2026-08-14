package com.urlshortener.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {

    private String baseUrl;
    private ShortCode shortCode = new ShortCode();
    private RateLimit rateLimit = new RateLimit();
    private GeoIp geoip = new GeoIp();
    private Cors cors = new Cors();

    @Data
    public static class ShortCode {
        private int length = 7;
    }

    @Data
    public static class RateLimit {
        private int capacity = 100;
        private int refillTokens = 100;
        private int refillDurationSeconds = 60;
    }

    @Data
    public static class GeoIp {
        private String databasePath;
    }

    @Data
    public static class Cors {
        private List<String> allowedOrigins = List.of("*");
    }
}