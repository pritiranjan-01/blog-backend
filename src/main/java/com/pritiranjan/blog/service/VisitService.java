package com.pritiranjan.blog.service;

import com.pritiranjan.blog.entity.Visit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface VisitService {

    // Register a visit synchronously (saving basic details) and trigger async enrichment
    Visit recordVisit(String ipAddress, String userAgent, String pageUrl, String referrer);

    // Asynchronously resolve location and update the visit record
    void enrichLocationAsync(Long visitId);

    // Retrieve paginated visit logs
    Page<Visit> getVisits(Pageable pageable);

    // Retrieve aggregate statistics
    Map<String, Object> getVisitStats();
}
