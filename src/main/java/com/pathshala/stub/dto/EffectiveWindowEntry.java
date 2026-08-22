package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.UUID;

/**
 * One row in the admin's date-scoped overview.
 * Embeds user name and role so the admin table doesn't need a second lookup.
 */
public record EffectiveWindowEntry(

        @JsonProperty("window_id")
        UUID windowId,

        @JsonProperty("user_id")
        UUID userId,

        @JsonProperty("user_name")
        String userName,

        String role,

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
        LocalDate effectiveFromDate
) {}
