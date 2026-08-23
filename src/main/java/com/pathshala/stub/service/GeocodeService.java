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
        if (apiKey == null || apiKey.isBlank()) {
            return new GeocodeResult(null, "Google Maps API key is not configured");
        }

        // Round to 6 decimal places (approx 11cm accuracy)
        String cacheKey = String.format(Locale.US, "%.6f,%.6f", lat, lng);
        if (cache.containsKey(cacheKey)) {
            return new GeocodeResult(cache.get(cacheKey), null);
        }

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

            if (response.statusCode() != 200) {
                return new GeocodeResult(null, "API error: HTTP " + response.statusCode());
            }

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
            } else {
                return new GeocodeResult(null, "API returned status: " + status);
            }
        } catch (Exception e) {
            log.error("Failed to connect to Geocoding API: {}", e.getMessage());
            return new GeocodeResult(null, "Failed to connect to Geocoding API: " + e.getMessage());
        }
        
        return new GeocodeResult(null, "Unknown error");
    }
}
