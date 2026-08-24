package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

/**
 * Response body for teacher endpoints.
 * Includes paathshaala_name from a join so the admin UI
 * doesn't need a second lookup to show the paathshaala label.
 */
public record TeacherResponse(
        UUID id,
        String name,

        @JsonProperty("phone_number")
        String phoneNumber,

        String password,

        boolean active,

        @JsonProperty("paathshaala_id")
        UUID paathshalaId,

        @JsonProperty("paathshaala_name")
        String paathshalaName,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
        @JsonProperty("created_at")
        Instant createdAt,

        @JsonProperty("last_location_lat")
        Double lastLocationLat,

        @JsonProperty("last_location_lng")
        Double lastLocationLng,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
        @JsonProperty("last_location_at")
        Instant lastLocationAt,

        @JsonProperty("latest_distance_meters")
        Double latestDistanceMeters
) {}
