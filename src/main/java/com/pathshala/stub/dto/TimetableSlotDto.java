package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record TimetableSlotDto(
        @JsonProperty("day_of_week")
        Integer dayOfWeek,

        @JsonProperty("slot")
        String slot,

        @JsonProperty("paathshaala_id")
        UUID paathshaalaId,
        
        @JsonProperty("paathshaala_name")
        String paathshaalaName
) {}
