package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record UpdateSelectedPaathshaalaRequest(
        @JsonProperty("paathshaala_id")
        UUID paathshaalaId
) {}
