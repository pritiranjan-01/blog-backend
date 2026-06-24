package com.pritiranjan.blog.repository;

import com.pritiranjan.blog.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    // Find the most recent resolved visit for an IP address to use cached location details
    Optional<Visit> findFirstByIpAddressAndCountryIsNotNullOrderByVisitedAtDesc(String ipAddress);

    // Count distinct visitor IPs
    @Query("SELECT COUNT(DISTINCT v.ipAddress) FROM Visit v")
    long countUniqueVisitors();

    // Top countries breakdown
    @Query("SELECT v.country, v.countryCode, COUNT(v) FROM Visit v WHERE v.country IS NOT NULL GROUP BY v.country, v.countryCode ORDER BY COUNT(v) DESC")
    List<Object[]> countVisitsByCountry();

    // Top referrers breakdown
    @Query("SELECT v.referrer, COUNT(v) FROM Visit v WHERE v.referrer IS NOT NULL AND v.referrer != '' GROUP BY v.referrer ORDER BY COUNT(v) DESC")
    List<Object[]> countVisitsByReferrer();

    // Top pages breakdown
    @Query("SELECT v.pageUrl, COUNT(v) FROM Visit v WHERE v.pageUrl IS NOT NULL GROUP BY v.pageUrl ORDER BY COUNT(v) DESC")
    List<Object[]> countVisitsByPageUrl();
}
