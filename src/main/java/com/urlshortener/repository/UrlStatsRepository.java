package com.urlshortener.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.urlshortener.entity.UrlStats;

import jakarta.persistence.LockModeType;

@Repository
public interface UrlStatsRepository extends JpaRepository<UrlStats, Long> {

    Optional<UrlStats> findByUrlId(Long urlId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM UrlStats s WHERE s.url.id = :urlId")
    Optional<UrlStats> findByUrlIdForUpdate(@Param("urlId") Long urlId);

    boolean existsByUrlId(Long urlId);
}