package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record GeofenceEventDto(
    @JsonProperty("event_type") String eventType,
    @JsonProperty("event_time") Instant eventTime,
    @JsonProperty("paathshaala_id") UUID paathshaalaId
) {}
