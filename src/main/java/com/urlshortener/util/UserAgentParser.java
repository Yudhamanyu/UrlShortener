package com.urlshortener.util;

import eu.bitwalker.useragentutils.UserAgent;

public final class UserAgentParser {

    private UserAgentParser() {
    }

    public static ParsedUserAgent parse(String userAgentString) {
        if (userAgentString == null || userAgentString.isBlank()) {
            return new ParsedUserAgent("Unknown", "Unknown", "Unknown");
        }

        UserAgent ua = UserAgent.parseUserAgentString(userAgentString);

        String browser = ua.getBrowser() != null ? ua.getBrowser().getName() : "Unknown";
        String os = ua.getOperatingSystem() != null ? ua.getOperatingSystem().getName() : "Unknown";
        String deviceType = (ua.getOperatingSystem() != null && ua.getOperatingSystem().getDeviceType() != null)
                ? ua.getOperatingSystem().getDeviceType().getName()
                : "Unknown";

        return new ParsedUserAgent(browser, os, deviceType);
    }

    public record ParsedUserAgent(String browser, String os, String deviceType) {
    }
}