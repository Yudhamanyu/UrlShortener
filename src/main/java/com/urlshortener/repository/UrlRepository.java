package com.urlshortener.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.urlshortener.entity.Url;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortCode(String shortCode);

    Optional<Url> findByShortCodeAndIsActiveTrue(String shortCode);

    boolean existsByShortCode(String shortCode);

    boolean existsByCustomAlias(String customAlias);

    Page<Url> findByIsActiveTrue(Pageable pageable);

    @org.springframework.data.jpa.repository.Query(
            "SELECT u FROM Url u WHERE u.shortCode = :shortCode " +
            "AND u.isActive = true " +
            "AND (u.expirationDate IS NULL OR u.expirationDate > :now)"
    )
    Optional<Url> findActiveAndNotExpired(@Param("shortCode") String shortCode,
                                           @Param("now") LocalDateTime now);
}