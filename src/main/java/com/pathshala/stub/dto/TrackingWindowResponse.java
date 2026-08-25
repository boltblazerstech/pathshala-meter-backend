package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TrackingWindowResponse(
    @JsonProperty("start_time") String startTime,
    @JsonProperty("end_time") String endTime,
    @JsonProperty("interval_minutes") int intervalMinutes,
    @JsonProperty("healing_check_interval_minutes") int healingCheckIntervalMinutes
) {}
