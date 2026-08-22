package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pathshala.stub.entity.LocationPoint;

import java.time.Instant;
import java.util.UUID;

/**
 * Read model for GET /api/locations.
 *
 * Both timestamps are stored as UTC Instants in the DB, but serialized
 * here as IST "yyyy-MM-dd HH:mm:ss" — the single place where that
 * conversion is defined.
 */
public record LocationPingResponse(

        Long id,

        @JsonProperty("user_id")
        UUID userId,

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
) {
    /** Convenience factory from entity. */
    public static LocationPingResponse from(LocationPoint entity) {
        return new LocationPingResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getLat(),
                entity.getLng(),
                entity.getCapturedAt(),
                entity.getReceivedAt(),
                entity.getSyncStatus()
        );
    }
}
