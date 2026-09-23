package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * PUT /api/admin/paathshaalas/{id} — all fields optional.
 * If map_link is provided (without manual lat/lng), coordinates are re-parsed from it.
 * If lat/lng are provided directly, they take precedence with confidence "manual".
 */
public record UpdatePaathshalaRequest(
        String name,

        @JsonProperty("map_link")
        String mapLink,

        Double lat,

        Double lng,

        @JsonProperty("supervisor_id")
        java.util.UUID supervisorId,

        @jakarta.validation.constraints.Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Format must be HH:mm")
        @JsonProperty("opening_time")
        String openingTime,

        @jakarta.validation.constraints.Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Format must be HH:mm")
        @JsonProperty("closing_time")
        String closingTime
) {}
