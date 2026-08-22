package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Applies one window definition to multiple users in a single call. */
public record BulkTrackingWindowRequest(

        @NotEmpty(message = "user_ids must not be empty")
        @JsonProperty("user_ids")
        List<UUID> userIds,

        @NotBlank(message = "start_time is required (HH:mm)")
        @JsonProperty("start_time")
        String startTime,

        @NotBlank(message = "end_time is required (HH:mm)")
        @JsonProperty("end_time")
        String endTime,

        @Min(value = 1, message = "interval_minutes must be at least 1")
        @JsonProperty("interval_minutes")
        int intervalMinutes,

        @NotNull(message = "effective_from_date is required (YYYY-MM-DD)")
        @JsonFormat(pattern = "yyyy-MM-dd")
        @JsonProperty("effective_from_date")
        LocalDate effectiveFromDate
) {}
