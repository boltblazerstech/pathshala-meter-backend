package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record LocationPointDetailDto(
        @JsonFormat(pattern = "HH:mm:ss", timezone = "Asia/Kolkata")
        @JsonProperty("captured_at")
        Instant capturedAt,

        @JsonFormat(pattern = "HH:mm:ss", timezone = "Asia/Kolkata")
        @JsonProperty("received_at")
        Instant receivedAt,

        double lat,
        double lng,

        @JsonProperty("distance_meters")
        Double distanceMeters
) {}
