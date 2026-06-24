package com.pritiranjan.blog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "visits")
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    private String country;

    @Column(name = "country_code")
    private String countryCode;

    private String region;

    private String city;

    private String zip;

    private Double latitude;

    private Double longitude;

    private String isp;

    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    @Column(name = "page_url", length = 1000)
    private String pageUrl;

    @Column(name = "referrer", length = 1000)
    private String referrer;

    @Column(name = "visited_at", nullable = false)
    private Instant visitedAt;

    @PrePersist
    public void prePersist() {
        if (visitedAt == null) {
            visitedAt = Instant.now();
        }
    }
}
