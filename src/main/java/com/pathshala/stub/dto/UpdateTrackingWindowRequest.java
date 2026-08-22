package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/** PUT /api/admin/tracking-windows/{id} — all fields optional. */
public record UpdateTrackingWindowRequest(

        @JsonProperty("start_time")
        String startTime,

        @JsonProperty("end_time")
        String endTime,

        @JsonProperty("interval_minutes")
        Integer intervalMinutes,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @JsonProperty("effective_from_date")
        LocalDate effectiveFromDate
) {}
