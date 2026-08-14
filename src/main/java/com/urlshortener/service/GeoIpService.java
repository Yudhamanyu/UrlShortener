package com.urlshortener.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

import org.springframework.stereotype.Service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CountryResponse;
import com.urlshortener.config.AppProperties;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoIpService {

    private final AppProperties appProperties;

    private DatabaseReader databaseReader;

    @PostConstruct
    public void init() {
        String path = appProperties.getGeoip().getDatabasePath();
        try {
            InputStream inputStream = resolveInputStream(path);
            if (inputStream == null) {
                log.warn("GeoIP database not found at '{}'. Country resolution will return 'Unknown'.", path);
                return;
            }
            databaseReader = new DatabaseReader.Builder(inputStream).build();
            log.info("GeoIP database loaded successfully from '{}'", path);
        } catch (Exception e) {
            log.warn("Failed to load GeoIP database from '{}'. Country resolution disabled. Reason: {}",
                    path, e.getMessage());
            databaseReader = null;
        }
    }

    private InputStream resolveInputStream(String path) throws IOException {
        if (path == null || path.isBlank()) {
            return null;
        }
        if (path.startsWith("classpath:")) {
            String classpathLocation = path.substring("classpath:".length());
            return getClass().getClassLoader().getResourceAsStream(classpathLocation);
        }
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        return new FileInputStream(file);
    }

    public String resolveCountry(String ipAddress) {
        if (databaseReader == null || ipAddress == null || ipAddress.isBlank()) {
            return "Unknown";
        }
        if (isPrivateOrLoopback(ipAddress)) {
            return "Unknown";
        }
        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            CountryResponse response = databaseReader.country(inetAddress);
            return (response.getCountry() != null && response.getCountry().getName() != null)
                    ? response.getCountry().getName()
                    : "Unknown";
        } catch (Exception e) {
            log.debug("GeoIP lookup failed for ip={}. Reason: {}", ipAddress, e.getMessage());
            return "Unknown";
        }
    }

    private boolean isPrivateOrLoopback(String ip) {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            return addr.isLoopbackAddress() || addr.isSiteLocalAddress() || addr.isAnyLocalAddress();
        } catch (Exception e) {
            return true;
        }
    }

    @PreDestroy
    public void close() {
        if (databaseReader != null) {
            try {
                databaseReader.close();
            } catch (IOException e) {
                log.warn("Failed to close GeoIP database reader", e);
            }
        }
    }
}