package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record CreateSupervisorRequest(

        @NotBlank(message = "name is required")
        String name,

        @NotBlank(message = "phone_number is required")
        @JsonProperty("phone_number")
        String phoneNumber,

        String password
) {}
