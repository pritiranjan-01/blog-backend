package com.pritiranjan.blog.controller;

import com.pritiranjan.blog.dto.ApiResponse;
import com.pritiranjan.blog.entity.Visit;
import com.pritiranjan.blog.service.VisitService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/visits")
public class VisitController {

    private final VisitService visitService;

    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Visit>> recordVisit(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        String ipAddress = getClientIp(request);
        String userAgent = body.getOrDefault("userAgent", "");
        String pageUrl = body.getOrDefault("pageUrl", "");
        String referrer = body.getOrDefault("referrer", "");

        Visit visit = visitService.recordVisit(ipAddress, userAgent, pageUrl, referrer);

        // Run the location enrichment asynchronously
        visitService.enrichLocationAsync(visit.getId());

        return ResponseEntity.ok(ApiResponse.success("Visit registered successfully", visit));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Visit>>> getVisits(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "visitedAt") String sortBy,
            @RequestParam(name = "direction", defaultValue = "desc") String direction) {

        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Visit> visits = visitService.getVisits(pageable);

        return ResponseEntity.ok(ApiResponse.success("Visits fetched successfully", visits));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getVisitStats() {
        Map<String, Object> stats = visitService.getVisitStats();
        return ResponseEntity.ok(ApiResponse.success("Visit stats fetched successfully", stats));
    }

    // Helper method to extract the client IP address from proxy headers if present
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // If multiple hops/proxies exist (comma separated list), return the first IP client address
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
