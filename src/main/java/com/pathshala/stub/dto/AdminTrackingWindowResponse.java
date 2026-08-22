package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Full tracking-window detail — used by admin endpoints. */
public record AdminTrackingWindowResponse(

        UUID id,

        @JsonProperty("user_id")
        UUID userId,

        @JsonFormat(pattern = "HH:mm")
        @JsonProperty("start_time")
        String startTime,

        @JsonFormat(pattern = "HH:mm")
        @JsonProperty("end_time")
        String endTime,

        @JsonProperty("interval_minutes")
        int intervalMinutes,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @JsonProperty("effective_from_date")
        LocalDate effectiveFromDate,

        @JsonProperty("is_active")
        boolean isActive,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
        @JsonProperty("created_at")
        Instant createdAt
) {}
