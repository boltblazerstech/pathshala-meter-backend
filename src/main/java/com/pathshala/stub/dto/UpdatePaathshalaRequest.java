package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * PUT /api/admin/paathshaalas/{id} — all fields optional.
 * If map_link is provided, coordinates are re-parsed from it.
 */
public record UpdatePaathshalaRequest(
        String name,

        @JsonProperty("map_link")
        String mapLink
) {}
