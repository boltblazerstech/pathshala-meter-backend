package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public record TimetableOverrideDeleteRequest(
        @JsonProperty("override_date")
        LocalDate overrideDate,

        @JsonProperty("slot")
        String slot
) {}
