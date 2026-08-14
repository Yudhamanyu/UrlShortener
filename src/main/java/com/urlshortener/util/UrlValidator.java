package com.urlshortener.util;

import com.urlshortener.exception.InvalidUrlException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public final class UrlValidator {

    private UrlValidator() {
    }

    public static void validate(String url) {
        try {
            URI uri = new URI(url);
            URL parsed = uri.toURL();
            String protocol = parsed.getProtocol();
            if (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol)) {
                throw new InvalidUrlException("URL must use http or https protocol");
            }
            if (parsed.getHost() == null || parsed.getHost().isBlank()) {
                throw new InvalidUrlException("URL must contain a valid host");
            }
        } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
            throw new InvalidUrlException("Malformed URL: " + url);
        }
    }
}