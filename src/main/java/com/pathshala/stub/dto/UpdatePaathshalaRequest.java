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

        Double lng
) {}
