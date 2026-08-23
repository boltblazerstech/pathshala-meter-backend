package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record CreatePaathshalaRequest(

        @NotBlank(message = "name is required")
        String name,

        @JsonProperty("map_link")
        String mapLink,

        Double lat,

        Double lng
) {}
