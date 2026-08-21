package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LocationPointDto(
    double lat,
    double lng,
    @JsonProperty("captured_at") String capturedAt
) {}
