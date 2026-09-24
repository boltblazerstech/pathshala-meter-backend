package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;

public record GeofenceEventResponse(
    @JsonProperty("date") LocalDate date,
    @JsonProperty("events") List<GeofenceEventDto> events
) {}
