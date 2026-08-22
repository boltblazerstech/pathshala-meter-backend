package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

/**
 * Enhanced location response for the Admin dashboard's live view.
 * Includes user details to avoid requiring the frontend to perform secondary lookups.
 */
public record AdminLiveLocationResponse(

        @JsonProperty("user_id")
        UUID userId,

        @JsonProperty("user_name")
        String userName,

        @JsonProperty("user_role")
        String userRole,

        @JsonProperty("paathshaala_name")
        String paathshaalaName,

        double lat,
        double lng,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
        @JsonProperty("captured_at")
        Instant capturedAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
        @JsonProperty("received_at")
        Instant receivedAt,

        @JsonProperty("sync_status")
        String syncStatus
) {}
