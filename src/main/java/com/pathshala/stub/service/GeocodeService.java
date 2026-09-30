package com.pathshala.stub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pathshala.stub.dto.GeocodeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GeocodeService {

    private static final Logger log = LoggerFactory.getLogger(GeocodeService.class);
    
    private final String apiKey;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    
    // In-memory cache keyed by rounded lat,lng
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    public GeocodeService(
            @Value("${google.maps.api-key:}") String apiKey,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Reverses geocodes a latitude and longitude into a readable address.
     * Uses an in-memory cache to avoid repeated identical calls.
     * Never throws exceptions; returns errors gracefully in GeocodeResult.
     */
    public GeocodeResult reverseGeocode(double lat, double lng) {
        // Round to 6 decimal places (approx 11cm accuracy)
        String cacheKey = String.format(Locale.US, "%.6f,%.6f", lat, lng);
        if (cache.containsKey(cacheKey)) {
            return new GeocodeResult(cache.get(cacheKey), null);
        }

        // 1. Try Google Maps Geocoding API if key is configured
        if (apiKey != null && !apiKey.isBlank()) {
            try {
                String url = String.format(Locale.US, 
                    "https://maps.googleapis.com/maps/api/geocode/json?latlng=%s&key=%s", 
                    cacheKey, apiKey);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(5))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    String status = root.path("status").asText();

                    if ("OK".equals(status)) {
                        JsonNode results = root.path("results");
                        if (results.isArray() && results.size() > 0) {
                            String address = results.get(0).path("formatted_address").asText();
                            cache.put(cacheKey, address);
                            return new GeocodeResult(address, null);
                        }
                    } else if ("ZERO_RESULTS".equals(status)) {
                        return new GeocodeResult(null, "No results found for coordinates");
                    }
                }
            } catch (Exception e) {
                log.warn("Google Geocoding failed, falling back to OpenStreetMap: {}", e.getMessage());
            }
        }

        // 2. Fallback to OpenStreetMap Nominatim (free, no API key needed for local dev)
        try {
            String osmUrl = String.format(Locale.US,
                "https://nominatim.openstreetmap.org/reverse?format=json&lat=%.6f&lon=%.6f",
                lat, lng);
            HttpRequest osmRequest = HttpRequest.newBuilder()
                    .uri(URI.create(osmUrl))
                    .header("User-Agent", "PathshalaMeter/1.0 (admin-reverse-geocode)")
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> osmResponse = httpClient.send(osmRequest, HttpResponse.BodyHandlers.ofString());
            if (osmResponse.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(osmResponse.body());
                String displayName = root.path("display_name").asText();
                if (displayName != null && !displayName.isBlank()) {
                    cache.put(cacheKey, displayName);
                    return new GeocodeResult(displayName, null);
                }
            }
        } catch (Exception e) {
            log.error("OpenStreetMap Geocoding failed: {}", e.getMessage());
        }

        return new GeocodeResult(null, "Could not resolve address");
    }
}
