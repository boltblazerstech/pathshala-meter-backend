package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateTeacherRequest(

        @NotBlank(message = "name is required")
        String name,

        @NotBlank(message = "phone_number is required")
        @JsonProperty("phone_number")
        String phoneNumber,

        @NotNull(message = "paathshaala_id is required")
        @JsonProperty("paathshaala_id")
        UUID paathshalaId,

        String password
) {}
