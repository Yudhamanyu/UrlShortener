package com.urlshortener.service;

import com.urlshortener.dto.CreateUrlRequest;
import com.urlshortener.dto.PagedResponse;
import com.urlshortener.dto.UpdateUrlRequest;
import com.urlshortener.dto.UrlResponse;

public interface UrlService {

    UrlResponse createShortUrl(CreateUrlRequest request);

    UrlResponse getUrlById(Long id);

    UrlResponse resolveShortUrl(String shortCode);

    UrlResponse updateUrl(Long id, UpdateUrlRequest request);

    void deleteUrl(Long id);

    PagedResponse<UrlResponse> getAllUrls(int page, int size);
}