package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

/** Response body for paathshaala endpoints. */
public record PaathshalaResponse(
        UUID id,
        String name,
        double lat,
        double lng,

        @JsonProperty("source_map_link")
        String sourceMapLink,

        @JsonProperty("coordinate_confidence")
        String coordinateConfidence,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
        @JsonProperty("created_at")
        Instant createdAt
) {}
