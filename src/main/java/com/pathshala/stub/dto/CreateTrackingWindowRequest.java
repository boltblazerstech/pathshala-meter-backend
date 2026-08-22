package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateTrackingWindowRequest(

        @NotNull(message = "user_id is required")
        @JsonProperty("user_id")
        UUID userId,

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
