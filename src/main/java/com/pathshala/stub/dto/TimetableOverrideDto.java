package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.UUID;

public record TimetableOverrideDto(
        @JsonProperty("override_date")
        LocalDate overrideDate,

        @JsonProperty("slot")
        String slot,

        @JsonProperty("paathshaala_id")
        UUID paathshaalaId,
        
        @JsonProperty("paathshaala_name")
        String paathshaalaName
) {}
