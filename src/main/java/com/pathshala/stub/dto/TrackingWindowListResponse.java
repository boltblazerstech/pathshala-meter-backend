package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TrackingWindowListResponse(
    @JsonProperty("windows") List<TrackingWindowDto> windows,
    @JsonProperty("healing_check_interval_minutes") int healingCheckIntervalMinutes
) {}
