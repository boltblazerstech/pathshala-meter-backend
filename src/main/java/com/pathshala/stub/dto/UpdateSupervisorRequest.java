package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** PUT /api/admin/supervisors/{id} — all fields optional. */
public record UpdateSupervisorRequest(
        String name,

        @JsonProperty("phone_number")
        String phoneNumber,

        String password,

        @JsonProperty("home_map_link")
        String homeMapLink,

        @JsonProperty("home_lat")
        Double homeLat,

        @JsonProperty("home_lng")
        Double homeLng
) {}
