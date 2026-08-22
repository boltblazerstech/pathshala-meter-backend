package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

/** Response body for supervisor endpoints. */
public record SupervisorResponse(
        UUID id,
        String name,

        @JsonProperty("phone_number")
        String phoneNumber,

        boolean active,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
        @JsonProperty("created_at")
        Instant createdAt
) {}
