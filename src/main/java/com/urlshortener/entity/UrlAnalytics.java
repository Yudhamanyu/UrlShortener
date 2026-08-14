package com.urlshortener.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "url_analytics",
        indexes = {
                @Index(name = "idx_url_analytics_url_id", columnList = "url_id"),
                @Index(name = "idx_url_analytics_clicked_at", columnList = "clicked_at"),
                @Index(
                        name = "idx_url_analytics_url_id_clicked_at",
                        columnList = "url_id, clicked_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UrlAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "url_id",
            nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(
                    name = "fk_url_analytics_url"
            )
    )
    private Url url;

    @Column(name = "clicked_at", nullable = false)
    @Builder.Default
    private LocalDateTime clickedAt = LocalDateTime.now();

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "browser", length = 100)
    private String browser;

    @Column(name = "os", length = 100)
    private String os;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(name = "referrer", columnDefinition = "TEXT")
    private String referrer;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}