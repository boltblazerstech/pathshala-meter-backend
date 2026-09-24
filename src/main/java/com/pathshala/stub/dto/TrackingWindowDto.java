package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record TrackingWindowDto(
    @JsonProperty("start_time") String startTime,
    @JsonProperty("end_time") String endTime,
    @JsonProperty("interval_minutes") int intervalMinutes,
    @JsonProperty("paathshaala_id") UUID paathshaalaId
) {}
