package com.pritiranjan.blog.service.impl;

import com.pritiranjan.blog.entity.Visit;
import com.pritiranjan.blog.repository.VisitRepository;
import com.pritiranjan.blog.service.VisitService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VisitServiceImpl implements VisitService {

    private final VisitRepository visitRepository;

    public VisitServiceImpl(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    @Override
    public Visit recordVisit(String ipAddress, String userAgent, String pageUrl, String referrer) {
        Visit visit = Visit.builder()
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .pageUrl(pageUrl)
                .referrer(referrer)
                .visitedAt(Instant.now())
                .build();

        Visit saved = visitRepository.save(visit);

        // Trigger asynchronous geolocation enrichment
        // Note: In Spring, invoking an @Async method within the same bean bypasses the proxy.
        // We will call it dynamically or handle the async boundary properly.
        // To be safe, we invoke it. Spring's proxy works when called from a controller.
        return saved;
    }

    @Async
    @Override
    public void enrichLocationAsync(Long visitId) {
        Optional<Visit> optionalVisit = visitRepository.findById(visitId);
        if (optionalVisit.isEmpty()) {
            return;
        }

        Visit visit = optionalVisit.get();
        String ipAddress = visit.getIpAddress();

        // 1. Handle local addresses
        if (isPrivateOrLocalAddress(ipAddress)) {
            visit.setCountry("Localhost");
            visit.setCountryCode("LH");
            visit.setRegion("Local Network");
            visit.setCity("Local Dev");
            visit.setZip("00000");
            visit.setLatitude(0.0);
            visit.setLongitude(0.0);
            visit.setIsp("Local Development Network");
            visitRepository.save(visit);
            return;
        }

        // 2. Try fetching from cached IP records in DB
        Optional<Visit> cachedVisit = visitRepository.findFirstByIpAddressAndCountryIsNotNullOrderByVisitedAtDesc(ipAddress);
        if (cachedVisit.isPresent()) {
            Visit cv = cachedVisit.get();
            visit.setCountry(cv.getCountry());
            visit.setCountryCode(cv.getCountryCode());
            visit.setRegion(cv.getRegion());
            visit.setCity(cv.getCity());
            visit.setZip(cv.getZip());
            visit.setLatitude(cv.getLatitude());
            visit.setLongitude(cv.getLongitude());
            visit.setIsp(cv.getIsp());
            visitRepository.save(visit);
            return;
        }

        // 3. Fallback to API requests
        RestTemplate restTemplate = new RestTemplate();
        try {
            String url = "http://ip-api.com/json/" + ipAddress;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && "success".equals(response.get("status"))) {
                visit.setCountry((String) response.get("country"));
                visit.setCountryCode((String) response.get("countryCode"));
                visit.setRegion((String) response.get("regionName"));
                visit.setCity((String) response.get("city"));
                visit.setZip((String) response.get("zip"));
                visit.setLatitude(toDouble(response.get("lat")));
                visit.setLongitude(toDouble(response.get("lon")));
                visit.setIsp((String) response.get("isp"));
            } else {
                tryFreeIpApi(restTemplate, ipAddress, visit);
            }
        } catch (Exception e) {
            try {
                tryFreeIpApi(restTemplate, ipAddress, visit);
            } catch (Exception ex) {
                // If everything fails, record as Unknown
                visit.setCountry("Unknown");
                visit.setCountryCode("UN");
                visit.setRegion("Unknown");
                visit.setCity("Unknown");
                visit.setZip("Unknown");
                visit.setIsp("Unknown ISP");
            }
        }
        visitRepository.save(visit);
    }

    @Override
    public Page<Visit> getVisits(Pageable pageable) {
        return visitRepository.findAll(pageable);
    }

    @Override
    public Map<String, Object> getVisitStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalVisits", visitRepository.count());
        stats.put("uniqueVisitors", visitRepository.countUniqueVisitors());

        // Top Countries
        List<Object[]> countryData = visitRepository.countVisitsByCountry();
        List<Map<String, Object>> countries = countryData.stream().map(row -> {
            Map<String, Object> item = new HashMap<>();
            item.put("name", row[0] != null ? row[0] : "Unknown");
            item.put("code", row[1] != null ? row[1] : "UN");
            item.put("count", row[2]);
            return item;
        }).collect(Collectors.toList());
        stats.put("countries", countries);

        // Top Referrers
        List<Object[]> referrerData = visitRepository.countVisitsByReferrer();
        List<Map<String, Object>> referrers = referrerData.stream().map(row -> {
            Map<String, Object> item = new HashMap<>();
            item.put("referrer", row[0] != null ? row[0] : "Direct");
            item.put("count", row[1]);
            return item;
        }).collect(Collectors.toList());
        stats.put("referrers", referrers);

        // Top Pages
        List<Object[]> pageData = visitRepository.countVisitsByPageUrl();
        List<Map<String, Object>> pages = pageData.stream().map(row -> {
            Map<String, Object> item = new HashMap<>();
            item.put("pageUrl", row[0] != null ? row[0] : "Unknown");
            item.put("count", row[1]);
            return item;
        }).collect(Collectors.toList());
        stats.put("pages", pages);

        return stats;
    }

    private void tryFreeIpApi(RestTemplate restTemplate, String ipAddress, Visit visit) {
        String url = "https://freeipapi.com/api/json/" + ipAddress;
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response != null) {
            visit.setCountry((String) response.get("countryName"));
            visit.setCountryCode((String) response.get("countryCode"));
            visit.setRegion((String) response.get("regionName"));
            visit.setCity((String) response.get("cityName"));
            visit.setZip((String) response.get("zipCode"));
            visit.setLatitude(toDouble(response.get("latitude")));
            visit.setLongitude(toDouble(response.get("longitude")));
            visit.setIsp("FreeIPAPI");
        }
    }

    private Double toDouble(Object val) {
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        } else if (val instanceof String) {
            try {
                return Double.parseDouble((String) val);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    private boolean isPrivateOrLocalAddress(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return true;
        }
        String cleanIp = ip.trim();
        return "127.0.0.1".equals(cleanIp) ||
                "0:0:0:0:0:0:0:1".equals(cleanIp) ||
                "localhost".equalsIgnoreCase(cleanIp) ||
                cleanIp.startsWith("10.") ||
                cleanIp.startsWith("192.168.") ||
                (cleanIp.startsWith("172.") && isPrivate172(cleanIp));
    }

    private boolean isPrivate172(String ip) {
        try {
            String[] parts = ip.split("\\.");
            if (parts.length >= 2) {
                int secondOctet = Integer.parseInt(parts[1]);
                return secondOctet >= 16 && secondOctet <= 31;
            }
        } catch (NumberFormatException e) {
            // Ignore parsing failures
        }
        return false;
    }
}
