package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/** PUT /api/admin/teachers/{id} — all fields optional. */
public record UpdateTeacherRequest(
        String name,

        @JsonProperty("phone_number")
        String phoneNumber,

        /** If provided, teacher is reassigned to a different paathshaala. */
        @JsonProperty("paathshaala_id")
        UUID paathshalaId,

        String password
) {}
