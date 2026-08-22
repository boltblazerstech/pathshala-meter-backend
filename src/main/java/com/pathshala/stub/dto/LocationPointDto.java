package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * One element of the POST /api/locations/batch request body.
 * user_id is intentionally absent — it is taken from the JWT, never trusted from the client.
 */
public record LocationPointDto(

        @NotNull(message = "lat is required")
        double lat,

        @NotNull(message = "lng is required")
        double lng,

        @NotBlank(message = "captured_at is required (ISO-8601 instant, e.g. 2026-08-22T10:00:00Z)")
        @JsonProperty("captured_at")
        String capturedAt
) {}
