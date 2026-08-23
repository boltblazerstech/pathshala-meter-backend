package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

/** Response body for supervisor endpoints. */
public record SupervisorResponse(
        UUID id,
        String name,

        @JsonProperty("phone_number")
        String phoneNumber,

        boolean active,

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

        @JsonProperty("selected_paathshaala_id")
        UUID selectedPaathshaalaId,

        @JsonProperty("selected_paathshaala_name")
        String selectedPaathshaalaName,

        @JsonProperty("latest_distance_meters")
        Double latestDistanceMeters
) {}
